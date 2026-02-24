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
import jakarta.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IDuplicateEntityHandler;
import it.eng.parer.soft.delete.beans.ILevelProcessor;
import it.eng.parer.soft.delete.beans.ISoftDeleteExecutor;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.cache.registry.DistributedTimestampRegistry;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.dto.EntityRelationKey;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroLogRichSoftDelete;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Classe helper per l'elaborazione di un singolo livello nella gerarchia.
 */
@ApplicationScoped
public class LevelProcessor implements ILevelProcessor {

    private static final Logger log = LoggerFactory.getLogger(LevelProcessor.class);
    private static final ZoneId APPLICATION_TIMEZONE = ZoneId.of("Europe/Berlin");

    private final EntityManager entityManager;
    private final DistributedTimestampRegistry timestampRegistry;
    private final RootEntityContext rootEntityContext;
    private final ISoftDeleteExecutor softDeleteExecutor;
    private final IDuplicateEntityHandler duplicateEntityHandler;

    @Inject
    public LevelProcessor(EntityManager entityManager,
            DistributedTimestampRegistry timestampRegistry, RootEntityContext rootEntityContext,
            ISoftDeleteExecutor softDeleteExecutor,
            IDuplicateEntityHandler duplicateEntityHandler) {
        this.entityManager = entityManager;
        this.timestampRegistry = timestampRegistry;
        this.rootEntityContext = rootEntityContext;
        this.softDeleteExecutor = softDeleteExecutor;
        this.duplicateEntityHandler = duplicateEntityHandler;
    }

    /**
     * Elabora un livello specifico nella gerarchia di entità
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public void processLevel(IStreamSource streamSource, SoftDeleteMode mode,
            AroItemRichSoftDelete item) throws AppGenericPersistenceException {

        int level = streamSource.getLevel();
        log.info("Elaborazione streaming del livello {} in modalità {}", level, mode);

        // Genera timestamp iniziale
        LocalDateTime initialTimestamp = LocalDateTime.now(APPLICATION_TIMEZONE);

        // Identifica tabelle che verranno elaborate
        Set<String> tablesToProcess = identifyTablesToProcess(streamSource);

        // Aggiorna il registro con il timestamp iniziale per ogni tabella
        for (String tableName : tablesToProcess) {
            timestampRegistry.updateTimestamp(tableName, initialTimestamp);
        }

        // Mappa per tenere traccia del conteggio per ogni tipo di entità
        Map<EntityRelationKey, Long> entityTypeCounter = new HashMap<>();

        try {
            // Elabora lo stream
            processStream(streamSource, entityTypeCounter, initialTimestamp, level, mode, item);

            // Persisti i log
            persistEntityUpdateLogs(entityTypeCounter, rootEntityContext.getCurrentRootId(), level,
                    item);

            log.info("Completata elaborazione del livello {} in modalità {}", level, mode);
        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Errore durante la cancellazione logica al livello " + level + " in modalità "
                            + mode + ": " + ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Identifica tutte le tabelle che verranno elaborate in questo livello
     */
    private Set<String> identifyTablesToProcess(IStreamSource streamSource) {
        Set<String> tables = new HashSet<>();

        try (Stream<EntityNode> nodeStream = streamSource.createStream()) {
            nodeStream.forEach(node -> tables
                    .add(JpaEntityReflectionHelper.getTableName(node.getEntityClass())));
        }

        return tables;
    }

    /**
     * Elabora lo stream di entità del livello corrente
     */
    private void processStream(IStreamSource source, Map<EntityRelationKey, Long> entityTypeCounter,
            LocalDateTime initialTimestamp, int level, SoftDeleteMode mode,
            AroItemRichSoftDelete item) {

        try (Stream<EntityNode> nodeStream = source.createStream()) {
            // Raggruppa entità durante lo stream
            Map<String, List<EntityNode>> entitiesByParentRelation = new HashMap<>();
            Map<String, List<EntityNode>> duplicatesByParentRelation = new HashMap<>();
            Set<EntityNode> standaloneEntities = new HashSet<>();

            // Prima passata: raggruppa le entità
            nodeStream.forEach(node -> {
                if (node.hasParent()) {
                    if (node.isDuplicate()) {
                        duplicatesByParentRelation
                                .computeIfAbsent(node.getRelationKey(), k -> new ArrayList<>())
                                .add(node);
                    } else {
                        entitiesByParentRelation
                                .computeIfAbsent(node.getRelationKey(), k -> new ArrayList<>())
                                .add(node);
                    }
                } else {
                    standaloneEntities.add(node);
                }
            });

            // Processa i tre gruppi di entità
            processStandaloneEntities(standaloneEntities, entityTypeCounter, initialTimestamp,
                    level, item);
            processGroupedEntities(entitiesByParentRelation, entityTypeCounter, initialTimestamp,
                    level, mode);
            processDuplicateEntities(duplicatesByParentRelation, entityTypeCounter,
                    initialTimestamp, level, mode, item);
        }
    }

