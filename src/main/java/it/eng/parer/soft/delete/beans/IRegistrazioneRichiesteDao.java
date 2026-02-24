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
import java.util.stream.Stream;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.jpa.entity.AroErrRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.DmUdDel;

public interface IRegistrazioneRichiesteDao {

    Long getIdUnitaDocVersataAnnul(BigDecimal idStrut, String cdRegistroKeyUnitaDoc,
            BigDecimal aaKeyUnitaDoc, String cdKeyUnitaDoc) throws AppGenericPersistenceException;

    Long getIdRichAnnulVersEvasaDaCancel(BigDecimal idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException;

    boolean existAroRichAnnulVersDaCancel(BigDecimal idStrut, BigDecimal idRichAnnulVers)
            throws AppGenericPersistenceException;

    BigDecimal getIdRichRestArchRestituita(BigDecimal idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException;

    boolean existAroRichRestArch(BigDecimal idStrut, BigDecimal idRichRestArch)
            throws AppGenericPersistenceException;

    AroRichSoftDelete getAroRichSoftDeleteContainingUd(Long idUnitaDoc, Long idRichSoftDelete)
            throws AppGenericPersistenceException;

    AroRichSoftDelete getAroRichSoftDeleteContainingRichAnnulVers(Long idRichAnnulVers,
            Long idRichSoftDelete) throws AppGenericPersistenceException;

    AroRichSoftDelete getAroRichSoftDeleteContainingRichRestArch(Long idRichRestArch,
            Long idRichSoftDelete, Long idStrut) throws AppGenericPersistenceException;

    List<DmUdDel> recuperaUnitaDocDaRichiesta(Long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException;

    /**
     * Recupera le unità documentarie associate a una richiesta come stream per elaborazione
     * efficiente
     */
    Stream<DmUdDel> streamUnitaDocDaRichiesta(Long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException;

    boolean isUdNonAnnullata(long idUnitaDoc) throws AppGenericPersistenceException;

    boolean isUdNonRestituita(long idUnitaDoc) throws AppGenericPersistenceException;

    List<AroErrRichSoftDelete> getAroErrRichSoftDeleteByGravity(long idItemRichSoftDelete,
            String tiGravita) throws AppGenericPersistenceException;

    boolean existAroUnitaDoc(BigDecimal idStrut, String cdRegistroKeyUnitaDoc,
            BigDecimal aaKeyUnitaDoc, String cdKeyUnitaDoc) throws AppGenericPersistenceException;

    Long countAroItemRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete,
            String... tiStato) throws AppGenericPersistenceException;

    BigDecimal getUltimoProgressivoStatoRichiesta(long idRichSoftDelete)
            throws AppGenericPersistenceException;

    void deleteAroErrRichSoftDelete(long idRichSoftDelete, String... tiErrRichSoftDelete)
            throws AppGenericPersistenceException;

    AroXmlRichSoftDelete createAroXmlRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiXmlRichSoftDelete, String blXmlRichSoftDelete, String cdVersioneXml)
            throws AppGenericPersistenceException;

    long countUnitaDocDaRichiesta(long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException;

}
