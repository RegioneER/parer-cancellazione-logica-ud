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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Query;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IEntityRelationResolver;
import it.eng.parer.soft.delete.beans.IRelationQueryExecutor;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Classe helper responsabile della risoluzione delle relazioni tra entità.
 */
@ApplicationScoped
public class EntityRelationResolver implements IEntityRelationResolver {

    private static final Logger log = LoggerFactory.getLogger(EntityRelationResolver.class);
    private static final int MAX_BATCH_SIZE = 999; // Limite Oracle per IN clause

    private final EntityManager entityManager;
    private final IRelationQueryExecutor relationQueryExecutor;

    @Inject
    public EntityRelationResolver(EntityManager entityManager,
            IRelationQueryExecutor relationQueryExecutor) {
        this.entityManager = entityManager;
        this.relationQueryExecutor = relationQueryExecutor;
    }

    /**
     * Trova le entità figlie per un batch di nodi parent dello stesso tipo.
     */
    public void findBatchChildEntities(Class<?> parentClass, List<Object> parentIds,
            int parentLevel, Set<String> processedEntities, Consumer<EntityNode> childConsumer)
            throws AppGenericPersistenceException {

        // Ottieni il campo ID della classe parent
        Field parentIdField = JpaEntityReflectionHelper.getIdField(parentClass);
        if (parentIdField == null) {
            log.warn("Campo ID non trovato per la classe {}", parentClass.getSimpleName());
            return;
        }

        // Elabora le associazioni OneToMany in batch
        processOneToManyRelations(parentClass, parentIds, parentIdField, parentLevel,
                processedEntities, childConsumer);

        // Elabora le associazioni OneToOne in batch
        processOneToOneRelations(parentClass, parentIds, parentIdField, parentLevel,
                processedEntities, childConsumer);
    }

