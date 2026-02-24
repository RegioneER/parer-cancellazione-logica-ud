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
package it.eng.parer.soft.delete.beans.impl;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import it.eng.parer.soft.delete.beans.IConfigurationDao;
import it.eng.parer.soft.delete.beans.IControlliSemanticiService;
import it.eng.parer.soft.delete.beans.IControlliWsService;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaParserService;
import it.eng.parer.soft.delete.beans.XmlSoftDeleteCache;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.CSVersatore;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS.SeverityEnum;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.utils.xml.XmlValidationEventHandler;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.ws.xml.esitoRichSoftDelete.ECRichiestaType;
import it.eng.parer.ws.xml.esitoRichSoftDelete.SCVersatoreType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;

@SuppressWarnings("unchecked")
@ApplicationScoped
public class CancellazioneLogicaParserService implements ICancellazioneLogicaParserService {

    // Istanza della richiesta cancellazione logica decodificata dall'XML di richiesta
    RichiestaCancellazioneLogica richiesta;
    // controlli sul db
    IControlliSemanticiService controlliSemanticiService;
    // verifica autorizzazione ws
    IControlliWsService controlliWsService;
    // singleton gestione cache dei parser jaxb della cancellazione logica
    XmlSoftDeleteCache xmlSoftDeleteCache;
    // gestione configurazioni applicative
    IConfigurationDao configurationDao;

    @Inject
    public CancellazioneLogicaParserService(IControlliSemanticiService controlliSemanticiService,
            IControlliWsService controlliWsService, XmlSoftDeleteCache xmlSoftDeleteCache,
            IConfigurationDao configurationDao) {
        this.controlliSemanticiService = controlliSemanticiService;
        this.controlliWsService = controlliWsService;
        this.xmlSoftDeleteCache = xmlSoftDeleteCache;
        this.configurationDao = configurationDao;
    }

