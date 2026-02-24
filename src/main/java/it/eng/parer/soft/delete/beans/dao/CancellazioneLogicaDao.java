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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.ICancellazioneLogicaDao;
import it.eng.parer.soft.delete.beans.IEntityHierarchyBuilder;
import it.eng.parer.soft.delete.beans.ILevelProcessor;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.beans.utils.CostantiDB;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.TiItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.viewEntity.AroVLisItemRichSoftDelete;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;

@ApplicationScoped
public class CancellazioneLogicaDao implements ICancellazioneLogicaDao {

    private static final Logger log = LoggerFactory.getLogger(CancellazioneLogicaDao.class);

    private static final ZoneId APPLICATION_TIMEZONE = ZoneId.of("Europe/Berlin");

    private static final String SKIP_LOCKED = "-2";

    private static final String ID_RICH_SOFT_DELETE = "idRichSoftDelete";
    private static final String TI_ITEM_RICH_SOFT_DELETE = "tiItemRichSoftDelete";

    private final EntityManager entityManager;

    private final RootEntityContext rootEntityContext;

    private final IEntityHierarchyBuilder entityHierarchyBuilder;

    private final ILevelProcessor levelProcessor;

    @Inject
    public CancellazioneLogicaDao(EntityManager entityManager, RootEntityContext rootEntityContext,
            IEntityHierarchyBuilder entityHierarchyBuilder, ILevelProcessor levelProcessor) {
        this.entityManager = entityManager;
        this.rootEntityContext = rootEntityContext;
        this.entityHierarchyBuilder = entityHierarchyBuilder;
        this.levelProcessor = levelProcessor;
    }

    @ConfigProperty(name = "quarkus.uuid")
    String instanceId;

    @ConfigProperty(name = "worker.batch.size", defaultValue = "5")
    int batchSize;

    @ConfigProperty(name = "worker.poll.enabled", defaultValue = "true")
    boolean pollingEnabled;

    @ConfigProperty(name = "worker.claim.timeout-minutes", defaultValue = "30")
    int claimTimeoutMinutes;

    @ConfigProperty(name = "kafka.verify.timeout-minutes", defaultValue = "15")
    int kafkaVerifyTimeoutMinutes;