    /**
     * Processa le entità standalone (senza parent)
     */
    private void processStandaloneEntities(Set<EntityNode> standaloneEntities,
            Map<EntityRelationKey, Long> entityTypeCounter, LocalDateTime initialTimestamp,
            int level, AroItemRichSoftDelete item) {

        for (EntityNode node : standaloneEntities) {
            String tableName = JpaEntityReflectionHelper.getTableName(node.getEntityClass());

            // Ottieni il timestamp più recente dalla cache distribuita
            LocalDateTime latestTimestamp = timestampRegistry.getLatestTimestamp(tableName,
                    initialTimestamp);

            int updated = softDeleteExecutor.softDeleteRootEntity(node.getEntityClass(),
                    node.getEntityId(), latestTimestamp, level,
                    rootEntityContext.getCurrentRootId());

            // Crea chiave per il conteggio
            EntityRelationKey relationKey = new EntityRelationKey(node.getEntityClass().getName(),
                    node.hasParent() ? node.getParentClass().getName() : null);
            long currentOffset = entityTypeCounter.getOrDefault(relationKey, 0L);

            // Aggiorna il contatore
            entityTypeCounter.put(relationKey, updated + currentOffset);

            // Aggiorna immediatamente lo stato dell'item dopo aver elaborato la root
            // entity
            updateItemStateIfNeeded(item, updated);
        }
    }

    /**
     * Processa entità raggruppate per relazione
     */
    private void processGroupedEntities(Map<String, List<EntityNode>> entitiesByParentRelation,
            Map<EntityRelationKey, Long> entityTypeCounter, LocalDateTime initialTimestamp,
            int level, SoftDeleteMode mode) {

        for (Map.Entry<String, List<EntityNode>> entry : entitiesByParentRelation.entrySet()) {
            List<EntityNode> nodes = entry.getValue();
            if (!nodes.isEmpty()) {
                EntityNode sample = nodes.get(0);
                String tableName = JpaEntityReflectionHelper.getTableName(sample.getEntityClass());

                // Ottieni il timestamp più recente dalla cache distribuita
                LocalDateTime latestTimestamp = timestampRegistry.getLatestTimestamp(tableName,
                        initialTimestamp);

                // Crea chiave per il conteggio
                EntityRelationKey relationKey = new EntityRelationKey(
                        sample.getEntityClass().getName(),
                        sample.hasParent() ? sample.getParentClass().getName() : null);
                long currentOffset = entityTypeCounter.getOrDefault(relationKey, 0L);

                // Verifica se usare la modalità completa
                boolean useCompleteMode = (mode == SoftDeleteMode.COMPLETA)
                        || sample.isForceCompleteMode();

                int updated;

                if (useCompleteMode) {
                    // Modalità COMPLETA: aggiorna tutti i figli con query bulk
                    updated = softDeleteExecutor.softDeleteAllEntityByParent(
                            sample.getEntityClass(), sample.getIdField(), sample.getParentId(),
                            sample.getParentField(), latestTimestamp, level, currentOffset,
                            rootEntityContext.getCurrentRootId());

                    log.info("Modalità COMPLETA: aggiornate {} entità {} per parent {}:{}", updated,
                            sample.getEntityClass().getSimpleName(),
                            sample.getParentClass().getSimpleName(), sample.getParentId());
                } else {
                    // Modalità CAMPIONE: aggiorna solo un figlio
                    updated = softDeleteExecutor.softDeleteOneEntity(sample.getEntityClass(),
                            sample.getEntityId(), sample.getParentId(), sample.getParentField(),
                            latestTimestamp, level, currentOffset,
                            rootEntityContext.getCurrentRootId());

                    log.info("Modalità CAMPIONE: aggiornata 1 entità {} (id: {}) per parent {}:{}",
                            sample.getEntityClass().getSimpleName(), sample.getEntityId(),
                            sample.getParentClass().getSimpleName(), sample.getParentId());
                }

                // Aggiorna il contatore
                entityTypeCounter.put(relationKey, currentOffset + updated);
            }
        }
    }