    @Override
    public void parseXML(String datiXml,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt,
            RispostaWSInvioRichiestaCancellazioneLogica rispostaWs) {
        //

        StringReader tmpReader;

        cancellazioneLogicaExt.setXmlRichiesta(datiXml);
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpReader = new StringReader(datiXml);
            XmlValidationEventHandler validationHandler = new XmlValidationEventHandler();
            try {
                Unmarshaller unmarshaller = xmlSoftDeleteCache.getRichSoftDeleteCtx()
                        .createUnmarshaller();
                unmarshaller.setSchema(xmlSoftDeleteCache.getRichSoftDeleteSchema());
                unmarshaller.setEventHandler(validationHandler);
                richiesta = (RichiestaCancellazioneLogica) unmarshaller.unmarshal(tmpReader);
                cancellazioneLogicaExt.setRichiestaCancellazioneLogica(richiesta);
            } catch (JAXBException e) {
                ValidationEvent event = validationHandler.getFirstErrorValidationEvent();
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode(MessaggiWSBundle.RICH_SOFT_DELETE_001);
                rispostaWs.setErrorMessage(MessaggiWSBundle
                        .getString(MessaggiWSBundle.RICH_SOFT_DELETE_001, event.getMessage()));
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.RICH_SOFT_DELETE_001,
                        event.getMessage());
            } catch (Exception e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode(MessaggiWSBundle.RICH_SOFT_DELETE_001);
                rispostaWs.setErrorMessage(
                        MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_001));
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.RICH_SOFT_DELETE_001,
                        MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_001));
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            popolaVersatoreRichiestaEsito(cancellazioneLogicaExt, rispostaWs);
        }

        /* CONTROLLO TAG VERSIONE XML (RICH_SOFT_DELETE_005) */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && !richiesta.getVersioneXmlRichiesta()
                .equals(cancellazioneLogicaExt.getVersioneWsChiamata())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_005,
                    MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_005));
        }

        /* CONTROLLO TAG USERID (RICH_SOFT_DELETE_002) */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && !richiesta.getVersatore().getUserID()
                .equals(cancellazioneLogicaExt.getLoginName())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_002,
                    MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_002));
        }

        /* CONTROLLO TAG TIPORICHIESTA (RICH_SOFT_DELETE_017) */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && richiesta
                .getRichiesteDiCancellazione().getRichiestaDiCancellazione().stream()
                .collect(Collectors.groupingBy(RichiestaDiCancellazioneType::getTipoRichiesta))
                .values().stream().distinct().count() > 1) {
            // Le richieste di cancellazione logica definite nella richiesta devono
            // avere stesso
            // Tipo Richiesta
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_017,
                    MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_017));
        }

        // RICH_SOFT_DELETE_018
        // RICH_SOFT_DELETE_019
        // RICH_SOFT_DELETE_020
        // RICH_SOFT_DELETE_021
        /* CONTROLLO TAG TIPOREGISTRO */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            richiesta.getRichiesteDiCancellazione().getRichiestaDiCancellazione().stream()
                    .findFirst().ifPresent(richiestaDiCancellazione -> {
                        if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.UNITA_DOCUMENTARIA.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getTipoRegistro)
                                        .anyMatch(x -> x == null)) {
                            // Per almeno una unità documentaria
                            // definita nella richiesta è
                            // necessario specificare il
                            // Tipo Registro
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_021,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_021));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.ANNULLAMENTO_VERSAMENTO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getTipoRegistro)
                                        .anyMatch(x -> x != null)) {
                            // Per almeno un annullamento definito nella
                            // richiesta è presente il
                            // Tipo Registro ma non
                            // è previsto
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_018,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_018));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.RESTITUZIONE_ARCHIVIO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getTipoRegistro)
                                        .anyMatch(x -> x != null)) {
                            // Per almeno una restituzione definita
                            // nella richiesta è presente il
                            // Tipo Registro ma non
                            // è previsto
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_019,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_019));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.SCARTO_ARCHIVISTICO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getTipoRegistro)
                                        .anyMatch(x -> x != null)) {
                            // Per almeno uno scarto definito nella
                            // richiesta è presente il Tipo
                            // Registro ma non
                            // è previsto
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_020,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_020));
                        }
                    });
        }

        // RICH_SOFT_DELETE_022
        // RICH_SOFT_DELETE_023
        // RICH_SOFT_DELETE_024
        // RICH_SOFT_DELETE_025
        /* CONTROLLO TAG IDRICHIESTASACER */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            richiesta.getRichiesteDiCancellazione().getRichiestaDiCancellazione().stream()
                    .findFirst().ifPresent(richiestaDiCancellazione -> {
                        if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.UNITA_DOCUMENTARIA.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getIDRichiestaSacer)
                                        .anyMatch(x -> x != null)) {
                            // Per almeno una unità documentaria
                            // definita nella richiesta è presente
                            // l'ID Richiesta
                            // Sacer ma non
                            // è previsto
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_025,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_025));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.ANNULLAMENTO_VERSAMENTO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getIDRichiestaSacer)
                                        .anyMatch(x -> x == null)) {
                            // Per almeno una annullamento definito
                            // nella richiesta è necessario
                            // specificare l'ID
                            // Richiesta
                            // Sacer
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_022,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_022));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.RESTITUZIONE_ARCHIVIO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getIDRichiestaSacer)
                                        .anyMatch(x -> x == null)) {
                            // Per almeno una restituzione definita
                            // nella richiesta è necessario
                            // specificare l'ID
                            // Richiesta Sacer
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_023,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_023));
                        } else if (richiestaDiCancellazione.getTipoRichiesta().value()
                                .equals(TipoRichiestaType.SCARTO_ARCHIVISTICO.value())
                                && richiesta.getRichiesteDiCancellazione()
                                        .getRichiestaDiCancellazione().stream()
                                        .map(RichiestaDiCancellazioneType::getIDRichiestaSacer)
                                        .anyMatch(x -> x == null)) {
                            // Per almeno uno scarto definito nella
                            // richiesta è necessario
                            // specificare l'ID Richiesta
                            // Sacer
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_024,
                                    MessaggiWSBundle
                                            .getString(MessaggiWSBundle.RICH_SOFT_DELETE_024));
                            rispostaWs.setEsitoWsError(MessaggiWSBundle.ERR_666,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666));
                        }
                    });

        }

        /* CONTROLLO VERSATORE (ambiente, ente e struttura) */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            CSVersatore tmpCSVersatore = new CSVersatore();
            tmpCSVersatore.setAmbiente(richiesta.getVersatore().getAmbiente());
            tmpCSVersatore.setEnte(richiesta.getVersatore().getEnte());
            tmpCSVersatore.setStruttura(richiesta.getVersatore().getStruttura());

            RispostaControlli rispostaControlli = controlliSemanticiService
                    .checkIdStrut(tmpCSVersatore);
            if (rispostaControlli.getrLong() < 1) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // salvo idStruttura
                cancellazioneLogicaExt.setIdStrut(rispostaControlli.getrLong());
            }
        }

        /*
         * CONTROLLO ABILITAZIONE SERVIZIO -- (RICH_SOFT_DELETE_003) N.B. LO METTO QUI PERCHE' DEVO
         * AVER CALCOLATO LA STRUTTURA PRIMA!
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            cancellazioneLogicaExt.getUtente()
                    .setIdOrganizzazioneFoglia(new BigDecimal(cancellazioneLogicaExt.getIdStrut()));

            RispostaControlli rispostaControlli = controlliWsService.checkAuthWS(
                    cancellazioneLogicaExt.getUtente(), cancellazioneLogicaExt.getDescrizione());
            if (!rispostaControlli.isrBoolean()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(MessaggiWSBundle.RICH_SOFT_DELETE_003,
                        MessaggiWSBundle.getString(MessaggiWSBundle.RICH_SOFT_DELETE_003));
            }
        }
    }

    private void popolaVersatoreRichiestaEsito(
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt,
            RispostaWSInvioRichiestaCancellazioneLogica rispostaWs) {
        rispostaWs.getEsitoRichiestaCancellazioneLogica().setVersatore(new SCVersatoreType());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersatore()
                .setAmbiente(cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getVersatore()
                        .getAmbiente());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersatore().setEnte(
                cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getVersatore().getEnte());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersatore()
                .setStruttura(cancellazioneLogicaExt.getRichiestaCancellazioneLogica()
                        .getVersatore().getStruttura());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersatore()
                .setUserID(cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getVersatore()
                        .getUserID());
        if (cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getVersatore()
                .getUtente() != null
                && !cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getVersatore()
                        .getUtente().isEmpty()) {
            rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersatore()
                    .setUtente(cancellazioneLogicaExt.getRichiestaCancellazioneLogica()
                            .getVersatore().getUtente());
        }
        rispostaWs.getEsitoRichiestaCancellazioneLogica().setRichiesta(new ECRichiestaType());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesta()
                .setDescrizione(cancellazioneLogicaExt.getRichiestaCancellazioneLogica()
                        .getRichiesta().getDescrizione());
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesta()
                .setMotivazione(cancellazioneLogicaExt.getRichiestaCancellazioneLogica()
                        .getRichiesta().getMotivazione());
    }
}