    /**
     * Ricavo gli item della richiesta corrente per tipo specifico con stato DA_ELABORARE
     *
     * @param idRichSoftDelete     l'id della richiesta corrente
     * @param tiItemRichSoftDelete tipo di item: UNI_DOC, ANN_VERS, REST_ARCH, SCARTO_ARCH
     *
     * @return lista oggetti di tipo {@link AroItemRichSoftDelete}
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public List<AroItemRichSoftDelete> claimItemsForRequest(long idRichSoftDelete,
            String tiItemRichSoftDelete, int maxItems) throws AppGenericPersistenceException {
        StringBuilder queryStr = new StringBuilder(
                "SELECT itemRichSoftDelete FROM AroItemRichSoftDelete itemRichSoftDelete ");

        if (TiItemRichSoftDelete.UNI_DOC.name().equals(tiItemRichSoftDelete)) {
            queryStr.append("JOIN FETCH itemRichSoftDelete.aroUnitaDoc unitaDoc ");
        }

        queryStr.append("JOIN itemRichSoftDelete.aroRichSoftDelete richSoftDelete "
                + "WHERE richSoftDelete.idRichSoftDelete = :idRichSoftDelete "
                + "AND itemRichSoftDelete.tiItemRichSoftDelete = :tiItemRichSoftDelete "
                + "AND itemRichSoftDelete.tiStatoItem = :statoItem "
                + "AND (itemRichSoftDelete.cdInstanceId IS NULL OR itemRichSoftDelete.dtClaim < :timeoutThreshold)");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(claimTimeoutMinutes);

        List<AroItemRichSoftDelete> items = entityManager
                .createQuery(queryStr.toString(), AroItemRichSoftDelete.class)
                .setParameter(ID_RICH_SOFT_DELETE, idRichSoftDelete)
                .setParameter(TI_ITEM_RICH_SOFT_DELETE, tiItemRichSoftDelete)
                .setParameter("statoItem", StatoItemRichSoftDelete.DA_ELABORARE.name())
                .setParameter("timeoutThreshold", timeoutThreshold).setMaxResults(maxItems)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("jakarta.persistence.lock.timeout", SKIP_LOCKED).getResultList();

        // Aggiorna le informazioni di claim
        for (AroItemRichSoftDelete item : items) {
            item.setCdInstanceId(instanceId);
            item.setDtClaim(LocalDateTime.now());
        }

        return items;
    }

    /**
     * Reclama un batch di item da elaborare usando SELECT FOR UPDATE SKIP LOCKED
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public List<Long> claimBatch() throws AppGenericPersistenceException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = now.minusMinutes(claimTimeoutMinutes);

        // Query che usa SELECT FOR UPDATE SKIP LOCKED per evitare contese
        List<AroItemRichSoftDelete> items = entityManager
                .createQuery(
                        "SELECT i FROM AroItemRichSoftDelete i "
                                + "WHERE i.tiStatoItem = :tiStatoItem "
                                + "AND i.tiItemRichSoftDelete = :tiItemRichSoftDelete "
                                + "AND (i.cdInstanceId IS NULL OR i.dtClaim < :timeoutThreshold) ",
                        AroItemRichSoftDelete.class)
                .setParameter("tiStatoItem", StatoItemRichSoftDelete.DA_ELABORARE.name())
                .setParameter(TI_ITEM_RICH_SOFT_DELETE, TiItemRichSoftDelete.UNI_DOC.name())
                .setParameter("timeoutThreshold", timeoutThreshold).setMaxResults(batchSize)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("jakarta.persistence.lock.timeout", SKIP_LOCKED).getResultList();

        // Aggiorna le informazioni di claim per ogni item ottenuto
        List<Long> itemIds = new ArrayList<>(items.size());

        if (!items.isEmpty()) {
            // Estrai gli ID per il risultato
            for (AroItemRichSoftDelete item : items) {
                item.setDtClaim(now);
                item.setCdInstanceId(instanceId);
                itemIds.add(item.getIdItemRichSoftDelete());
            }
        }

        return itemIds;
    }

    @Override
    public void updateStatoItemList(Long idRichSoftDelete, String tiStatoItem)
            throws AppGenericPersistenceException {
        Query q = entityManager.createQuery(
                "UPDATE AroItemRichSoftDelete itemRichSoftDelete SET itemRichSoftDelete.tiStatoItem = :tiStatoItem "
                        + "WHERE itemRichSoftDelete.aroRichSoftDelete.idRichSoftDelete = :idRichSoftDelete AND itemRichSoftDelete.tiStatoItem = 'DA_ELABORARE'");
        q.setParameter(ID_RICH_SOFT_DELETE, idRichSoftDelete);
        q.setParameter("tiStatoItem", tiStatoItem);
        q.executeUpdate();
    }

    /**
     * Restituisce la lista degli item della richiesta di cancellazione logica fornita in input
     *
     * @param idRichSoftDelete     id richiesta di cancellazione logica
     *
     * @param tiItemRichSoftDelete tipo di item da cercare (es. UNI_DOC, ANNULL_VERS, REST_ARCH,
     *                             SCARTO_ARCH)
     *
     * @return lista oggetti di tipo {@link AroVLisItemRichSoftDelete}
     */
    @SuppressWarnings("unchecked")
    public List<AroVLisItemRichSoftDelete> getAroVLisItemRichSoftDelete(BigDecimal idRichSoftDelete,
            String tiItemRichSoftDelete) throws AppGenericPersistenceException {
        String queryStr = "SELECT itemRichSoftDelete FROM AroVLisItemRichSoftDelete itemRichSoftDelete "
                + "WHERE itemRichSoftDelete.idRichSoftDelete = :idRichSoftDelete AND itemRichSoftDelete.tiItemRichSoftDelete = :tiItemRichSoftDelete";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(ID_RICH_SOFT_DELETE, idRichSoftDelete);
        query.setParameter(TI_ITEM_RICH_SOFT_DELETE, tiItemRichSoftDelete);
        return query.getResultList();
    }

