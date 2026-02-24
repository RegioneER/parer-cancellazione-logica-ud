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

package it.eng.parer.soft.delete.beans.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.ICancellazioneLogicaDao;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaParserService;
import it.eng.parer.soft.delete.beans.IControlliWsService;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteService;
import it.eng.parer.soft.delete.beans.XmlSoftDeleteCache;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.WSDescCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.security.User;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.beans.utils.CostantiDB;
import it.eng.parer.soft.delete.jpa.entity.*;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.TipoCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
class CancellazioneLogicaServiceTest {

    @Mock
    private IControlliWsService controlliWsService;

    @Mock
    private ICancellazioneLogicaParserService cancellazioneLogicaParserService;

    @Mock
    private IRegistrazioneRichiesteService registrazioneRichiesteService;

    @Mock
    private ICancellazioneLogicaDao cancellazioneLogicaDao;

    @Mock
    private IRegistrazioneRichiesteDao registrazioneRichiesteDao;

    @Mock
    private EntityManager entityManager;

    @Mock
    private XmlSoftDeleteCache xmlSoftDeleteCache;

    @Mock
    private ManagedExecutor executor;

    @InjectMocks
    private CancellazioneLogicaService service;

    private RispostaWSInvioRichiestaCancellazioneLogica rispostaWs;
    private InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt;
    private User mockUser;
    private AroRichSoftDelete mockRich;

    @BeforeEach
    void setup() {
        try {
            var field = CancellazioneLogicaService.class.getDeclaredField("instanceId");
            field.setAccessible(true);
            field.set(service, "test-instance-id");

            var batchSizeField = CancellazioneLogicaService.class.getDeclaredField("batchSize");
            batchSizeField.setAccessible(true);
            batchSizeField.set(service, 5);

            var pollingField = CancellazioneLogicaService.class.getDeclaredField("pollingEnabled");
            pollingField.setAccessible(true);
            pollingField.set(service, true);

            var kafkaField = CancellazioneLogicaService.class
                    .getDeclaredField("kafkaVerifyBatchSize");
            kafkaField.setAccessible(true);
            kafkaField.set(service, 50);
        } catch (Exception e) {
            fail("Errore nell'inizializzazione: " + e.getMessage());
        }

        rispostaWs = new RispostaWSInvioRichiestaCancellazioneLogica();
        cancellazioneLogicaExt = new InvioRichiestaCancellazioneLogicaExt();

        mockUser = new User();
        mockUser.setIdUtente(10L);
        mockUser.setUsername("testuser");

        mockRich = new AroRichSoftDelete();
        mockRich.setIdRichSoftDelete(100L);
        mockRich.setTiRichSoftDelete(CostantiDB.TiRichSoftDelete.UD.name());
        mockRich.setTiModCancellazione("CAMPIONE");
        mockRich.setAroItemRichSoftDelete(new ArrayList<>());
        mockRich.setAroStatoRichSoftDelete(new ArrayList<>());

        // Inizializza sempre l'esito
        EsitoRichiestaCancellazioneLogica esito = new EsitoRichiestaCancellazioneLogica();
        esito.setEsitoRichiesta(new EsitoRichiestaType());
        rispostaWs.setEsitoRichiestaCancellazioneLogica(esito);
    }

    // ==================== init ====================

    @Test
    void init_success() {
        Map<String, String> wsVersions = Map.of("1.0", "1.0");
        RispostaControlli rcLoadWsVers = new RispostaControlli();
        rcLoadWsVers.setrBoolean(true);
        rcLoadWsVers.setrObject(wsVersions);

        when(controlliWsService.loadWsVersions(any())).thenReturn(rcLoadWsVers);

        AvanzamentoWs avanzamento = service.init(rispostaWs, cancellazioneLogicaExt);

        assertNotNull(avanzamento);
        assertNotNull(cancellazioneLogicaExt.getDescrizione());
        assertEquals(IRispostaWS.SeverityEnum.OK, rispostaWs.getSeverity());
    }

