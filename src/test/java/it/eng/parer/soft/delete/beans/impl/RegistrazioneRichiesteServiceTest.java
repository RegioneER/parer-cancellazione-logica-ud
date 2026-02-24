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
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.CostantiDB;
import it.eng.parer.soft.delete.jpa.entity.AroErrRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichAnnulVers;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichiestaRa;
import it.eng.parer.soft.delete.jpa.entity.AroStatoRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.DmUdDel;
import it.eng.parer.soft.delete.jpa.entity.IamUser;
import it.eng.parer.soft.delete.jpa.entity.OrgStrut;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
class RegistrazioneRichiesteServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private IRegistrazioneRichiesteDao registrazioneRichiesteDao;

    @InjectMocks
    private RegistrazioneRichiesteService service;

    private OrgStrut mockStrut;
    private AroRichSoftDelete mockRich;
    private IamUser mockUser;
    private AtomicLong itemIdCounter;
    private AtomicLong statoIdCounter;

    @BeforeEach
    void setup() {
        itemIdCounter = new AtomicLong(1L);
        statoIdCounter = new AtomicLong(1L);

        mockStrut = new OrgStrut();
        mockStrut.setIdStrut(1L);

        mockRich = new AroRichSoftDelete();
        mockRich.setIdRichSoftDelete(100L);
        mockRich.setOrgStrut(mockStrut);
        mockRich.setAroItemRichSoftDelete(new ArrayList<>());
        mockRich.setAroStatoRichSoftDelete(new ArrayList<>());

        mockUser = new IamUser();
        mockUser.setIdUserIam(10L);

        // Mock persist con lenient() per evitare UnnecessaryStubbingException
        lenient().doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg instanceof AroItemRichSoftDelete) {
                AroItemRichSoftDelete item = (AroItemRichSoftDelete) arg;
                item.setIdItemRichSoftDelete(itemIdCounter.getAndIncrement());
            } else if (arg instanceof AroStatoRichSoftDelete) {
                AroStatoRichSoftDelete stato = (AroStatoRichSoftDelete) arg;
                stato.setIdStatoRichSoftDelete(statoIdCounter.getAndIncrement());
            }
            return null;
        }).when(entityManager).persist(any());
    }

    // ==================== insertRichSoftDelete ====================

    @Test
    void insertRichSoftDelete_success() throws AppGenericPersistenceException {
        when(entityManager.find(OrgStrut.class, 1L)).thenReturn(mockStrut);

        AroRichSoftDelete result = service.insertRichSoftDelete(10L, 1L, "Descrizione", "Note",
                "IMMEDIATA", LocalDateTime.now(), "COMPLETA");

        assertNotNull(result);
        verify(entityManager).persist(any(AroRichSoftDelete.class));
        verify(entityManager).flush();
    }

    @Test
    void insertRichSoftDelete_throwsException() {
        when(entityManager.find(OrgStrut.class, 1L)).thenThrow(new RuntimeException("DB error"));

        assertThrows(AppGenericPersistenceException.class, () -> service.insertRichSoftDelete(10L,
                1L, "Desc", "Note", "IMMEDIATA", LocalDateTime.now(), "COMPLETA"));
    }

    // ==================== createItems - UNITA_DOCUMENTARIA ====================

    @Test
    void createItems_unitaDocumentaria_success() throws Exception {
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richieste = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        RichiestaDiCancellazioneType richiesta = new RichiestaDiCancellazioneType();
        richiesta.setTipoRichiesta(TipoRichiestaType.UNITA_DOCUMENTARIA);
        richiesta.setTipoRegistro("REG");
        richiesta.setAnno(2024);
        richiesta.setNumero("001");
        richieste.getRichiestaDiCancellazione().add(richiesta);

        when(registrazioneRichiesteDao.getIdUnitaDocVersataAnnul(any(), anyString(), any(),
                anyString())).thenReturn(1L);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(1L);
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name());
        when(entityManager.find(AroUnitaDoc.class, 1L)).thenReturn(ud);

        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        service.createItems(mockRich, richieste, 10L);

        verify(entityManager, atLeastOnce()).persist(any(AroItemRichSoftDelete.class));
    }

    @Test
    void createItems_unitaDocumentaria_duplicata() throws Exception {
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richieste = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();

        RichiestaDiCancellazioneType r1 = new RichiestaDiCancellazioneType();
        r1.setTipoRichiesta(TipoRichiestaType.UNITA_DOCUMENTARIA);
        r1.setTipoRegistro("REG");
        r1.setAnno(2024);
        r1.setNumero("001");

        RichiestaDiCancellazioneType r2 = new RichiestaDiCancellazioneType();
        r2.setTipoRichiesta(TipoRichiestaType.UNITA_DOCUMENTARIA);
        r2.setTipoRegistro("REG");
        r2.setAnno(2024);
        r2.setNumero("001");

        richieste.getRichiestaDiCancellazione().add(r1);
        richieste.getRichiestaDiCancellazione().add(r2);

        when(registrazioneRichiesteDao.getIdUnitaDocVersataAnnul(any(), anyString(), any(),
                anyString())).thenReturn(1L);

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(1L);
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name());
        when(entityManager.find(AroUnitaDoc.class, 1L)).thenReturn(ud);

        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        service.createItems(mockRich, richieste, 10L);

        verify(entityManager, times(2)).persist(any(AroItemRichSoftDelete.class));
        verify(entityManager, atLeastOnce()).persist(any(AroErrRichSoftDelete.class));
    }

    // ==================== elaboraItemConStream - ANNULLAMENTO_VERSAMENTO ====================

    @Test
    void elaboraItemConStream_annullamentoVersamento() throws Exception {
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richieste = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        RichiestaDiCancellazioneType richiesta = new RichiestaDiCancellazioneType();
        richiesta.setTipoRichiesta(TipoRichiestaType.ANNULLAMENTO_VERSAMENTO);
        richiesta.setIDRichiestaSacer(BigInteger.valueOf(500L));
        richieste.getRichiestaDiCancellazione().add(richiesta);

        when(registrazioneRichiesteDao.getIdRichAnnulVersEvasaDaCancel(any(), any()))
                .thenReturn(10L);

        AroRichAnnulVers richAnnul = new AroRichAnnulVers();
        richAnnul.setIdRichAnnulVers(10L);
        richAnnul.setCdRichAnnulVers("ANN-001");
        when(entityManager.find(AroRichAnnulVers.class, 10L)).thenReturn(richAnnul);

        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        DmUdDel udDel = new DmUdDel();
        udDel.setCdRegistroKeyUnitaDoc("REG");
        udDel.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        udDel.setCdKeyUnitaDoc("001");

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(1L);
        ud.setCdRegistroKeyUnitaDoc("REG");
        ud.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        ud.setCdKeyUnitaDoc("001");
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name());
        udDel.setAroUnitaDoc(ud);

        when(registrazioneRichiesteDao.streamUnitaDocDaRichiesta(anyLong(), any()))
                .thenReturn(Stream.of(udDel));
        when(registrazioneRichiesteDao.countUnitaDocDaRichiesta(anyLong(), any())).thenReturn(1L);

        when(entityManager.find(eq(AroRichSoftDelete.class), anyLong())).thenReturn(mockRich);
        when(entityManager.find(eq(AroItemRichSoftDelete.class), anyLong())).thenAnswer(inv -> {
            AroItemRichSoftDelete item = new AroItemRichSoftDelete();
            item.setIdItemRichSoftDelete(itemIdCounter.getAndIncrement());
            item.setAroRichSoftDelete(mockRich);
            item.setAroErrRichSoftDelete(new ArrayList<>());
            item.setTiStatoItem(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name());
            return item;
        });

        service.createItems(mockRich, richieste, 10L);

        verify(entityManager, atLeastOnce()).persist(any(AroItemRichSoftDelete.class));
    }

    // ==================== elaboraItemConStream - RESTITUZIONE_ARCHIVIO ====================

    @Test
    void elaboraItemConStream_restituzioneArchivio() throws Exception {
        RichiestaCancellazioneLogica.RichiesteDiCancellazione richieste = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();
        RichiestaDiCancellazioneType richiesta = new RichiestaDiCancellazioneType();
        richiesta.setTipoRichiesta(TipoRichiestaType.RESTITUZIONE_ARCHIVIO);
        richiesta.setIDRichiestaSacer(BigInteger.valueOf(600L));
        richieste.getRichiestaDiCancellazione().add(richiesta);

        when(registrazioneRichiesteDao.getIdRichRestArchRestituita(any(), any()))
                .thenReturn(BigDecimal.valueOf(20L));

        AroRichiestaRa richRa = new AroRichiestaRa();
        richRa.setIdRichiestaRa(20L);
        when(entityManager.find(AroRichiestaRa.class, 20L)).thenReturn(richRa);

        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        DmUdDel udDel = new DmUdDel();
        udDel.setCdRegistroKeyUnitaDoc("REG");
        udDel.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        udDel.setCdKeyUnitaDoc("002");

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(2L);
        ud.setCdRegistroKeyUnitaDoc("REG");
        ud.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        ud.setCdKeyUnitaDoc("002");
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name());
        udDel.setAroUnitaDoc(ud);

        when(registrazioneRichiesteDao.streamUnitaDocDaRichiesta(anyLong(), any()))
                .thenReturn(Stream.of(udDel));
        when(registrazioneRichiesteDao.countUnitaDocDaRichiesta(anyLong(), any())).thenReturn(1L);

        when(entityManager.find(eq(AroRichSoftDelete.class), anyLong())).thenReturn(mockRich);
        when(entityManager.find(eq(AroItemRichSoftDelete.class), anyLong())).thenAnswer(inv -> {
            AroItemRichSoftDelete item = new AroItemRichSoftDelete();
            item.setIdItemRichSoftDelete(itemIdCounter.getAndIncrement());
            item.setAroRichSoftDelete(mockRich);
            item.setAroErrRichSoftDelete(new ArrayList<>());
            item.setTiStatoItem(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name());
            return item;
        });

        service.createItems(mockRich, richieste, 10L);

        verify(entityManager, atLeastOnce()).persist(any(AroItemRichSoftDelete.class));
    }

    // ==================== processaBatchInNuovaTransazione ====================

    @Test
    void processaBatchInNuovaTransazione_success() throws Exception {
        DmUdDel udDel = new DmUdDel();
        udDel.setCdRegistroKeyUnitaDoc("REG");
        udDel.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        udDel.setCdKeyUnitaDoc("003");

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(3L);
        ud.setCdRegistroKeyUnitaDoc("REG");
        ud.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        ud.setCdKeyUnitaDoc("003");
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name());
        udDel.setAroUnitaDoc(ud);

        List<DmUdDel> batch = List.of(udDel);

        AroItemRichSoftDelete itemPadre = new AroItemRichSoftDelete();
        itemPadre.setIdItemRichSoftDelete(1L);
        itemPadre.setAroRichSoftDelete(mockRich);
        itemPadre.setAroErrRichSoftDelete(new ArrayList<>());

        when(entityManager.find(AroRichSoftDelete.class, 100L)).thenReturn(mockRich);
        when(entityManager.find(AroItemRichSoftDelete.class, 1L)).thenReturn(itemPadre);

        AtomicInteger progressivo = new AtomicInteger(1);

        service.processaBatchInNuovaTransazione(batch, 100L, 1L, progressivo, 10L, 1,
                TipoRichiestaType.ANNULLAMENTO_VERSAMENTO);

        verify(entityManager, atLeastOnce()).persist(any(AroItemRichSoftDelete.class));
        assertEquals(2, progressivo.get());
    }

    @Test
    void processaBatchInNuovaTransazione_itemPadreNull_throwsException() {
        when(entityManager.find(AroRichSoftDelete.class, 100L)).thenReturn(mockRich);
        when(entityManager.find(AroItemRichSoftDelete.class, 999L)).thenReturn(null);

        assertThrows(AppGenericPersistenceException.class,
                () -> service.processaBatchInNuovaTransazione(Collections.emptyList(), 100L, 999L,
                        new AtomicInteger(1), 10L, 1,
                        TipoRichiestaType.ANNULLAMENTO_VERSAMENTO));
    }

    // ==================== controlloItemDaElaborare - ANNULLAMENTO ====================

    @Test
    void controlloItemDaElaborare_annullamento_udAnnullata() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(10L);
        item.setAroRichSoftDelete(mockRich);
        item.setTiItemRichSoftDelete(CostantiDB.TiItemRichSoftDelete.UNI_DOC.name());
        item.setAroErrRichSoftDelete(new ArrayList<>());

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(1L);
        ud.setCdRegistroKeyUnitaDoc("REG");
        ud.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        ud.setCdKeyUnitaDoc("001");
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name());
        item.setAroUnitaDoc(ud);

        when(registrazioneRichiesteDao.getAroRichSoftDeleteContainingUd(anyLong(), anyLong()))
                .thenReturn(null);
        when(registrazioneRichiesteDao.isUdNonAnnullata(anyLong())).thenReturn(false);
        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        var method = RegistrazioneRichiesteService.class.getDeclaredMethod(
                "controlloItemDaElaborare", AroItemRichSoftDelete.class, Long.class,
                TipoRichiestaType.class);
        method.setAccessible(true);
        method.invoke(service, item, 10L, TipoRichiestaType.ANNULLAMENTO_VERSAMENTO);

        assertEquals(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name(), item.getTiStatoItem());
    }

    // ==================== controlloItemDaElaborare - RESTITUZIONE ====================

    @Test
    void controlloItemDaElaborare_restituzione_udRestituita() throws Exception {
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(11L);
        item.setAroRichSoftDelete(mockRich);
        item.setTiItemRichSoftDelete(CostantiDB.TiItemRichSoftDelete.UNI_DOC.name());
        item.setAroErrRichSoftDelete(new ArrayList<>());

        AroUnitaDoc ud = new AroUnitaDoc();
        ud.setIdUnitaDoc(2L);
        ud.setCdRegistroKeyUnitaDoc("REG");
        ud.setAaKeyUnitaDoc(BigDecimal.valueOf(2024));
        ud.setCdKeyUnitaDoc("002");
        ud.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name());
        item.setAroUnitaDoc(ud);

        when(registrazioneRichiesteDao.getAroRichSoftDeleteContainingUd(anyLong(), anyLong()))
                .thenReturn(null);
        when(registrazioneRichiesteDao.isUdNonRestituita(anyLong())).thenReturn(false);
        when(registrazioneRichiesteDao.getAroErrRichSoftDeleteByGravity(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        var method = RegistrazioneRichiesteService.class.getDeclaredMethod(
                "controlloItemDaElaborare", AroItemRichSoftDelete.class, Long.class,
                TipoRichiestaType.class);
        method.setAccessible(true);
        method.invoke(service, item, 10L, TipoRichiestaType.RESTITUZIONE_ARCHIVIO);

        assertEquals(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name(), item.getTiStatoItem());
    }

    // ==================== countItemsInRichSoftDelete ====================

    @Test
    void countItemsInRichSoftDelete_withStati() throws Exception {
        when(registrazioneRichiesteDao.countAroItemRichSoftDelete(any(), anyString(),
                any(String[].class))).thenReturn(5L);

        Long count = service.countItemsInRichSoftDelete(BigDecimal.valueOf(100L),
                CostantiDB.TiItemRichSoftDelete.UNI_DOC.name(),
                CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name());

        assertEquals(5L, count);
        verify(registrazioneRichiesteDao).countAroItemRichSoftDelete(BigDecimal.valueOf(100L),
                CostantiDB.TiItemRichSoftDelete.UNI_DOC.name(),
                CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name());
    }

    @Test
    void countItemsInRichSoftDelete_withoutStati() throws Exception {
        when(registrazioneRichiesteDao.countAroItemRichSoftDelete(any(), anyString()))
                .thenReturn(10L);

        Long count = service.countItemsInRichSoftDelete(BigDecimal.valueOf(100L),
                CostantiDB.TiItemRichSoftDelete.UNI_DOC.name());

        assertEquals(10L, count);
        verify(registrazioneRichiesteDao).countAroItemRichSoftDelete(BigDecimal.valueOf(100L),
                CostantiDB.TiItemRichSoftDelete.UNI_DOC.name());
    }

    // ==================== insertAroStatoRichSoftDelete ====================

    @Test
    void insertAroStatoRichSoftDelete_success() throws Exception {
        when(registrazioneRichiesteDao.getUltimoProgressivoStatoRichiesta(anyLong()))
                .thenReturn(BigDecimal.ZERO);
        when(entityManager.find(IamUser.class, 10L)).thenReturn(mockUser);

        AroStatoRichSoftDelete stato = service.insertAroStatoRichSoftDelete(mockRich,
                CostantiDB.StatoRichSoftDelete.ACQUISITA.name(), LocalDateTime.now(), 10L);

        assertNotNull(stato);
        assertEquals(CostantiDB.StatoRichSoftDelete.ACQUISITA.name(),
                stato.getTiStatoRichSoftDelete());
        verify(entityManager).persist(any(AroStatoRichSoftDelete.class));
        verify(entityManager).merge(mockRich);
    }

    // ==================== createAroXmlRichSoftDelete ====================

    @Test
    void createAroXmlRichSoftDelete_success() throws Exception {
        AroXmlRichSoftDelete xmlRich = new AroXmlRichSoftDelete();
        when(registrazioneRichiesteDao.createAroXmlRichSoftDelete(any(), anyString(), anyString(),
                anyString())).thenReturn(xmlRich);

        AroXmlRichSoftDelete result = service.createAroXmlRichSoftDelete(mockRich, "RICHIESTA",
                "<xml/>", "1.0");

        assertNotNull(result);
        verify(registrazioneRichiesteDao).createAroXmlRichSoftDelete(mockRich, "RICHIESTA",
                "<xml/>", "1.0");
    }

    // ==================== updateStatoRichiestaToErrore ====================

    @Test
    void updateStatoRichiestaToErrore_success() throws Exception {
        when(entityManager.find(AroRichSoftDelete.class, 100L,
                LockModeType.PESSIMISTIC_WRITE)).thenReturn(mockRich);

        TypedQuery<AroStatoRichSoftDelete> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(AroStatoRichSoftDelete.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());

        when(registrazioneRichiesteDao.getUltimoProgressivoStatoRichiesta(anyLong()))
                .thenReturn(BigDecimal.ZERO);
        when(entityManager.find(IamUser.class, 10L)).thenReturn(mockUser);

        AroStatoRichSoftDelete stato = service.updateStatoRichiestaToErrore(100L, 10L);

        assertNotNull(stato);
        assertEquals(CostantiDB.StatoRichSoftDelete.ERRORE.name(), stato.getTiStatoRichSoftDelete());
        verify(entityManager, atLeastOnce()).flush();
    }

    // ==================== getStatoCorrenteRichiesta ====================

    @Test
    void getStatoCorrenteRichiesta_success() throws Exception {
        AroStatoRichSoftDelete statoCorrente = new AroStatoRichSoftDelete();
        statoCorrente.setIdStatoRichSoftDelete(1L);
        statoCorrente.setTiStatoRichSoftDelete(CostantiDB.StatoRichSoftDelete.ACQUISITA.name());

        TypedQuery<AroStatoRichSoftDelete> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(AroStatoRichSoftDelete.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(statoCorrente);

        AroStatoRichSoftDelete result = service.getStatoCorrenteRichiesta(100L);

        assertNotNull(result);
        assertEquals(CostantiDB.StatoRichSoftDelete.ACQUISITA.name(),
                result.getTiStatoRichSoftDelete());
    }

    // ==================== Helper Methods Coverage ====================

    @Test
    void getTipoItemDaTipoRichiesta_allTypes() throws Exception {
        var method = RegistrazioneRichiesteService.class
                .getDeclaredMethod("getTipoItemDaTipoRichiesta", TipoRichiestaType.class);
        method.setAccessible(true);

        assertEquals(CostantiDB.TiItemRichSoftDelete.ANNUL_VERS.name(),
                method.invoke(service, TipoRichiestaType.ANNULLAMENTO_VERSAMENTO));
        assertEquals(CostantiDB.TiItemRichSoftDelete.REST_ARCH.name(),
                method.invoke(service, TipoRichiestaType.RESTITUZIONE_ARCHIVIO));
        assertEquals(CostantiDB.TiItemRichSoftDelete.SCARTO_ARCH.name(),
                method.invoke(service, TipoRichiestaType.SCARTO_ARCHIVISTICO));

        assertThrows(Exception.class,
                () -> method.invoke(service, TipoRichiestaType.UNITA_DOCUMENTARIA));
    }

    @Test
    void getDescrizioneRichiesta_allTypes() throws Exception {
        var method = RegistrazioneRichiesteService.class
                .getDeclaredMethod("getDescrizioneRichiesta", TipoRichiestaType.class);
        method.setAccessible(true);

        assertTrue(((String) method.invoke(service, TipoRichiestaType.ANNULLAMENTO_VERSAMENTO))
                .contains("annullamento"));
        assertTrue(((String) method.invoke(service, TipoRichiestaType.RESTITUZIONE_ARCHIVIO))
                .contains("restituzione"));
        assertTrue(((String) method.invoke(service, TipoRichiestaType.SCARTO_ARCHIVISTICO))
                .contains("scarto"));
    }

    @Test
    void verificaStatoConservazionePerTipologia_annullamento() throws Exception {
        var method = RegistrazioneRichiesteService.class.getDeclaredMethod(
                "verificaStatoConservazionePerTipologia", String.class, TipoRichiestaType.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(service,
                CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name(),
                TipoRichiestaType.ANNULLAMENTO_VERSAMENTO));
        assertFalse((Boolean) method.invoke(service,
                CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name(),
                TipoRichiestaType.ANNULLAMENTO_VERSAMENTO));
    }

    @Test
    void verificaStatoConservazionePerTipologia_restituzione() throws Exception {
        var method = RegistrazioneRichiesteService.class.getDeclaredMethod(
                "verificaStatoConservazionePerTipologia", String.class, TipoRichiestaType.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(service,
                CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name(),
                TipoRichiestaType.RESTITUZIONE_ARCHIVIO));
        assertTrue((Boolean) method.invoke(service,
                CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO.name(),
                TipoRichiestaType.RESTITUZIONE_ARCHIVIO));
        assertTrue((Boolean) method.invoke(service,
                CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name(),
                TipoRichiestaType.RESTITUZIONE_ARCHIVIO));
    }
}
