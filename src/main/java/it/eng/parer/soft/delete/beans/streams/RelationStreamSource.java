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

package it.eng.parer.soft.delete.beans.streams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.service.RelationQueryExecutor;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;

/**
 * StreamSource che genera nodi figli per una lista di relazioni
 */
public class RelationStreamSource implements IStreamSource {

    private static final Logger log = LoggerFactory.getLogger(RelationStreamSource.class);

    private final int level;

    private final List<EntityRelation> relations;

    private final RelationQueryExecutor relationQueryExecutor;

    private final EntityManager entityManager;

    public RelationStreamSource(int level, List<EntityRelation> relations,
            RelationQueryExecutor relationQueryExecutor, EntityManager entityManager) {
        this.level = level;
        this.relations = relations;
        this.relationQueryExecutor = relationQueryExecutor;
        this.entityManager = entityManager;
    }

    @Override
    public Stream<EntityNode> createStream() {
        // Raggruppa le relazioni per tipo (childClass + parentClass)
        Map<String, List<EntityRelation>> relationsByType = new HashMap<>();

        for (EntityRelation relation : relations) {
            String relationKey = relation.getChildClass().getName() + "->"
                    + relation.getParentClass().getName();
            relationsByType.computeIfAbsent(relationKey, k -> new ArrayList<>()).add(relation);
        }

        // Stream risultanti per ciascun tipo di relazione
        List<Stream<EntityNode>> streams = new ArrayList<>();

        // Per ogni tipo di relazione
        for (List<EntityRelation> relationsOfSameType : relationsByType.values()) {
            if (relationsOfSameType.isEmpty()) {
                continue;
            }

            // Estrai informazioni comuni dal primo elemento
            EntityRelation sample = relationsOfSameType.get(0);
            Class<?> childClass = sample.getChildClass();
            Class<?> parentClass = sample.getParentClass();

            // Verifica se esiste query ottimizzata per questa relazione
            boolean useOptimizedQuery = false;
            try {
                useOptimizedQuery = relationQueryExecutor.hasOptimizedQueryFor(childClass,
                        parentClass, level);
            } catch (Exception e) {
                log.warn("Errore nel verificare query ottimizzata per {} -> {}: {}",
                        childClass.getSimpleName(), parentClass.getSimpleName(), e.getMessage());
            }

            if (useOptimizedQuery) {
                // CASO QUERY OTTIMIZZATE
                boolean supportsBatch = false;
                try {
                    supportsBatch = relationQueryExecutor.supportsBatchQuery(childClass,
                            parentClass, level);
                } catch (Exception e) {
                    log.warn("Errore nel verificare supporto batch per {} -> {}: {}",
                            childClass.getSimpleName(), parentClass.getSimpleName(),
                            e.getMessage());
                }

                if (supportsBatch && relationsOfSameType.size() > 1) {
                    // QUERY OTTIMIZZATA BATCH
                    try {
                        List<Object> parentIds = relationsOfSameType.stream()
                                .map(r -> r.getParentId()).toList();

                        // La query batch restituisce tutti i figli, ma ci
                        // basta un rappresentante
                        // per relazione
                        Stream<EntityNode> batchStream = relationQueryExecutor
                                .executeQueryForRelationBatch(childClass, parentClass,
                                        sample.getChildIdField(), sample.getParentField(),
                                        parentIds, level);

                        // Filtra per ottenere solo un rappresentante per
                        // parentId
                        Map<Object, EntityNode> representativesByParentId = new HashMap<>();
                        batchStream.forEach(node ->
                        // Mantiene solo il primo nodo per ciascun parentId
                        representativesByParentId.putIfAbsent(node.getParentId(), node));

                        // Aggiunge lo stream dei rappresentanti
                        Stream<EntityNode> representativesStream = representativesByParentId
                                .values().stream();
                        streams.add(representativesStream);

                        log.debug("Creato stream di {} rappresentanti da query batch per {} -> {}",
                                representativesByParentId.size(), childClass.getSimpleName(),
                                parentClass.getSimpleName());

                    } catch (Exception e) {
                        log.warn("Errore nell'esecuzione della query batch per {} -> {}: {}",
                                childClass.getSimpleName(), parentClass.getSimpleName(),
                                e.getMessage());

                        // Fallback a query singole in caso di errore
                        addSingleQueryStreams(streams, relationsOfSameType);
                    }
                } else {
                    // QUERY OTTIMIZZATE SINGOLE (una per relazione)
                    addSingleQueryStreams(streams, relationsOfSameType);
                }
            } else {
                // CASO QUERY STANDARD
                // Valuta se usare un approccio batch anche per query standard
                if (relationsOfSameType.size() > 1) {
                    try {
                        // Costruzione query batch standard
                        List<Object> parentIds = relationsOfSameType.stream()
                                .map(r -> r.getParentId()).toList();

                        String queryString = String.format(
                                "SELECT c.%s, c.%s.%s FROM %s c WHERE c.%s.%s IN :parentIds",
                                sample.getChildIdField().getName(),
                                sample.getParentField().getName(),
                                JpaEntityReflectionHelper.getIdField(parentClass).getName(),
                                childClass.getSimpleName(), sample.getParentField().getName(),
                                JpaEntityReflectionHelper.getIdField(parentClass).getName());

                        Query query = entityManager.createQuery(queryString)
                                .setParameter("parentIds", parentIds)
                                .setHint(HibernateHints.HINT_FETCH_SIZE, 100);

                        Stream<Object[]> resultStream = query.getResultStream();

                        // Trasformazione in EntityNode mantenendo la
                        // tracciabilità del parentId
                        Map<Object, EntityNode> nodesByParentId = new HashMap<>();
                        Set<Object> duplicateParentIds = relationsOfSameType.stream()
                                .filter(r -> r.isDuplicate()).map(r -> r.getParentId())
                                .collect(Collectors.toSet());

                        resultStream.forEach(result -> {
                            Object childId = result[0];
                            Object parentId = result[1];

                            // Verifica duplicati in O(1)
                            boolean isDuplicate = duplicateParentIds.contains(parentId);

                            // Mantiene solo un rappresentante per
                            // parentId
                            nodesByParentId.putIfAbsent(parentId,
                                    new EntityNode(childClass, childId, sample.getChildIdField(),
                                            level, parentClass, parentId, sample.getParentField(),
                                            isDuplicate));
                        });

                        streams.add(nodesByParentId.values().stream());

                        log.debug(
                                "Creato stream di {} rappresentanti da query standard batch per {} -> {}",
                                nodesByParentId.size(), childClass.getSimpleName(),
                                parentClass.getSimpleName());

                    } catch (Exception e) {
                        log.warn("Errore nella query batch standard per {} -> {}: {}",
                                childClass.getSimpleName(), parentClass.getSimpleName(),
                                e.getMessage());

                        // Fallback a query singole
                        addStandardQueryStreams(streams, relationsOfSameType);
                    }
                } else {
                    // Query standard singole (una per relazione)
                    addStandardQueryStreams(streams, relationsOfSameType);
                }
            }
        }

        // Concatena tutti gli stream
        if (streams.isEmpty()) {
            return Stream.empty();
        } else if (streams.size() == 1) {
            return streams.get(0);
        } else {
            Stream<EntityNode> result = streams.get(0);
            for (int i = 1; i < streams.size(); i++) {
                result = Stream.concat(result, streams.get(i));
            }
            return result;
        }
    }

