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

/**
 *
 */
package it.eng.parer.soft.delete.beans;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;

import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroStatoRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;

public interface IRegistrazioneRichiesteService {

    AroRichSoftDelete insertRichSoftDelete(long idUserIam, Long idStrut, String dsRichSoftDelete,
            String ntRichSoftDelete, String tiRichSoftDelete, LocalDateTime dtCreazione,
            String tiAnnulRichSoftDelete) throws AppGenericPersistenceException;

    Long countItemsInRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete)
            throws AppGenericPersistenceException;

    Long countItemsInRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete,
            String... statiItems) throws AppGenericPersistenceException;

    AroStatoRichSoftDelete insertAroStatoRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiStatoRichSoftDelete, LocalDateTime dtRegStatoRichSoftDelete, long idUser)
            throws AppGenericPersistenceException;

    AroXmlRichSoftDelete createAroXmlRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiXmlRichSoftDelete, String blXmlRichSoftDelete, String cdVersioneXml)
            throws AppGenericPersistenceException;

    void createItems(AroRichSoftDelete rich,
            RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCancellazione,
            long idUserIam) throws AppGenericPersistenceException;

    /**
     * Aggiorna lo stato di una richiesta a ERRORE in caso di errore durante l'elaborazione
     *
     * @param idRichSoftDelete l' ID della richiesta da aggiornare
     * @param idUserIam        l'ID dell'utente che ha effettuato la richiesta
     *
     * @return il nuovo stato della richiesta
     *
     * @throws AppGenericPersistenceException in caso di errori di persistenza
     */
    AroStatoRichSoftDelete updateStatoRichiestaToErrore(Long idRichSoftDelete, Long idUserIam)
            throws AppGenericPersistenceException;

    /**
     * Recupera lo stato corrente di una richiesta di cancellazione logica.
     *
     * @param idRichSoftDelete ID della richiesta di cancellazione logica
     *
     * @return L'oggetto AroStatoRichSoftDelete rappresentante lo stato corrente della richiesta
     *
     * @throws AppGenericPersistenceException in caso di errori di persistenza
     */
    AroStatoRichSoftDelete getStatoCorrenteRichiesta(Long idRichSoftDelete)
            throws AppGenericPersistenceException;
}
