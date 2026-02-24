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
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaDao;
import it.eng.parer.soft.delete.beans.IEntityHierarchyBuilder;
import it.eng.parer.soft.delete.beans.ILevelProcessor;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.TiItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class CancellazioneLogicaDaoTest {

    @Inject
    ICancellazioneLogicaDao cancellazioneLogicaDao;

    @Inject
    EntityManager entityManager;

    @Inject
    RootEntityContext rootEntityContext;

    @Inject
    IEntityHierarchyBuilder entityHierarchyBuilder;

    @Inject
    ILevelProcessor levelProcessor;

    @BeforeEach
    void setup() {
        assertNotNull(entityManager);
        assertNotNull(cancellazioneLogicaDao);
        assertNotNull(rootEntityContext);
    }

    @Test
    void dependencies_areInjected() {
        assertNotNull(cancellazioneLogicaDao);
        assertNotNull(entityManager);
        assertNotNull(rootEntityContext);
        assertNotNull(entityHierarchyBuilder);
        assertNotNull(levelProcessor);
        assertTrue(cancellazioneLogicaDao instanceof CancellazioneLogicaDao);
    }

    @Test
    @TestTransaction
    void claimItemsForRequest_withValidRequest() throws AppGenericPersistenceException {
        List<AroItemRichSoftDelete> items = cancellazioneLogicaDao.claimItemsForRequest(999999L,
                TiItemRichSoftDelete.UNI_DOC.name(), 10);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    @TestTransaction
    void claimBatch_returnsListOfLongs() throws AppGenericPersistenceException {
        List<Long> result = cancellazioneLogicaDao.claimBatch();
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    @TestTransaction
    void updateStatoItemList_withDifferentStates() {
        for (StatoItemRichSoftDelete stato : StatoItemRichSoftDelete.values()) {
            assertDoesNotThrow(() -> {
                cancellazioneLogicaDao.updateStatoItemList(999L, stato.name());
            });
        }
    }

    @Test
    @TestTransaction
    void getAroVLisItemRichSoftDelete_withAllItemTypes() {
        for (TiItemRichSoftDelete tipo : TiItemRichSoftDelete.values()) {
            assertDoesNotThrow(() -> {
                cancellazioneLogicaDao.getAroVLisItemRichSoftDelete(BigDecimal.valueOf(999L),
                        tipo.name());
            });
        }
    }

    @Test
    @TestTransaction
    void findRequestsToFinalize_success() throws AppGenericPersistenceException {
        assertNotNull(cancellazioneLogicaDao.findRequestsToFinalize());
    }

    @Test
    @TestTransaction
    void claimUdBatchToVerify_withDifferentBatchSizes() {
        for (int size : new int[] {
                1, 10, 100 }) {
            assertDoesNotThrow(() -> {
                List<Long> result = cancellazioneLogicaDao.claimUdBatchToVerify(size);
                assertNotNull(result);
            });
        }
    }

    @Test
    void getActualRecordCountsForBatch_handlesEmptyAndNull() throws AppGenericPersistenceException {
        assertTrue(cancellazioneLogicaDao.getActualRecordCountsForBatch(null).isEmpty());
        assertTrue(cancellazioneLogicaDao.getActualRecordCountsForBatch(Collections.emptyList())
                .isEmpty());
    }

    @Test
    @TestTransaction
    void getActualRecordCountsForBatch_withValidIds() throws AppGenericPersistenceException {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        Map<Long, Long> result = cancellazioneLogicaDao.getActualRecordCountsForBatch(ids);

        assertEquals(3, result.size());
        assertTrue(result.containsKey(1L));
    }

    @Test
    void getExpectedRecordCountsForBatch_handlesEmptyAndNull()
            throws AppGenericPersistenceException {
        assertTrue(cancellazioneLogicaDao.getExpectedRecordCountsForBatch(null).isEmpty());
        assertTrue(cancellazioneLogicaDao.getExpectedRecordCountsForBatch(Collections.emptyList())
                .isEmpty());
    }

    @Test
    @TestTransaction
    void updateUnitaDocToCancellabileBatch_handlesEmptyAndNull() {
        assertDoesNotThrow(() -> {
            cancellazioneLogicaDao.updateUnitaDocToCancellabileBatch(null);
            cancellazioneLogicaDao.updateUnitaDocToCancellabileBatch(Collections.emptyList());
        });
    }

    @Test
    @TestTransaction
    void resetUnitaDocToDaCancellareBatch_handlesEmptyAndNull() {
        assertDoesNotThrow(() -> {
            cancellazioneLogicaDao.resetUnitaDocToDaCancellareBatch(null);
            cancellazioneLogicaDao.resetUnitaDocToDaCancellareBatch(Collections.emptyList());
        });
    }

    @Test
    void rootEntityContext_management() {
        Object rootId = 123L;
        rootEntityContext.setCurrentRootId(rootId);
        assertEquals(rootId, rootEntityContext.getCurrentRootId());

        rootEntityContext.clear();
        assertNull(rootEntityContext.getCurrentRootId());
    }
}
