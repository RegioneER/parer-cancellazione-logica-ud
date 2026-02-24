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

package it.eng.parer.soft.delete.beans;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.viewEntity.AroVLisItemRichSoftDelete;

public interface ICancellazioneLogicaDao {

    List<Long> claimBatch() throws AppGenericPersistenceException;

    List<AroItemRichSoftDelete> claimItemsForRequest(long idRichSoftDelete,
            String tiItemRichSoftDelete, int maxItems) throws AppGenericPersistenceException;

    void updateStatoItemList(Long idRichSoftDelete, String tiStatoItem)
            throws AppGenericPersistenceException;

    List<AroVLisItemRichSoftDelete> getAroVLisItemRichSoftDelete(BigDecimal idRichSoftDelete,
            String tiItemRichSoftDelete) throws AppGenericPersistenceException;

    void softDeleteBottomUp(Class<?> parentClass, Object parentId,
            AroItemRichSoftDelete aroItemRichSoftDelete) throws AppGenericPersistenceException;

    void softDeleteBottomUp(Class<?> parentClass, Object parentId, SoftDeleteMode mode,
            AroItemRichSoftDelete aroItemRichSoftDelete) throws AppGenericPersistenceException;

    List<AroRichSoftDelete> findRequestsToFinalize() throws AppGenericPersistenceException;

    List<Long> claimUdBatchToVerify(int batchSize) throws AppGenericPersistenceException;

    Map<Long, Long> getActualRecordCountsForBatch(List<Long> udIds)
            throws AppGenericPersistenceException;

    Map<Long, Long> getExpectedRecordCountsForBatch(List<Long> udIds)
            throws AppGenericPersistenceException;

    void updateUnitaDocToCancellabileBatch(List<Long> ids) throws AppGenericPersistenceException;

    void resetUnitaDocToDaCancellareBatch(List<Long> ids) throws AppGenericPersistenceException;
}