    // Metodo helper per aggiungere query ottimizzate singole
    private void addSingleQueryStreams(List<Stream<EntityNode>> streams,
            List<EntityRelation> relations) {
        for (EntityRelation relation : relations) {
            try {
                Stream<EntityNode> stream = relationQueryExecutor.executeQueryForRelation(
                        relation.getChildClass(), relation.getParentClass(),
                        relation.getChildIdField(), relation.getParentField(),
                        relation.getParentId(), level);

                // Limita a un rappresentante per relazione
                Stream<EntityNode> limitedStream = stream.limit(1);
                streams.add(limitedStream);

            } catch (Exception e) {
                log.warn("Errore nell'esecuzione query ottimizzata per {} -> {}: {}",
                        relation.getChildClass().getSimpleName(),
                        relation.getParentClass().getSimpleName(), e.getMessage());
            }
        }
    }

    // Metodo helper per aggiungere query standard singole
    private void addStandardQueryStreams(List<Stream<EntityNode>> streams,
            List<EntityRelation> relations) {
        for (EntityRelation relation : relations) {
            try {
                // Crea query per questa relazione
                String queryString = String.format("select c.%s from %s c where c.%s.%s = :pid",
                        relation.getChildIdField().getName(),
                        relation.getChildClass().getSimpleName(),
                        relation.getParentField().getName(),
                        JpaEntityReflectionHelper.getIdField(relation.getParentClass()).getName());

                Query query = entityManager.createQuery(queryString)
                        .setParameter("pid", relation.getParentId()).setMaxResults(1)
                        .setHint(HibernateHints.HINT_FETCH_SIZE, 100);

                Stream<Object> idStream = query.getResultStream();

                Stream<EntityNode> nodeStream = idStream
                        .map(childId -> new EntityNode(relation.getChildClass(), childId,
                                relation.getChildIdField(), level, relation.getParentClass(),
                                relation.getParentId(), relation.getParentField(),
                                relation.isDuplicate()));

                streams.add(nodeStream);

            } catch (Exception e) {
                log.warn("Errore nell'esecuzione query standard per {} -> {}: {}",
                        relation.getChildClass().getSimpleName(),
                        relation.getParentClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public int getLevel() {
        return level;
    }
}