    /**
     * Processa entità duplicate raggruppate per relazione
     */
    private void processDuplicateEntities(Map<String, List<EntityNode>> duplicatesByParentRelation,
            Map<EntityRelationKey, Long> entityTypeCounter, LocalDateTime initialTimestamp,
            int level, SoftDeleteMode mode, AroItemRichSoftDelete item) {

        for (Map.Entry<String, List<EntityNode>> entry : duplicatesByParentRelation.entrySet()) {
            List<EntityNode> nodes = entry.getValue();
            if (!nodes.isEmpty()) {
                EntityNode sample = nodes.get(0);
                String tableName = JpaEntityReflectionHelper.getTableName(sample.getEntityClass());

                // Ottieni il timestamp più recente dalla cache distribuita
                LocalDateTime latestTimestamp = timestampRegistry.getLatestTimestamp(tableName,
                        initialTimestamp);

                // Crea chiave per il conteggio
                EntityRelationKey relationKey = new EntityRelationKey(
                        sample.getEntityClass().getName(),
                        sample.hasParent() ? sample.getParentClass().getName() : null);
                long currentOffset = entityTypeCounter.getOrDefault(relationKey, 0L);

                // Verifica se usare la modalità completa
                boolean useCompleteMode = (mode == SoftDeleteMode.COMPLETA)
                        || sample.isForceCompleteMode();

                int updated;

                if (useCompleteMode) {
                    // Modalità COMPLETA: inserisci tutti i figli nella tabella
                    // dei duplicati
                    updated = duplicateEntityHandler.insertDuplicateAllEntityByParent(
                            sample.getEntityClass(), sample.getIdField(), sample.getParentId(),
                            sample.getParentField(), latestTimestamp, level, currentOffset,
                            rootEntityContext.getCurrentRootId(), item);

                    log.info(
                            "Modalità COMPLETA (duplicato): inseriti {} record per {} con parent {}:{} nella tabella duplicati",
                            updated, sample.getEntityClass().getSimpleName(),
                            sample.getParentClass().getSimpleName(), sample.getParentId());
                } else {
                    // Modalità CAMPIONE: inserisci solo un figlio nella tabella
                    // dei duplicati
                    updated = duplicateEntityHandler.insertDuplicateOneEntity(
                            sample.getEntityClass(), sample.getEntityId(), sample.getParentId(),
                            sample.getParentField(), latestTimestamp, level, currentOffset,
                            rootEntityContext.getCurrentRootId(), item);

                    log.info(
                            "Modalità CAMPIONE (duplicato): inserito 1 record per {} (id: {}) con parent {}:{} nella tabella duplicati",
                            sample.getEntityClass().getSimpleName(), sample.getEntityId(),
                            sample.getParentClass().getSimpleName(), sample.getParentId());
                }

                // Aggiorna il contatore
                entityTypeCounter.put(relationKey, currentOffset + updated);
            }
        }
    }

    /**
     * Aggiorna lo stato dell'item se necessario
     */
    private void updateItemStateIfNeeded(AroItemRichSoftDelete item, int updated) {
        // Aggiorna immediatamente lo stato dell'item dopo aver elaborato la root entity.
        // Ciò permette di monitorare in tempo reale quali item sono stati completati.
        if (item != null && updated > 0) {
            item.setTiStatoItem(StatoItemRichSoftDelete.ELABORATO.name());
            item.setDtFineElab(LocalDateTime.now());
            entityManager.merge(item);
            log.info("Aggiornato stato dell'item {} a ELABORATO", item.getIdItemRichSoftDelete());
        }
    }

    /**
     * Persiste i log delle entità aggiornate
     */
    private void persistEntityUpdateLogs(Map<EntityRelationKey, Long> entityTypeCounter,
            Object rootId, int level, AroItemRichSoftDelete item) {

        if (entityTypeCounter.isEmpty()) {
            log.debug("Nessun log da persistere per il livello {}", level);
            return;
        }

        log.debug("Inizio persistenza log per {} tipi di entità nel livello {}",
                entityTypeCounter.size(), level);

        for (Map.Entry<EntityRelationKey, Long> entry : entityTypeCounter.entrySet()) {
            try {
                EntityRelationKey relationKey = entry.getKey();
                Long updated = entry.getValue();

                if (updated <= 0) {
                    continue; // Salta le entità senza aggiornamenti
                }

                String childClassName = relationKey.getChildClassName();
                String parentClassName = relationKey.getParentClassName();

                String childTable = JpaEntityReflectionHelper
                        .getTableName(Class.forName(childClassName));
                String parentTable = parentClassName != null
                        ? JpaEntityReflectionHelper.getTableName(Class.forName(parentClassName))
                        : "N/A";

                AroLogRichSoftDelete logRichSoftDelete = new AroLogRichSoftDelete();
                logRichSoftDelete.setAroItemRichSoftDelete(item);
                logRichSoftDelete.setIdUnitaDocRef(new BigDecimal((long) rootId));
                logRichSoftDelete.setNmParentTable(parentTable);
                logRichSoftDelete.setNmChildTable(childTable);
                logRichSoftDelete.setNiLevel(BigDecimal.valueOf(level));
                logRichSoftDelete.setUpdatedRowCount(BigDecimal.valueOf(updated));

                entityManager.persist(logRichSoftDelete);

            } catch (ClassNotFoundException e) {
                // Gestisci l'errore senza interrompere l'intero batch
                log.warn("Impossibile risolvere la classe per il log: {}", entry.getKey(), e);
            }
        }

        log.debug("Completata persistenza dei log per il livello {}", level);
    }
}