    @Test
    void init_loadVersionsError() {
        RispostaControlli rcLoadWsVers = new RispostaControlli();
        rcLoadWsVers.setrBoolean(false);
        rcLoadWsVers.setCodErr("ERR-001");
        rcLoadWsVers.setDsErr("Errore caricamento versioni");

        when(controlliWsService.loadWsVersions(any())).thenReturn(rcLoadWsVers);

        service.init(rispostaWs, cancellazioneLogicaExt);

        assertEquals(IRispostaWS.SeverityEnum.ERROR, rispostaWs.getSeverity());
    }

    // ==================== verificaVersione ====================

    @Test
    void verificaVersione_success() {
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());
        cancellazioneLogicaExt.setWsVersions(Map.of("1.0", "1.0"));

        RispostaControlli rcCheckVers = new RispostaControlli();
        rcCheckVers.setrBoolean(true);

        when(controlliWsService.checkVersione(anyString(), anyString(), any()))
                .thenReturn(rcCheckVers);

        service.verificaVersione("1.0", rispostaWs, cancellazioneLogicaExt);

        assertEquals("1.0", cancellazioneLogicaExt.getVersioneWsChiamata());
    }

    @Test
    void verificaVersione_versioneNonSupportata() {
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());
        cancellazioneLogicaExt.setWsVersions(Map.of("1.0", "1.0"));

        RispostaControlli rcCheckVers = new RispostaControlli();
        rcCheckVers.setrBoolean(false);
        rcCheckVers.setCodErr("VER-001");
        rcCheckVers.setDsErr("Versione non supportata");

        when(controlliWsService.checkVersione(anyString(), anyString(), any()))
                .thenReturn(rcCheckVers);

        service.verificaVersione("2.0", rispostaWs, cancellazioneLogicaExt);

        assertEquals(IRispostaWS.SeverityEnum.ERROR, rispostaWs.getSeverity());
    }

    // ==================== verificaCredenziali ====================

    @Test
    void verificaCredenziali_success() {
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());

        RispostaControlli rcCheckCred = new RispostaControlli();
        rcCheckCred.setrBoolean(true);
        rcCheckCred.setrObject(mockUser);

        RispostaControlli rcCheckAuth = new RispostaControlli();
        rcCheckAuth.setrBoolean(true);

        when(controlliWsService.checkCredenziali(anyString(), anyString(), anyString()))
                .thenReturn(rcCheckCred);
        when(controlliWsService.checkAuthWSNoOrg(any(), any())).thenReturn(rcCheckAuth);

        service.verificaCredenziali("user", "pass", "127.0.0.1", rispostaWs,
                cancellazioneLogicaExt);

        assertEquals("user", cancellazioneLogicaExt.getLoginName());
        assertNotNull(cancellazioneLogicaExt.getUtente());
    }

    @Test
    void verificaCredenziali_invalidCredentials() {
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());

        RispostaControlli rcCheckCred = new RispostaControlli();
        rcCheckCred.setrBoolean(false);
        rcCheckCred.setCodErr("AUTH-001");
        rcCheckCred.setDsErr("Credenziali non valide");

        when(controlliWsService.checkCredenziali(anyString(), anyString(), anyString()))
                .thenReturn(rcCheckCred);

        service.verificaCredenziali("wronguser", "wrongpass", "127.0.0.1", rispostaWs,
                cancellazioneLogicaExt);

        assertEquals(IRispostaWS.SeverityEnum.ERROR, rispostaWs.getSeverity());
    }

    // ==================== parseXML ====================

    @Test
    void parseXML_success() {
        cancellazioneLogicaExt.setUtente(mockUser);

        doNothing().when(cancellazioneLogicaParserService).parseXML(anyString(), any(), any());

        service.parseXML("<xml/>", rispostaWs, cancellazioneLogicaExt);

        verify(cancellazioneLogicaParserService).parseXML("<xml/>", cancellazioneLogicaExt,
                rispostaWs);
    }

    // ==================== registraRichieste ====================

    @Test
    void registraRichieste_success() throws Exception {
        cancellazioneLogicaExt.setUtente(mockUser);
        cancellazioneLogicaExt.setIdStrut(1L);

        RichiestaCancellazioneLogica richCancLog = new RichiestaCancellazioneLogica();
        RichiestaType richiesta = new RichiestaType();
        richiesta.setDescrizione("Test");
        richiesta.setMotivazione("Motivazione test");
        richiesta.setTipoCancellazione(TipoCancellazioneType.CAMPIONE);
        richCancLog.setRichiesta(richiesta);

        RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCanc = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        RichiestaDiCancellazioneType richDiCanc = new RichiestaDiCancellazioneType();
        richDiCanc.setTipoRichiesta(TipoRichiestaType.UNITA_DOCUMENTARIA);
        richiesteDiCanc.getRichiestaDiCancellazione().add(richDiCanc);
        richCancLog.setRichiesteDiCancellazione(richiesteDiCanc);

        cancellazioneLogicaExt.setRichiestaCancellazioneLogica(richCancLog);
        cancellazioneLogicaExt.setXmlRichiesta("<xml/>");
        cancellazioneLogicaExt.setDataElaborazione(new Date());

        when(registrazioneRichiesteService.insertRichSoftDelete(anyLong(), anyLong(), anyString(),
                anyString(), anyString(), any(), anyString())).thenReturn(mockRich);

        AroXmlRichSoftDelete xmlRich = new AroXmlRichSoftDelete();
        when(registrazioneRichiesteDao.createAroXmlRichSoftDelete(any(), anyString(), anyString(),
                any())).thenReturn(xmlRich);

        AroStatoRichSoftDelete stato = new AroStatoRichSoftDelete();
        stato.setIdStatoRichSoftDelete(1L);
        when(registrazioneRichiesteService.insertAroStatoRichSoftDelete(any(), anyString(), any(),
                anyLong())).thenReturn(stato);

        Long idRichiesta = service.registraRichieste(rispostaWs, cancellazioneLogicaExt);

        assertNotNull(idRichiesta);
        assertEquals(100L, idRichiesta);
    }

    // ==================== avviaElaborazioneAsincrona ====================

    @Test
    void avviaElaborazioneAsincrona_success() throws Exception {
        AroStatoRichSoftDelete stato = new AroStatoRichSoftDelete();
        stato.setTiStatoRichSoftDelete(CostantiDB.StatoRichSoftDelete.PRESA_IN_CARICO.name());

        lenient().when(registrazioneRichiesteService.getStatoCorrenteRichiesta(anyLong()))
                .thenReturn(stato);
        when(executor.runAsync(any(Runnable.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        RichiestaCancellazioneLogica richCancLog = new RichiestaCancellazioneLogica();
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCanc = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        richCancLog.setRichiesteDiCancellazione(richiesteDiCanc);
        cancellazioneLogicaExt.setRichiestaCancellazioneLogica(richCancLog);

        CompletionStage<Void> result = service.avviaElaborazioneAsincrona(100L,
                cancellazioneLogicaExt, 10L);

        assertNotNull(result);
    }

    // ==================== createItemsInNewTransaction ====================

    @Test
    void createItemsInNewTransaction_success() throws Exception {
        when(entityManager.find(AroRichSoftDelete.class, 100L,
                LockModeType.PESSIMISTIC_WRITE)).thenReturn(mockRich);

        TypedQuery<AroStatoRichSoftDelete> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(AroStatoRichSoftDelete.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());

        AroStatoRichSoftDelete stato = new AroStatoRichSoftDelete();
        stato.setIdStatoRichSoftDelete(1L);
        when(registrazioneRichiesteService.insertAroStatoRichSoftDelete(any(), anyString(), any(),
                anyLong())).thenReturn(stato);

        RichiestaCancellazioneLogica richCancLog = new RichiestaCancellazioneLogica();
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCanc = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        richCancLog.setRichiesteDiCancellazione(richiesteDiCanc);
        cancellazioneLogicaExt.setRichiestaCancellazioneLogica(richCancLog);

        service.createItemsInNewTransaction(100L, cancellazioneLogicaExt, 10L);

        verify(registrazioneRichiesteService).createItems(eq(mockRich), any(), eq(10L));
    }

    // ==================== finalizeRequest ====================

    @Test
    void finalizeRequest_success() throws Exception {
        AroStatoRichSoftDelete currentStatus = new AroStatoRichSoftDelete();
        currentStatus.setIdStatoRichSoftDelete(1L);
        currentStatus.setTiStatoRichSoftDelete(CostantiDB.StatoRichSoftDelete.ACQUISITA.name());

        IamUser user = new IamUser();
        user.setIdUserIam(10L);
        currentStatus.setIamUser(user);

        mockRich.setIdStatoRichSoftDeleteCor(BigDecimal.valueOf(1L));

        when(entityManager.find(AroRichSoftDelete.class, 100L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(mockRich);
        // Supporta sia Integer che Long per l'ID
        lenient().when(entityManager.find(eq(AroStatoRichSoftDelete.class), any()))
                .thenReturn(currentStatus);

        AroStatoRichSoftDelete newStatus = new AroStatoRichSoftDelete();
        newStatus.setIdStatoRichSoftDelete(2L);
        when(registrazioneRichiesteService.insertAroStatoRichSoftDelete(any(), anyString(), any(),
                anyLong())).thenReturn(newStatus);

        service.finalizeRequest(mockRich);

        verify(cancellazioneLogicaDao).updateStatoItemList(eq(100L), anyString());
    }

    @Test
    void finalizeRequest_alreadyFinalized() throws Exception {
        AroStatoRichSoftDelete currentStatus = new AroStatoRichSoftDelete();
        currentStatus.setIdStatoRichSoftDelete(1L);
        currentStatus.setTiStatoRichSoftDelete(CostantiDB.StatoRichSoftDelete.EVASA.name());

        mockRich.setIdStatoRichSoftDeleteCor(BigDecimal.valueOf(1L));

        when(entityManager.find(AroRichSoftDelete.class, 100L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(mockRich);
        // Supporta sia Integer che Long per l'ID
        lenient().when(entityManager.find(eq(AroStatoRichSoftDelete.class), any()))
                .thenReturn(currentStatus);

        service.finalizeRequest(mockRich);

        verify(cancellazioneLogicaDao, never()).updateStatoItemList(anyLong(), anyString());
    }

    // ==================== pollAndProcessItems ====================

    @Test
    void pollAndProcessItems_success() throws Exception {
        when(cancellazioneLogicaDao.claimBatch()).thenReturn(List.of(1L, 2L));

        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(1L);
        item.setCdInstanceId("test-instance-id");
        item.setAroRichSoftDelete(mockRich);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(10L);
        item.setAroUnitaDoc(ud);

        when(entityManager.find(AroItemRichSoftDelete.class, 1L)).thenReturn(item);
        when(entityManager.find(AroItemRichSoftDelete.class, 2L)).thenReturn(item);

        service.pollAndProcessItems();

        verify(cancellazioneLogicaDao, atLeastOnce()).claimBatch();
    }

    @Test
    void pollAndProcessItems_nessunItemDisponibile() throws Exception {
        when(cancellazioneLogicaDao.claimBatch()).thenReturn(Collections.emptyList());

        service.pollAndProcessItems();

        verify(cancellazioneLogicaDao).claimBatch();
    }

    // ==================== finalizeCompletedRequests ====================

    @Test
    void finalizeCompletedRequests_success() throws Exception {
        when(cancellazioneLogicaDao.findRequestsToFinalize()).thenReturn(List.of(mockRich));

        AroStatoRichSoftDelete currentStatus = new AroStatoRichSoftDelete();
        currentStatus.setIdStatoRichSoftDelete(1L);
        currentStatus.setTiStatoRichSoftDelete(CostantiDB.StatoRichSoftDelete.ACQUISITA.name());

        IamUser user = new IamUser();
        user.setIdUserIam(10L);
        currentStatus.setIamUser(user);

        mockRich.setIdStatoRichSoftDeleteCor(BigDecimal.valueOf(1L));

        when(entityManager.find(AroRichSoftDelete.class, 100L,
                LockModeType.PESSIMISTIC_WRITE)).thenReturn(mockRich);
        // Supporta sia Integer che Long per l'ID
        lenient().when(entityManager.find(eq(AroStatoRichSoftDelete.class), any()))
                .thenReturn(currentStatus);

        AroStatoRichSoftDelete newStatus = new AroStatoRichSoftDelete();
        newStatus.setIdStatoRichSoftDelete(2L);
        when(registrazioneRichiesteService.insertAroStatoRichSoftDelete(any(), anyString(), any(),
                anyLong())).thenReturn(newStatus);

        service.finalizeCompletedRequests();

        verify(cancellazioneLogicaDao).findRequestsToFinalize();
    }

    // ==================== verifyKafkaRecordsLoaded ====================

    @Test
    void verifyKafkaRecordsLoaded_success() throws Exception {
        when(cancellazioneLogicaDao.claimUdBatchToVerify(anyInt())).thenReturn(List.of(1L, 2L));

        Map<Long, Long> actualCounts = Map.of(1L, 10L, 2L, 5L);
        Map<Long, Long> expectedCounts = Map.of(1L, 10L, 2L, 5L);

        when(cancellazioneLogicaDao.getActualRecordCountsForBatch(any()))
                .thenReturn(actualCounts);
        when(cancellazioneLogicaDao.getExpectedRecordCountsForBatch(any()))
                .thenReturn(expectedCounts);

        service.verifyKafkaRecordsLoaded();

        verify(cancellazioneLogicaDao).updateUnitaDocToCancellabileBatch(List.of(1L, 2L));
    }

    @Test
    void verifyKafkaRecordsLoaded_conteggioNonCoincide() throws Exception {
        when(cancellazioneLogicaDao.claimUdBatchToVerify(anyInt())).thenReturn(List.of(1L, 2L));

        Map<Long, Long> actualCounts = Map.of(1L, 8L, 2L, 5L);
        Map<Long, Long> expectedCounts = Map.of(1L, 10L, 2L, 5L);

        when(cancellazioneLogicaDao.getActualRecordCountsForBatch(any()))
                .thenReturn(actualCounts);
        when(cancellazioneLogicaDao.getExpectedRecordCountsForBatch(any()))
                .thenReturn(expectedCounts);

        service.verifyKafkaRecordsLoaded();

        verify(cancellazioneLogicaDao).updateUnitaDocToCancellabileBatch(List.of(2L));
    }

    // ==================== processItemById ====================

    @Test
    void processItemById_success() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(1L);
        item.setCdInstanceId("test-instance-id");
        item.setAroRichSoftDelete(mockRich);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(10L);
        item.setAroUnitaDoc(ud);

        when(entityManager.find(AroItemRichSoftDelete.class, 1L)).thenReturn(item);

        service.processItemById(1L);

        verify(cancellazioneLogicaDao).softDeleteBottomUp(eq(AroUnitaDoc.class), eq(10L), any(),
                eq(item));
    }

    // ==================== markItemAsFailed ====================

    @Test
    void markItemAsFailed_success() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(1L);

        when(entityManager.find(AroItemRichSoftDelete.class, 1L)).thenReturn(item);

        service.markItemAsFailed(1L, "Errore di test");

        assertEquals(CostantiDB.StatoItemRichSoftDelete.ERRORE_ELABORAZIONE.name(),
                item.getTiStatoItem());
    }

    // ==================== softDeleteByItem ====================

    @Test
    void softDeleteByItem_campione() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(1L);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(10L);
        item.setAroUnitaDoc(ud);

        service.softDeleteByItem(item, SoftDeleteMode.CAMPIONE);

        verify(cancellazioneLogicaDao).softDeleteBottomUp(AroUnitaDoc.class, 10L,
                SoftDeleteMode.CAMPIONE, item);
    }

    @Test
    void softDeleteByItem_completa() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(1L);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(10L);
        item.setAroUnitaDoc(ud);

        service.softDeleteByItem(item, SoftDeleteMode.COMPLETA);

        verify(cancellazioneLogicaDao).softDeleteBottomUp(AroUnitaDoc.class, 10L,
                SoftDeleteMode.COMPLETA, item);
    }
}
