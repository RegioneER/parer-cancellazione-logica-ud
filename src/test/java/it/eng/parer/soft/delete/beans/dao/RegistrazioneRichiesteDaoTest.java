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

package it.eng.parer.soft.delete.beans.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.TiItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.TiXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.DmUdDel;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class RegistrazioneRichiesteDaoTest {

    @Inject
    IRegistrazioneRichiesteDao registrazioneRichiesteDao;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setup() {
        assertNotNull(entityManager);
        assertNotNull(registrazioneRichiesteDao);
    }

    @Test
    void dependencies_areInjected() {
        assertNotNull(registrazioneRichiesteDao);
        assertNotNull(entityManager);
        assertTrue(registrazioneRichiesteDao instanceof RegistrazioneRichiesteDao);
    }

    @Test
    @TestTransaction
    void getIdUnitaDocVersataAnnul_notFound() throws AppGenericPersistenceException {
        Long result = registrazioneRichiesteDao.getIdUnitaDocVersataAnnul(BigDecimal.valueOf(999L),
                "REG_TEST", BigDecimal.valueOf(2024), "DOC001");
        assertNull(result);
    }

    @Test
    @TestTransaction
    void getIdRichAnnulVersEvasaDaCancel_notFound() throws AppGenericPersistenceException {
        Long result = registrazioneRichiesteDao.getIdRichAnnulVersEvasaDaCancel(
                BigDecimal.valueOf(999L), BigDecimal.valueOf(100L));
        assertNull(result);
    }

    @Test
    @TestTransaction
    void getAroRichSoftDeleteContainingUd_withAndWithoutExcludedId()
            throws AppGenericPersistenceException {
        assertNull(registrazioneRichiesteDao.getAroRichSoftDeleteContainingUd(999L, null));
        assertNull(registrazioneRichiesteDao.getAroRichSoftDeleteContainingUd(999L, 100L));
    }

    @Test
    @TestTransaction
    void getAroRichSoftDeleteContainingRichAnnulVers_withAndWithoutExcludedId()
            throws AppGenericPersistenceException {
        assertNull(
                registrazioneRichiesteDao.getAroRichSoftDeleteContainingRichAnnulVers(999L, null));
        assertNull(
                registrazioneRichiesteDao.getAroRichSoftDeleteContainingRichAnnulVers(999L, 100L));
    }

    @Test
    @TestTransaction
    void recuperaUnitaDocDaRichiesta_returnsEmptyList() throws AppGenericPersistenceException {
        assertTrue(registrazioneRichiesteDao
                .recuperaUnitaDocDaRichiesta(999L, BigDecimal.valueOf(100L)).isEmpty());
    }

    @Test
    @TestTransaction
    void isUdNonAnnullata_returnsFalse() throws AppGenericPersistenceException {
        assertFalse(registrazioneRichiesteDao.isUdNonAnnullata(999L));
    }

    @Test
    @TestTransaction
    void existAroUnitaDoc_returnsFalse() throws AppGenericPersistenceException {
        assertFalse(registrazioneRichiesteDao.existAroUnitaDoc(BigDecimal.valueOf(999L), "REG_TEST",
                BigDecimal.valueOf(2024), "DOC001"));
    }

    @Test
    @TestTransaction
    void existAroRichAnnulVersDaCancel_returnsFalse() throws AppGenericPersistenceException {
        assertFalse(registrazioneRichiesteDao
                .existAroRichAnnulVersDaCancel(BigDecimal.valueOf(999L), BigDecimal.valueOf(100L)));
    }

    @Test
    @TestTransaction
    void countAroItemRichSoftDelete_withAndWithoutStati() throws AppGenericPersistenceException {
        assertEquals(0L, registrazioneRichiesteDao.countAroItemRichSoftDelete(
                BigDecimal.valueOf(999L), TiItemRichSoftDelete.UNI_DOC.name()));

        assertEquals(0L,
                registrazioneRichiesteDao.countAroItemRichSoftDelete(BigDecimal.valueOf(999L),
                        TiItemRichSoftDelete.UNI_DOC.name(),
                        StatoItemRichSoftDelete.DA_ELABORARE.name()));
    }

    @Test
    @TestTransaction
    void getUltimoProgressivoStatoRichiesta_returnsZero() throws AppGenericPersistenceException {
        assertEquals(BigDecimal.ZERO,
                registrazioneRichiesteDao.getUltimoProgressivoStatoRichiesta(999L));
    }

    @Test
    @TestTransaction
    void createAroXmlRichSoftDelete_createsCorrectly() throws AppGenericPersistenceException {
        AroRichSoftDelete richSoftDelete = new AroRichSoftDelete();
        AroXmlRichSoftDelete result = registrazioneRichiesteDao.createAroXmlRichSoftDelete(
                richSoftDelete, TiXmlRichSoftDelete.RICHIESTA.name(), "<xml>test</xml>", "1.0");

        assertNotNull(result);
        assertEquals(TiXmlRichSoftDelete.RICHIESTA.name(), result.getTiXmlRichSoftDelete());
        assertEquals("<xml>test</xml>", result.getBlXmlRichSoftDelete());
        assertEquals("1.0", result.getCdVersioneXml());
    }

    @Test
    @TestTransaction
    void streamUnitaDocDaRichiesta_returnsEmptyStream() throws AppGenericPersistenceException {
        Stream<DmUdDel> stream = registrazioneRichiesteDao.streamUnitaDocDaRichiesta(999L,
                BigDecimal.valueOf(100L));
        assertEquals(0, stream.count());
    }

    @Test
    @TestTransaction
    void countUnitaDocDaRichiesta_returnsZero() throws AppGenericPersistenceException {
        assertEquals(0L,
                registrazioneRichiesteDao.countUnitaDocDaRichiesta(999L, BigDecimal.valueOf(100L)));
    }
}