    /**
     * Trova le richieste che hanno tutti gli item elaborati e sono pronte per la finalizzazione
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public List<AroRichSoftDelete> findRequestsToFinalize() throws AppGenericPersistenceException {
        // Query monolitica JPQL che trova tutte le richieste ACQUISITA con tutti gli item
        // completati
        String jpql = """
                SELECT r FROM AroRichSoftDelete r
                WHERE r.idStatoRichSoftDeleteCor IN (
                    SELECT s.idStatoRichSoftDelete FROM AroStatoRichSoftDelete s
                    WHERE s.tiStatoRichSoftDelete = :statoAcquisita
                )
                AND NOT EXISTS (
                    SELECT i.idItemRichSoftDelete FROM AroItemRichSoftDelete i
                    WHERE i.aroRichSoftDelete = r
                    AND i.tiStatoItem = :stato
                    AND i.tiItemRichSoftDelete = :tiItemRichSoftDelete
                )
                ORDER BY r.idRichSoftDelete
                """;

        return entityManager.createQuery(jpql, AroRichSoftDelete.class)
                .setParameter("statoAcquisita", CostantiDB.StatoRichSoftDelete.ACQUISITA.name())
                .setParameter("stato", StatoItemRichSoftDelete.DA_ELABORARE.name())
                .setParameter(TI_ITEM_RICH_SOFT_DELETE, TiItemRichSoftDelete.UNI_DOC.name())
                .setMaxResults(5).getResultList();
    }

    /**
     * Reclama un batch di unità documentarie, aggiornandole tutte insieme con un'unica query.
     *
     * @return Lista di ID delle unità documentarie che sono state reclamate
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public List<Long> claimUdBatchToVerify(int batchSize) {
        // Calcola la soglia di timeout per recuperare UD rimaste in VERIFICA_IN_CORSO
        LocalDateTime timeoutThreshold = LocalDateTime.now(APPLICATION_TIMEZONE)
                .minusMinutes(kafkaVerifyTimeoutMinutes);

        // Query nativa che usa ROWNUM (tramite setMaxResults) invece di FETCH FIRST,
        // compatibile
        // con FOR UPDATE
        String sql = """
                SELECT dm.id_unita_doc
                FROM dm_ud_del dm
                JOIN dm_ud_del_richieste rich ON rich.id_ud_del_richiesta = dm.id_ud_del_richiesta
                JOIN aro_item_rich_soft_delete item ON item.id_unita_doc = dm.id_unita_doc
                WHERE (
                    (rich.ti_stato_richiesta = 'DA_EVADERE'
                    AND rich.ti_stato_interno_rich = 'IN_ELABORAZIONE_LOGICA'
                    AND dm.ti_stato_ud_cancellate = 'DA_CANCELLARE'
                    AND item.ti_stato_item = 'ELABORATO')
                    OR
                    (dm.ti_stato_ud_cancellate = 'VERIFICA_IN_CORSO'
                    AND dm.dt_stato_ud_cancellate < ?)
                )
                FOR UPDATE SKIP LOCKED
                """;

        Query query = entityManager.createNativeQuery(sql).setParameter(1, timeoutThreshold)
                .setMaxResults(batchSize);

        List<BigDecimal> result = query.getResultList();

        // Se non ci sono ID reclamati, ritorna subito
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        // Converti BigDecimal a Long
        List<Long> claimedIds = result.stream().map(BigDecimal::longValue).toList();

        // Aggiorna immediatamente TUTTE le UD reclamate a VERIFICA_IN_CORSO con un unico
        // UPDATE
        String updateJpql = """
                UPDATE DmUdDel dm
                SET dm.tiStatoUdCancellate = 'VERIFICA_IN_CORSO',
                    dm.dtStatoUdCancellate = :now
                WHERE dm.idUnitaDoc IN :idList
                """;

        int updatedCount = entityManager.createQuery(updateJpql)
                .setParameter("now", LocalDateTime.now(APPLICATION_TIMEZONE))
                .setParameter("idList", claimedIds).executeUpdate();

        log.debug("Aggiornate {} unità documentarie a VERIFICA_IN_CORSO", updatedCount);

        // Ritorna gli ID reclamati
        return claimedIds;
    }

    /**
     * Ottiene i conteggi effettivi (actual) per un batch di unità documentarie.
     *
     * @param udIds Lista di ID delle unità documentarie da verificare
     *
     * @return Mappa che associa ciascun ID di unità documentaria con il conteggio effettivo
     */
    @Override
    public Map<Long, Long> getActualRecordCountsForBatch(List<Long> udIds) {
        if (udIds == null || udIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Inizializza la mappa dei risultati
        Map<Long, Long> actualCountMap = udIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        // Ottieni i conteggi effettivi con una query GROUP BY
        String jpql = """
                SELECT rec.dmUdDel.idUnitaDoc, COUNT(rec)
                FROM DmUdDelRecRefTab rec
                WHERE rec.dmUdDel.idUnitaDoc IN :idsList
                GROUP BY rec.dmUdDel.idUnitaDoc
                """;

        Query query = entityManager.createQuery(jpql).setParameter("idsList", udIds);

        @SuppressWarnings("unchecked")
        List<Object[]> actualCounts = query.getResultList();

        // Aggiorna la mappa con i conteggi effettivi
        for (Object[] row : actualCounts) {
            Long id = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            actualCountMap.put(id, count);
        }

        return actualCountMap;
    }

    /**
     * Ottiene i conteggi attesi (expected) per un batch di unità documentarie.
     *
     * @param udIds Lista di ID delle unità documentarie da verificare
     *
     * @return Mappa che associa ciascun ID di unità documentaria con il conteggio atteso
     */
    @Override
    public Map<Long, Long> getExpectedRecordCountsForBatch(List<Long> udIds) {
        if (udIds == null || udIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Inizializza la mappa dei risultati con tutti gli ID mappati a 0L
        Map<Long, Long> expectedCountMap = udIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        try {
            // Converti gli ID in BigDecimal per compatibilità con la query
            List<BigDecimal> idsBigDecimal = udIds.stream().map(BigDecimal::new).toList();

            // Query JPQL con GROUP BY per ottenere i conteggi per tutti gli ID in
            // un'unica
            // operazione
            String jpql = """
                    SELECT log.idUnitaDocRef, SUM(log.updatedRowCount)
                    FROM AroLogRichSoftDelete log
                    WHERE log.idUnitaDocRef IN :idsList
                    GROUP BY log.idUnitaDocRef
                    """;

            Query query = entityManager.createQuery(jpql).setParameter("idsList", idsBigDecimal);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // Aggiorna la mappa con i risultati della query
            for (Object[] row : results) {
                BigDecimal idBigDecimal = (BigDecimal) row[0];
                Number count = (Number) row[1];
                Long id = idBigDecimal.longValue();
                Long total = (count != null) ? count.longValue() : 0L;
                expectedCountMap.put(id, total);
            }

            return expectedCountMap;
        } catch (Exception e) {
            log.error("Errore durante il recupero dei conteggi attesi: {}",
                    ExceptionUtils.getRootCauseMessage(e), e);
            return expectedCountMap; // Ritorna la mappa con tutti zeri in caso di
            // errore
        }
    }

    /**
     * Aggiorna un batch di unità documentarie a CANCELLABILE
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void updateUnitaDocToCancellabileBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String jpql = """
                    UPDATE DmUdDel dm
                    SET dm.tiStatoUdCancellate = 'CANCELLABILE',
                        dm.dtStatoUdCancellate = :now
                    WHERE dm.idUnitaDoc IN :ids
                    AND dm.tiStatoUdCancellate = 'VERIFICA_IN_CORSO'
                """;

        int updated = entityManager.createQuery(jpql)
                .setParameter("now", LocalDateTime.now(APPLICATION_TIMEZONE))
                .setParameter("ids", ids).executeUpdate();

        log.info("Aggiornate {} unità documentarie a CANCELLABILE in batch", updated);
    }

    /**
     * Ripristina un batch di unità documentarie a DA_CANCELLARE
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void resetUnitaDocToDaCancellareBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String jpql = """
                    UPDATE DmUdDel dm
                    SET dm.tiStatoUdCancellate = 'DA_CANCELLARE',
                        dm.dtStatoUdCancellate = :now
                    WHERE dm.idUnitaDoc IN :ids
                    AND dm.tiStatoUdCancellate = 'VERIFICA_IN_CORSO'
                """;

        int updated = entityManager.createQuery(jpql)
                .setParameter("now", LocalDateTime.now(APPLICATION_TIMEZONE))
                .setParameter("ids", ids).executeUpdate();

        log.debug("Ripristinate {} unità documentarie a DA_CANCELLARE in batch", updated);
    }

    /**
     * Esegue la cancellazione logica in modalità bottom-up con supporto per due modalità: -
     * Modalità CAMPIONE: recupera e cancella solo un'entità rappresentante per ogni relazione
     * parent-child - Modalità COMPLETA: recupera tutte le entità e le aggiorna in gruppi basati sul
     * parent
     *
     * @param parentClass           la classe dell'entità principale
     * @param parentId              l'id dell'entità principale
     * @param mode                  modalità di cancellazione (CAMPIONE o MASSIVA)
     * @param aroItemRichSoftDelete l'item di cancellazione logica da elaborare
     *
     * @throws AppGenericPersistenceException in caso di errori durante l'elaborazione della
     *                                        cancellazione logica
     */
    @Override
    public void softDeleteBottomUp(Class<?> parentClass, Object parentId, SoftDeleteMode mode,
            AroItemRichSoftDelete aroItemRichSoftDelete) throws AppGenericPersistenceException {
        long startTime = System.currentTimeMillis();
        log.info("Inizio cancellazione logica per {} con id {}", parentClass.getSimpleName(),
                parentId);

        try {
            // Imposta l'ID root nel contesto
            rootEntityContext.setCurrentRootId(parentId);

            // Costruisci la gerarchia utilizzando EntityHierarchyBuilder
            Map<Integer, IStreamSource> streamSources = entityHierarchyBuilder
                    .buildHierarchy(parentClass, parentId);

            // Procedi dal livello più profondo verso l'alto per le relazioni normali
            List<Integer> sortedLevels = new ArrayList<>(streamSources.keySet());
            Collections.sort(sortedLevels, Collections.reverseOrder());

            // Elabora i livelli in ordine inverso utilizzando LevelProcessor
            for (Integer level : sortedLevels) {
                IStreamSource source = streamSources.get(level);
                levelProcessor.processLevel(source, mode, aroItemRichSoftDelete);
            }

            long endTime = System.currentTimeMillis();
            log.info("Cancellazione logica completata in {} ms", (endTime - startTime));
        } catch (Exception e) {
            throw new AppGenericPersistenceException(e, "Errore durante la cancellazione logica "
                    + ExceptionUtils.getRootCauseMessage(e));
        } finally {
            // Pulisci il contesto alla fine
            rootEntityContext.clear();
        }
    }

    /**
     * Implementazione retrocompatibile che usa la modalità completa
     */
    @Override
    public void softDeleteBottomUp(Class<?> parentClass, Object parentId,
            AroItemRichSoftDelete aroItemRichSoftDelete) throws AppGenericPersistenceException {
        softDeleteBottomUp(parentClass, parentId, SoftDeleteMode.COMPLETA, aroItemRichSoftDelete);
    }

}
