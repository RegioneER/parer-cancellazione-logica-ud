/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.beans.service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IRelationQueryExecutor;
import it.eng.parer.soft.delete.beans.annotations.RelationQueries;
import it.eng.parer.soft.delete.beans.annotations.RelationQuery;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;

@ApplicationScoped
public class RelationQueryExecutor implements IRelationQueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RelationQueryExecutor.class);

    private static final String PARAM_ID = "Impostato parametro {} = {}";

    private EntityManager entityManager;

    private RootEntityContext rootEntityContext;

    @Inject
    public RelationQueryExecutor(EntityManager entityManager, RootEntityContext rootEntityContext) {
        this.entityManager = entityManager;
        this.rootEntityContext = rootEntityContext;
    }

    // Cache delle query ottimizzate per relazione e livello
    private final Map<String, Map<Integer, RelationQuery>> queriesByRelationAndLevel = new ConcurrentHashMap<>();

    /**
     * Verifica se esiste una query ottimizzata per questa relazione al livello specificato
     */
    @Override
    public boolean hasOptimizedQueryFor(Class<?> childClass, Class<?> parentClass, int level)
            throws AppGenericPersistenceException {
        String relationKey = childClass.getName() + "->" + parentClass.getName();

        if (!queriesByRelationAndLevel.containsKey(relationKey)) {
            loadQueryAnnotations(childClass, parentClass, relationKey);
        }

        Map<Integer, RelationQuery> levelQueries = queriesByRelationAndLevel.get(relationKey);
        if (levelQueries == null) {
            return false;
        }

        // Verifica se esiste una query specifica per questo livello o una query per tutti i
        // livelli
        // (-1)
        return levelQueries.containsKey(level) || levelQueries.containsKey(-1);
    }

    /**
     * Carica le annotazioni RelationQuery per la coppia child-parent
     */
    private void loadQueryAnnotations(Class<?> childClass, Class<?> parentClass,
            String relationKey) {
        Map<Integer, RelationQuery> levelQueries = new HashMap<>();

        // Cerca le annotazioni RelationQuery
        RelationQuery[] queries;
        if (childClass.isAnnotationPresent(RelationQueries.class)) {
            // Multiple annotazioni
            RelationQueries container = childClass.getAnnotation(RelationQueries.class);
            queries = container.value();
        } else if (childClass.isAnnotationPresent(RelationQuery.class)) {
            // Singola annotazione
            queries = new RelationQuery[] {
                    childClass.getAnnotation(RelationQuery.class) };
        } else {
            // Nessuna annotazione
            queries = new RelationQuery[0];
        }

        // Filtra le query per questa relazione parent-child
        for (RelationQuery query : queries) {
            if (query.parentClass().equals(parentClass)) {
                if (query.levels().length == 0) {
                    // Se non sono specificati livelli, la query si applica a
                    // tutti i livelli
                    levelQueries.put(-1, query);
                } else {
                    // Altrimenti, registra la query per ciascun livello
                    // specificato
                    for (int level : query.levels()) {
                        levelQueries.put(level, query);
                    }
                }
            }
        }

        if (!levelQueries.isEmpty()) {
            queriesByRelationAndLevel.put(relationKey, levelQueries);
        }
    }

    /**
     * Ottiene la query più adatta per il livello specificato
     */
    private RelationQuery getBestQueryForLevel(Class<?> childClass, Class<?> parentClass,
            int level) {
        String relationKey = childClass.getName() + "->" + parentClass.getName();

        if (!queriesByRelationAndLevel.containsKey(relationKey)) {
            loadQueryAnnotations(childClass, parentClass, relationKey);
        }

        Map<Integer, RelationQuery> levelQueries = queriesByRelationAndLevel.get(relationKey);
        if (levelQueries == null) {
            return null;
        }

        // Preferisci la query specifica per questo livello, altrimenti usa quella generica
        return levelQueries.getOrDefault(level, levelQueries.get(-1));
    }

    /**
     * Verifica se una query ottimizzata supporta operazioni batch (contiene già una clausola IN)
     */
    @Override
    public boolean supportsBatchQuery(Class<?> childClass, Class<?> parentClass, int level)
            throws AppGenericPersistenceException {
        RelationQuery annotation = getBestQueryForLevel(childClass, parentClass, level);
        if (annotation == null) {
            return false;
        }

        String queryString = annotation.query();
        // Controlla prima il parentIdsParam esplicito se specificato
        String batchParamName = annotation.parentIdsParam();
        if (batchParamName.isEmpty()) {
            // Se non specificato, usa la convenzione: parentIdParam + "s"
            batchParamName = annotation.parentIdParam() + "s";
        }

        // Verifica se la query contiene già una clausola IN con il parametro batch
        return queryString.contains("IN :" + batchParamName)
                || queryString.contains("IN:" + batchParamName)
                || queryString.contains("in :" + batchParamName)
                || queryString.contains("in:" + batchParamName);
    }

    /**
     * Esegue una query ottimizzata per una relazione e restituisce uno stream di EntityNode. Il
     * risultato è uno stream che può essere utilizzato in un blocco try-with-resources.
     */
    @Override
    public Stream<EntityNode> executeQueryForRelation(Class<?> childClass, Class<?> parentClass,
            Field childIdField, Field parentField, Object parentId, int level)
            throws AppGenericPersistenceException {

        try {
            // Ottieni la query ottimizzata per questa relazione e livello
            RelationQuery annotation = getBestQueryForLevel(childClass, parentClass, level);

            if (annotation == null) {
                log.warn(
                        "Nessuna query ottimizzata trovata per la relazione {} -> {} al livello {}",
                        childClass.getSimpleName(), parentClass.getSimpleName(), level);
                return Stream.empty();
            }

            // Recupera l'ID root dal contesto
            Object rootId = rootEntityContext.getCurrentRootId();
            if (rootId == null) {
                log.warn("ID root non trovato nel contesto per la relazione {} -> {} al livello {}",
                        childClass.getSimpleName(), parentClass.getSimpleName(), level);
                return Stream.empty();
            }

            // Log dei parametri usati
            log.debug(
                    "Esecuzione query ottimizzata per relazione {} -> {} (livello {}), rootId={}, parentId={}",
                    childClass.getSimpleName(), parentClass.getSimpleName(), level, rootId,
                    parentId);

            String queryString = annotation.query();
            Query query = entityManager.createQuery(queryString);

            // Imposta i parametri della query
            setQueryParameters(query, queryString, annotation, rootId, parentId);

            // Ottimizza la query con hint appropriati
            configureQueryHints(query);

            // Ottieni lo stream dei risultati
            Stream<Object[]> resultStream = query.getResultStream();

            // Trasforma i risultati in EntityNode
            Stream<EntityNode> nodeStream = resultStream
                    .map(row -> createEntityNodeFromQueryResult(row, childClass, childIdField,
                            level, parentClass, parentField))
                    .filter(Objects::nonNull);

            // Aggiungi handler onClose per garantire la pulizia delle risorse
            return nodeStream
                    .onClose(() -> safelyCloseStream(resultStream, childClass, parentClass, level));

        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Errore nell'esecuzione della query ottimizzata per relazione "
                            + childClass.getSimpleName() + " -> " + parentClass.getSimpleName()
                            + " al livello " + level + ": " + e.getMessage());
        }
    }

    /**
     * Esegue una query ottimizzata per batch di parentId
     */
    @Override
    public Stream<EntityNode> executeQueryForRelationBatch(Class<?> childClass,
            Class<?> parentClass, Field childIdField, Field parentField, List<Object> parentIds,
            int level) throws AppGenericPersistenceException {

        if (parentIds == null || parentIds.isEmpty()) {
            log.debug("Lista di parentId vuota, nessuna query da eseguire");
            return Stream.empty();
        }

        try {
            // Ottieni la query ottimizzata
            RelationQuery annotation = getBestQueryForLevel(childClass, parentClass, level);

            if (annotation == null) {
                log.warn(
                        "Nessuna query ottimizzata trovata per batch relazione {} -> {} al livello {}",
                        childClass.getSimpleName(), parentClass.getSimpleName(), level);
                return Stream.empty();
            }

            // Recupera l'ID root dal contesto
            Object rootId = rootEntityContext.getCurrentRootId();
            if (rootId == null) {
                log.warn(
                        "ID root non trovato nel contesto per batch relazione {} -> {} al livello {}",
                        childClass.getSimpleName(), parentClass.getSimpleName(), level);
                return Stream.empty();
            }

            // Determina il nome del parametro batch
            String batchParamName = annotation.parentIdsParam();
            if (batchParamName.isEmpty()) {
                // Se non specificato, usa la convenzione: parentIdParam + "s"
                batchParamName = annotation.parentIdParam() + "s";
            }

            log.debug(
                    "Esecuzione query batch ottimizzata per relazione {} -> {} (livello {}), rootId={}, {} parentIds, usando parametro:{}",
                    childClass.getSimpleName(), parentClass.getSimpleName(), level, rootId,
                    parentIds.size(), batchParamName);

            // Utilizza la query originale senza modifiche
            String queryString = annotation.query();

            Query query = entityManager.createQuery(queryString);

            // Imposta il parametro rootId se presente nella query
            if (queryString.contains(":" + annotation.rootIdParam())) {
                query.setParameter(annotation.rootIdParam(), rootId);
                log.debug(PARAM_ID, annotation.rootIdParam(), rootId);
            }

            // Imposta la lista di parentIds con il nome del parametro appropriato
            if (queryString.contains(":" + batchParamName)) {
                query.setParameter(batchParamName, parentIds);
                log.debug("Impostato parametro {} con {} valori", batchParamName, parentIds.size());
            } else {
                log.warn("Parametro batch {} non trovato nella query", batchParamName);
            }

            // Ottimizza la query con hint
            configureQueryHints(query);

            // Ottieni lo stream dei risultati
            Stream<Object[]> resultStream = query.getResultStream();

            // Trasforma i risultati in EntityNode
            Stream<EntityNode> nodeStream = resultStream
                    .map(row -> createEntityNodeFromQueryResult(row, childClass, childIdField,
                            level, parentClass, parentField))
                    .filter(Objects::nonNull);

            // Aggiungi handler onClose per pulizia risorse
            return nodeStream
                    .onClose(() -> safelyCloseStream(resultStream, childClass, parentClass, level));

        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Errore nell'esecuzione della query batch ottimizzata per relazione "
                            + childClass.getSimpleName() + " -> " + parentClass.getSimpleName()
                            + " al livello " + level + ": " + e.getMessage());
        }
    }

    /**
     * Imposta i parametri della query in base alle annotazioni
     */
    private void setQueryParameters(Query query, String queryString, RelationQuery annotation,
            Object rootId, Object parentId) {

        // Imposta il parametro rootId se presente nella query
        if (queryString.contains(":" + annotation.rootIdParam())) {
            query.setParameter(annotation.rootIdParam(), rootId);
            log.debug(PARAM_ID, annotation.rootIdParam(), rootId);
        }

        // Imposta il parametro parentId se presente nella query
        if (queryString.contains(":" + annotation.parentIdParam())) {
            query.setParameter(annotation.parentIdParam(), parentId);
            log.debug(PARAM_ID, annotation.parentIdParam(), parentId);
        }
    }

    /**
     * Configura gli hint di ottimizzazione per la query
     */
    private void configureQueryHints(Query query) {
        // Ottimizza la dimensione del fetch per migliorare le prestazioni
        query.setHint(HibernateHints.HINT_FETCH_SIZE, 100);

        // Aggiungi ulteriori hint se necessario
        query.setHint("org.hibernate.readOnly", true); // Ottimizzazione per query di sola
        // lettura
    }

    /**
     * Crea un EntityNode dai risultati della query
     */
    private EntityNode createEntityNodeFromQueryResult(Object[] row, Class<?> childClass,
            Field childIdField, int level, Class<?> parentClass, Field parentField) {

        try {
            // Controllo validità dei risultati
            if (row == null || row.length < 2) {
                log.warn("Risultato query incompleto o null, attesi almeno 2 valori, trovati: {}",
                        row == null ? 0 : row.length);
                return null;
            }

            Object childId = row[0];
            Object parentIdResult = row[1];

            // Verifica che gli ID non siano null
            if (childId == null) {
                log.warn("ID figlio null per classe {}", childClass.getSimpleName());
                return null;
            }

            // Crea un nuovo nodo EntityNode
            return new EntityNode(childClass, childId, childIdField, level, parentClass,
                    parentIdResult, parentField);
        } catch (Exception e) {
            log.warn("Errore durante la mappatura del risultato della query per {}: {}",
                    childClass.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Chiude in modo sicuro lo stream di risultati
     */
    private void safelyCloseStream(Stream<?> stream, Class<?> childClass, Class<?> parentClass,
            int level) {
        try {
            if (stream != null) {
                stream.close();
                log.debug("Stream chiuso per la relazione {} -> {} (livello {})",
                        childClass.getSimpleName(), parentClass.getSimpleName(), level);
            }
        } catch (Exception e) {
            log.warn("Errore nella chiusura dello stream di risultati: {}", e.getMessage());
        }
    }
}
