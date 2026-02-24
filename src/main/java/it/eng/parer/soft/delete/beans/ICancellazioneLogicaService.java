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

import java.util.concurrent.CompletionStage;

import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import jakarta.validation.constraints.NotNull;

public interface ICancellazioneLogicaService {

    AvanzamentoWs init(
            @NotNull(message = "ICancellazioneLogicaService.init: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "ICancellazioneLogicaService.init: cancellazioneLogica non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogica);

    void verificaVersione(String versione,
            @NotNull(message = "ICancellazioneLogicaService.verificaVersione: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "ICancellazioneLogicaService.verificaVersione: cancellazioneLogica non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogica);

    void verificaCredenziali(String loginName, String password, String indirizzoIp,
            @NotNull(message = "ICancellazioneLogicaService.verificaCredenziali: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "ICancellazioneLogicaService.verificaCredenziali: cancellazioneLogica non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogica);

    void parseXML(
            @NotNull(message = "ICancellazioneLogicaService.parseXML: datiXml non inizializzato") String datiXml,
            @NotNull(message = "ICancellazioneLogicaService.parseXML: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "ICancellazioneLogicaService.parseXML: cancellazioneLogica non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogica);

    Long esaminaRichiesteCancellazioneLogica(
            @NotNull(message = "ICancellazioneLogicaService.init: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "ICancellazioneLogicaService.init: cancellazioneLogica non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogica);

    Long registraRichieste(
            @NotNull(message = "IRegistrazioneRichiesteService.registraRichieste: rispostaWs non inizializzato") RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            @NotNull(message = "IRegistrazioneRichiesteService.registraRichieste: versamento non inizializzato") InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt)
            throws AppGenericPersistenceException;

    void createItemsInNewTransaction(Long idRichSoftDelete,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt, Long idUserIam)
            throws AppGenericPersistenceException;

    CompletionStage<Void> avviaElaborazioneAsincrona(Long idRichiesta,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt, Long idUserIam)
            throws AppGenericPersistenceException;

    void processItemById(Long idItem) throws AppGenericPersistenceException;

    void softDeleteByItem(AroItemRichSoftDelete item, SoftDeleteMode mode)
            throws AppGenericPersistenceException;

    void markItemAsFailed(Long itemId, String errorMessage) throws AppGenericPersistenceException;

    void finalizeRequest(AroRichSoftDelete request) throws AppGenericPersistenceException;
}