    /**
     * Elabora le associazioni OneToMany per un batch di entità parent dello stesso tipo
     */
    public void processOneToManyRelations(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, int parentLevel, Set<String> processedEntities,
            Consumer<EntityNode> childConsumer) throws AppGenericPersistenceException {

        if (parentIds.isEmpty()) {
            return;
        }

        // Usa la cache per le relazioni OneToMany
        List<Field> oneToManyFields = JpaEntityReflectionHelper.getOneToManyFields(parentClass);

        for (Field field : oneToManyFields) {
            Class<?> childClass = JpaEntityReflectionHelper.getFirstActualTypeArgument(field);
            if (childClass != null) {
                String mappedByFieldName = JpaEntityReflectionHelper.getMappedByFieldName(field);
                if (mappedByFieldName == null || mappedByFieldName.isEmpty()) {
                    log.warn("Campo mappedBy non definito per {}.{}", parentClass.getSimpleName(),
                            field.getName());
                    continue;
                }

                try {
                    Field specificManyToOneField = childClass.getDeclaredField(mappedByFieldName);
                    if (!specificManyToOneField.isAnnotationPresent(ManyToOne.class)) {
                        log.warn("Campo {} in {} non ha l'annotazione @ManyToOne",
                                mappedByFieldName, childClass.getSimpleName());
                        continue;
                    }

                    Field childIdField = JpaEntityReflectionHelper.getIdField(childClass);
                    if (childIdField != null) {
                        processBatchChildEntities(parentClass, parentIds, parentIdField, childClass,
                                childIdField, specificManyToOneField, parentLevel,
                                processedEntities, childConsumer);
                    }
                } catch (NoSuchFieldException e) {
                    log.warn("Campo mappedBy {} non trovato in {}: {}", mappedByFieldName,
                            childClass.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Elabora le associazioni OneToOne per un batch di entità parent dello stesso tipo
     */
    public void processOneToOneRelations(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, int parentLevel, Set<String> processedEntities,
            Consumer<EntityNode> childConsumer) throws AppGenericPersistenceException {

        if (parentIds.isEmpty()) {
            return;
        }

        // Usa la cache per le relazioni OneToOne
        List<Field> oneToOneFields = JpaEntityReflectionHelper.getOneToOneFields(parentClass);

        for (Field field : oneToOneFields) {
            Class<?> childClass = field.getType();
            if (childClass != null) {
                Field inverseOneToOneField = JpaEntityReflectionHelper
                        .getInverseOneToOneField(childClass, parentClass);
                Field childIdField = JpaEntityReflectionHelper.getIdField(childClass);

                if (inverseOneToOneField != null && childIdField != null) {
                    processBatchChildEntities(parentClass, parentIds, parentIdField, childClass,
                            childIdField, inverseOneToOneField, parentLevel, processedEntities,
                            childConsumer);
                }
            }
        }
    }

    /**
     * Processa le entità figlie in batch per tutti i parent forniti
     */
    public void processBatchChildEntities(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, Class<?> childClass, Field childIdField, Field relationField,
            int parentLevel, Set<String> processedEntities, Consumer<EntityNode> childConsumer)
            throws AppGenericPersistenceException {

        try {
            if (parentIds.isEmpty()) {
                return;
            }

            int childLevel = parentLevel + 1;

            // Verifica se esiste una query ottimizzata per questa relazione
            boolean useOptimizedQuery = relationQueryExecutor.hasOptimizedQueryFor(childClass,
                    parentClass, childLevel);

            if (useOptimizedQuery) {
                processWithOptimizedQuery(parentClass, parentIds, childClass, childIdField,
                        relationField, processedEntities, childConsumer, childLevel);
            } else {
                processWithStandardQuery(parentClass, parentIds, parentIdField, childClass,
                        childIdField, relationField, parentLevel, processedEntities, childConsumer,
                        childLevel);
            }

        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Errore durante il recupero batch dei figli di tipo "
                            + childClass.getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Elabora la relazione con query ottimizzata
     */
    private void processWithOptimizedQuery(Class<?> parentClass, List<Object> parentIds,
            Class<?> childClass, Field childIdField, Field relationField,
            Set<String> processedEntities, Consumer<EntityNode> childConsumer, int childLevel)
            throws AppGenericPersistenceException {

        // Verifica se la query supporta batch
        boolean supportsBatch = relationQueryExecutor.supportsBatchQuery(childClass, parentClass,
                childLevel);

        if (supportsBatch && parentIds.size() > 1) {
            // Dividi i parentIds in batch più piccoli se necessario
            for (List<Object> batchParentIds : partitionList(parentIds, MAX_BATCH_SIZE)) {
                try (Stream<EntityNode> nodeStream = relationQueryExecutor
                        .executeQueryForRelationBatch(childClass, parentClass, childIdField,
                                relationField, batchParentIds, childLevel)) {

                    // Processa i nodi risultanti, gestendo i duplicati
                    final int[] processed = {
                            0 };
                    nodeStream.forEach(node -> {
                        processNode(node, processedEntities, childConsumer);
                        processed[0]++;
                    });
                    log.debug("Query batch completata: elaborati {} figli {} per {} parent",
                            processed[0], childClass.getSimpleName(), batchParentIds.size());
                }
            }
        } else {
            // Fallback all'approccio originale per query ottimizzate che non supportano
            // batch
            for (Object parentId : parentIds) {
                try (Stream<EntityNode> nodeStream = relationQueryExecutor.executeQueryForRelation(
                        childClass, parentClass, childIdField, relationField, parentId,
                        childLevel)) {

                    final int[] processed = {
                            0 };
                    nodeStream.forEach(node -> {
                        processNode(node, processedEntities, childConsumer);
                        processed[0]++;
                    });

                    log.debug("Query ottimizzata completata: elaborati {} figli {} per parent {}",
                            processed[0], childClass.getSimpleName(), parentId);
                }
            }
        }
    }

    /**
     * Elabora la relazione con query standard
     */
    private void processWithStandardQuery(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, Class<?> childClass, Field childIdField, Field relationField,
            int parentLevel, Set<String> processedEntities, Consumer<EntityNode> childConsumer,
            int childLevel) {

        // Fallback: Query batch standard, suddivisa in batch più piccoli
        for (List<Object> batchParentIds : partitionList(parentIds, MAX_BATCH_SIZE)) {
            String queryString = String.format(
                    "select c.%s, c.%s.%s from %s c where c.%s.%s IN :parentIds",
                    childIdField.getName(), relationField.getName(), parentIdField.getName(),
                    childClass.getSimpleName(), relationField.getName(), parentIdField.getName());

            Query query = entityManager.createQuery(queryString)
                    .setParameter("parentIds", batchParentIds)
                    .setHint(HibernateHints.HINT_FETCH_SIZE, 100);

            log.debug(
                    "Esecuzione query batch standard per {} figli di {}/{} padri di tipo {} (livello {})",
                    childClass.getSimpleName(), batchParentIds.size(), parentIds.size(),
                    parentClass.getSimpleName(), parentLevel);

            long startTime = System.currentTimeMillis();
            final int[] processed = {
                    0 };

            try (Stream<Object[]> resultStream = query.getResultStream()) {
                resultStream.forEach(result -> {
                    Object childId = result[0];
                    Object parentId = result[1];

                    String entityKey = childClass.getName() + ":" + childId;
                    boolean alreadyProcessed = processedEntities.contains(entityKey);

                    EntityNode childNode;
                    if (!alreadyProcessed) {
                        processedEntities.add(entityKey);
                        childNode = new EntityNode(childClass, childId, childIdField, childLevel,
                                parentClass, parentId, relationField);
                    } else {
                        // È un duplicato
                        childNode = new EntityNode(childClass, childId, childIdField, childLevel,
                                parentClass, parentId, relationField, true);
                    }

                    childConsumer.accept(childNode);
                    processed[0]++;
                });
            }

            long endTime = System.currentTimeMillis();
            log.debug("Completato streaming di {} figli {} in {} ms per batch di {} parent",
                    processed[0], childClass.getSimpleName(), (endTime - startTime),
                    batchParentIds.size());
        }
    }

    /**
     * Processa un singolo nodo entità
     */
    private void processNode(EntityNode node, Set<String> processedEntities,
            Consumer<EntityNode> childConsumer) {
        String entityKey = node.getEntityClass().getName() + ":" + node.getEntityId();
        boolean alreadyProcessed = processedEntities.contains(entityKey);

        if (!alreadyProcessed) {
            processedEntities.add(entityKey);
        } else {
            // È un duplicato, crea un nuovo nodo con isDuplicate = true
            node = new EntityNode(node.getEntityClass(), node.getEntityId(), node.getIdField(),
                    node.getLevel(), node.getParentClass(), node.getParentId(),
                    node.getParentField(), true);
        }

        childConsumer.accept(node);
    }

    /**
     * Suddivide una lista in sottoliste di dimensione massima specificata
     */
    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            int endIndex = Math.min(i + size, list.size());
            partitions.add(list.subList(i, endIndex));
        }
        return partitions;
    }
}
