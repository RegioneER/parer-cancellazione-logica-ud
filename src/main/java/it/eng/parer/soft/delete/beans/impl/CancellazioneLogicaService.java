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

import static it.eng.parer.soft.delete.beans.utils.converter.DateUtilsConverter.convert;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ApplicationNotRunning;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaDao;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaParserService;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaService;
import it.eng.parer.soft.delete.beans.IControlliWsService;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteService;
import it.eng.parer.soft.delete.beans.XmlSoftDeleteCache;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.WSDescCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS.SeverityEnum;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.exceptions.ParamApplicNotFoundException;
import it.eng.parer.soft.delete.beans.security.User;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.CostantiDB;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.beans.utils.Costanti.VersioneWS;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.TiItemRichSoftDelete;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.soft.delete.beans.utils.xml.XmlDateUtility;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroStatoRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.viewEntity.AroVLisItemRichSoftDelete;
import it.eng.parer.ws.xml.esitoRichSoftDelete.CodiceEsitoType;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaType;
import it.eng.parer.ws.xml.esitoRichSoftDelete.ECRichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.validation.ValidationException;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

@ApplicationScoped
public class CancellazioneLogicaService implements ICancellazioneLogicaService {

    private static final Logger log = LoggerFactory.getLogger(CancellazioneLogicaService.class);

    private static final String LOG_MESSAGE_SOFT_DELETE_UD = "Cancellazione Logica Unit\u00E0 Documentarie ---";
    private static final String LOG_MESSAGE_ERROR_UPDATE = "Impossibile aggiornare lo stato della richiesta a ERRORE dopo un'eccezione: {}";

    IControlliWsService controlliWsService;

    ICancellazioneLogicaParserService cancellazioneLogicaParserService;

    IRegistrazioneRichiesteService registrazioneRichiesteService;

    ICancellazioneLogicaDao cancellazioneLogicaDao;

    IRegistrazioneRichiesteDao registrazioneRichiesteDao;

    EntityManager entityManager;

    XmlSoftDeleteCache xmlSoftDeleteCache;

    ManagedExecutor executor;

    @ConfigProperty(name = "quarkus.uuid")
    String instanceId;

    @ConfigProperty(name = "worker.batch.size", defaultValue = "5")
    int batchSize;

    @ConfigProperty(name = "worker.poll.enabled", defaultValue = "true")
    boolean pollingEnabled;

    @ConfigProperty(name = "worker.claim.timeout-minutes", defaultValue = "30")
    int claimTimeoutMinutes;

    @ConfigProperty(name = "kafka.verify.batch.size", defaultValue = "50")
    int kafkaVerifyBatchSize;

    @Inject
    public CancellazioneLogicaService(IControlliWsService controlliWsService,
            ICancellazioneLogicaParserService cancellazioneLogicaParserService,
            IRegistrazioneRichiesteService registrazioneRichiesteService,
            ICancellazioneLogicaDao cancellazioneLogicaDao,
            IRegistrazioneRichiesteDao registrazioneRichiesteDao, EntityManager entityManager,
            XmlSoftDeleteCache xmlSoftDeleteCache, ManagedExecutor executor) {
        this.controlliWsService = controlliWsService;
        this.cancellazioneLogicaParserService = cancellazioneLogicaParserService;
        this.registrazioneRichiesteService = registrazioneRichiesteService;
        this.cancellazioneLogicaDao = cancellazioneLogicaDao;
        this.registrazioneRichiesteDao = registrazioneRichiesteDao;
        this.entityManager = entityManager;
        this.xmlSoftDeleteCache = xmlSoftDeleteCache;
        this.executor = executor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AvanzamentoWs init(RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt) {
        //
        Date now = convert(LocalDateTime.now());
        //
        Map<String, String> wsVersions = null;
        // base
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());
        // load versions
        RispostaControlli rcLoadWsVers = controlliWsService
                .loadWsVersions(cancellazioneLogicaExt.getDescrizione());
        // if positive ...
        if (rcLoadWsVers.isrBoolean()) {
            wsVersions = (Map<String, String>) rcLoadWsVers.getrObject();
            cancellazioneLogicaExt.setWsVersions(wsVersions);
        }

        // base
        AvanzamentoWs avanzamento = AvanzamentoWs.nuovoAvanzamentoWS("Q-" + instanceId,
                AvanzamentoWs.Funzioni.CANCELLAZIONE_LOGICA);
        // aggancia alla rispostaWS
        rispostaWs.setAvanzamento(avanzamento);

        // base
        rispostaWs.setSeverity(IRispostaWS.SeverityEnum.OK);
        rispostaWs.setErrorCode(StringUtils.EMPTY);
        rispostaWs.setErrorMessage(StringUtils.EMPTY);

        // prepara la classe esito e la aggancia alla rispostaWS
        EsitoRichiestaCancellazioneLogica myEsito = new EsitoRichiestaCancellazioneLogica();
        rispostaWs.setEsitoRichiestaCancellazioneLogica(myEsito);

        //
        myEsito.setVersioneXmlEsito(
                cancellazioneLogicaExt.getDescrizione().getVersione(wsVersions));
        //
        myEsito.setVersioneXmlRichiesta(
                cancellazioneLogicaExt.getDescrizione().getVersione(wsVersions));
        //
        myEsito.setDataRichiesta(XmlDateUtility.dateToXMLGregorianCalendarOrNull(now));

        //
        myEsito.setEsitoRichiesta(new EsitoRichiestaType());
        if (!rcLoadWsVers.isrBoolean()) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rcLoadWsVers.getCodErr(), rcLoadWsVers.getDsErr());
        } else {
            myEsito.getEsitoRichiesta().setCodiceEsito(CodiceEsitoType.POSITIVO);
            myEsito.getEsitoRichiesta().setCodiceErrore(StringUtils.EMPTY);
            myEsito.getEsitoRichiesta().setMessaggioErrore(StringUtils.EMPTY);
        }

        cancellazioneLogicaExt.setDataElaborazione(now);

        return avanzamento;
    }

    @Override
    public void verificaVersione(String versione,
            RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt) {
        log.atDebug().log("sono nel metodo verificaVersione");
        EsitoRichiestaCancellazioneLogica myEsito = rispostaWs
                .getEsitoRichiestaCancellazioneLogica();
        cancellazioneLogicaExt.setVersioneWsChiamata(versione);
        myEsito.setVersioneXmlRichiesta(versione);
        RispostaControlli rcCheckVers = controlliWsService.checkVersione(versione,
                cancellazioneLogicaExt.getDescrizione().getNomeWs(),
                cancellazioneLogicaExt.getWsVersions());
        if (!rcCheckVers.isrBoolean()) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rcCheckVers.getCodErr(), rcCheckVers.getDsErr());
        } else {
            myEsito.setVersioneXmlEsito(VersioneWS.calculate(versione).getVersion());
        }
    }

    @Override
    public void verificaCredenziali(String loginName, String password, String indirizzoIp,
            RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt) {
        log.atDebug().log("sono nel metodo verificaCredenziali");
        User tmpUser = null;
        RispostaControlli rcCheckCred = controlliWsService.checkCredenziali(loginName, password,
                indirizzoIp);
        if (rcCheckCred.isrBoolean()) {
            tmpUser = (User) rcCheckCred.getrObject();
            rcCheckCred = controlliWsService.checkAuthWSNoOrg(tmpUser,
                    cancellazioneLogicaExt.getDescrizione());
        }
        if (!rcCheckCred.isrBoolean()) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rcCheckCred.getCodErr(), rcCheckCred.getDsErr());
        }

        cancellazioneLogicaExt.setLoginName(loginName);
        cancellazioneLogicaExt.setUtente(tmpUser);
    }

    @Override
    public void parseXML(String datiXml, RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt) {
        log.atDebug().log("sono nel metodo parseXML");

        if (cancellazioneLogicaExt.getUtente() == null) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore: l'utente non è autenticato.");
            return;
        }

        try {
            cancellazioneLogicaParserService.parseXML(datiXml, cancellazioneLogicaExt, rispostaWs);
        } catch (Exception e) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            if (ExceptionUtils.getRootCause(e) instanceof ParamApplicNotFoundException rootCause) {
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_018_001,
                        rootCause.getNmParamApplic());
            } else {
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore generico nella fase di parsing dell'XML "
                                + ExceptionUtils.getMessage(e));
            }
            log.atError().log("Errore generico nella fase di parsing dell'XML", e);
        }
    }

    @Override
    public Long esaminaRichiesteCancellazioneLogica(
            RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt) {
        log.atDebug().log("sono nel metodo esaminaRichiesteCancellazioneLogica");

        if (cancellazioneLogicaExt.getUtente() == null) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore: l'utente non è autenticato.");
            return null;
        }

        Long idRichiesta = null;
        try {
            log.info("Registro la richiesta di cancellazione logica ... ");
            // Apro transazione e registro le richieste
            idRichiesta = registraRichieste(rispostaWs, cancellazioneLogicaExt);
            log.info("Richiesta registrata");
            // Chiusa transazione
            if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR && idRichiesta != null) {
                AroRichSoftDelete richSoftDelete = entityManager.find(AroRichSoftDelete.class,
                        idRichiesta);

                String tiItemRichSoftDelete;
                if (CostantiDB.TiRichSoftDelete.ANNULLAMENTO.name()
                        .equals(richSoftDelete.getTiRichSoftDelete())) {
                    tiItemRichSoftDelete = TiItemRichSoftDelete.ANNUL_VERS.name();
                } else if (CostantiDB.TiRichSoftDelete.RESTITUZIONE.name()
                        .equals(richSoftDelete.getTiRichSoftDelete())) {
                    tiItemRichSoftDelete = TiItemRichSoftDelete.REST_ARCH.name();
                } else if (CostantiDB.TiRichSoftDelete.SCARTO.name()
                        .equals(richSoftDelete.getTiRichSoftDelete())) {
                    tiItemRichSoftDelete = TiItemRichSoftDelete.SCARTO_ARCH.name();
                } else {
                    tiItemRichSoftDelete = TiItemRichSoftDelete.UNI_DOC.name();
                }
                Long numeroItems = registrazioneRichiesteService.countItemsInRichSoftDelete(
                        new BigDecimal(richSoftDelete.getIdRichSoftDelete()), tiItemRichSoftDelete);
                Long numeroItemsNonElaborabili = registrazioneRichiesteService
                        .countItemsInRichSoftDelete(
                                new BigDecimal(richSoftDelete.getIdRichSoftDelete()),
                                tiItemRichSoftDelete,
                                CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name());
                String xmlRisposta = generaRisposta(rispostaWs, numeroItems,
                        numeroItemsNonElaborabili, richSoftDelete);
                // Registro l'XML ricevuto
                registrazioneRichiesteService.createAroXmlRichSoftDelete(richSoftDelete,
                        CostantiDB.TiXmlRichSoftDelete.RISPOSTA.name(), xmlRisposta,
                        rispostaWs.getEsitoRichiestaCancellazioneLogica().getVersioneXmlEsito());
            }
        } catch (Exception e) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore nella fase di generazione dell'XML di risposta del bean "
                            + ExceptionUtils.getRootCauseMessage(e));
            log.error("Errore nella fase di generazione dell'XML di risposta del bean: {}",
                    e.getMessage(), e);
            if (idRichiesta != null) {
                log.debug("{} Aggiorno lo stato della richiesta a ERRORE {}",
                        LOG_MESSAGE_SOFT_DELETE_UD, idRichiesta);
                try {
                    registrazioneRichiesteService.updateStatoRichiestaToErrore(idRichiesta,
                            cancellazioneLogicaExt.getUtente().getIdUtente());
                } catch (Exception ex) {
                    log.error(LOG_MESSAGE_ERROR_UPDATE, ex.getMessage(), ex);
                }
            }
        }

        return idRichiesta;
    }

    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public Long registraRichieste(RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt)
            throws AppGenericPersistenceException {
        Long idRichiesta = null;

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            User user = cancellazioneLogicaExt.getUtente();
            Long idStrut = cancellazioneLogicaExt.getIdStrut();
            String descrizione = cancellazioneLogicaExt.getRichiestaCancellazioneLogica()
                    .getRichiesta().getDescrizione();
            String note = cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getRichiesta()
                    .getMotivazione();

            TipoCancellazioneType tipoCancellazione = TipoCancellazioneType.CAMPIONE;

            TipoRichiestaType tipoRichiesta = cancellazioneLogicaExt
                    .getRichiestaCancellazioneLogica().getRichiesteDiCancellazione()
                    .getRichiestaDiCancellazione().get(0).getTipoRichiesta();

            String partMsg;
            if (TipoRichiestaType.ANNULLAMENTO_VERSAMENTO.value().equals(tipoRichiesta.value())) {
                partMsg = "annullamento versamento";
            } else if (TipoRichiestaType.RESTITUZIONE_ARCHIVIO.value()
                    .equals(tipoRichiesta.value())) {
                partMsg = "restituzione archivio";
            } else if (TipoRichiestaType.SCARTO_ARCHIVISTICO.value()
                    .equals(tipoRichiesta.value())) {
                partMsg = "scarto archivistico";
            } else {
                partMsg = "unità documentaria";
            }
            log.info("{} --- Apertura transazione richiesta cancellazione logica per {}",
                    RegistrazioneRichiesteService.class.getSimpleName(), partMsg);

            String tiRichSoftDelete;
            if (TipoRichiestaType.ANNULLAMENTO_VERSAMENTO.value().equals(tipoRichiesta.value())) {
                tiRichSoftDelete = CostantiDB.TiRichSoftDelete.ANNULLAMENTO.name();
            } else if (TipoRichiestaType.RESTITUZIONE_ARCHIVIO.value()
                    .equals(tipoRichiesta.value())) {
                tiRichSoftDelete = CostantiDB.TiRichSoftDelete.RESTITUZIONE.name();
            } else if (TipoRichiestaType.SCARTO_ARCHIVISTICO.value()
                    .equals(tipoRichiesta.value())) {
                tiRichSoftDelete = CostantiDB.TiRichSoftDelete.SCARTO.name();
            } else {
                tiRichSoftDelete = CostantiDB.TiRichSoftDelete.UD.name();
            }

            String tiCancelRichSoftDelete = "CAMPIONE";
            if (cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getRichiesta()
                    .getTipoCancellazione() != null
                    && !cancellazioneLogicaExt.getRichiestaCancellazioneLogica().getRichiesta()
                            .getTipoCancellazione().value().equals(tipoCancellazione.value())) {
                tiCancelRichSoftDelete = "COMPLETA";
            }

            // Registro la richiesta di cancellazione logica
            AroRichSoftDelete richSoftDelete = registrazioneRichiesteService.insertRichSoftDelete(
                    user.getIdUtente(), idStrut, descrizione, note, tiRichSoftDelete,
                    cancellazioneLogicaExt.getDataElaborazione().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    tiCancelRichSoftDelete);

            log.info("{} --- Registro su DB gli XML di richiesta ed esito cancellazione logica {}",
                    RegistrazioneRichiesteService.class.getSimpleName(), partMsg);

            // Registro l'XML ricevuto
            AroXmlRichSoftDelete xmlRichSoftDelete = registrazioneRichiesteDao
                    .createAroXmlRichSoftDelete(richSoftDelete,
                            CostantiDB.TiXmlRichSoftDelete.RICHIESTA.name(),
                            cancellazioneLogicaExt.getXmlRichiesta(), cancellazioneLogicaExt
                                    .getRichiestaCancellazioneLogica().getVersioneXmlRichiesta());
            entityManager.persist(xmlRichSoftDelete);

            // Imposta lo stato a PRESA_IN_CARICO
            registrazioneRichiesteService.insertAroStatoRichSoftDelete(richSoftDelete,
                    CostantiDB.StatoRichSoftDelete.PRESA_IN_CARICO.name(),
                    cancellazioneLogicaExt.getDataElaborazione().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    user.getIdUtente());

            log.info(
                    "Richiesta {} registrata con stato PRESA_IN_CARICO. Avvio elaborazione asincrona...",
                    idRichiesta);

            // Aggiorna l'esito della risposta
            EsitoRichiestaCancellazioneLogica myEsito = rispostaWs
                    .getEsitoRichiestaCancellazioneLogica();
            myEsito.getEsitoRichiesta().setCodiceEsito(CodiceEsitoType.POSITIVO);
            myEsito.getEsitoRichiesta().setCodiceErrore("");
            myEsito.getEsitoRichiesta().setMessaggioErrore("");

            entityManager.flush();
            idRichiesta = richSoftDelete.getIdRichSoftDelete();
        }
        return idRichiesta;
    }

    /**
     * Avvia l'elaborazione asincrona degli item della richiesta usando ManagedExecutor
     */
    @Override
    public CompletionStage<Void> avviaElaborazioneAsincrona(Long idRichSoftDelete,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt, Long idUserIam)
            throws AppGenericPersistenceException {

        // Memorizza il momento di inizio per statistiche
        LocalDateTime startTime = LocalDateTime.now();

        return executor.runAsync(() -> {
            try {
                AroStatoRichSoftDelete statoRichSoftDelete = registrazioneRichiesteService
                        .getStatoCorrenteRichiesta(idRichSoftDelete);
                log.info("Stato corrente della richiesta ID {}: {}", idRichSoftDelete,
                        statoRichSoftDelete.getTiStatoRichSoftDelete());
                if (statoRichSoftDelete.getTiStatoRichSoftDelete()
                        .equals(CostantiDB.StatoRichSoftDelete.PRESA_IN_CARICO.name())) {
                    log.info("Inizio elaborazione asincrona degli item per la richiesta {}",
                            idRichSoftDelete);

                    // Nuova transazione per elaborare gli item
                    createItemsInNewTransaction(idRichSoftDelete, cancellazioneLogicaExt,
                            idUserIam);

                    // Calcola la durata dell'elaborazione
                    long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
                    log.info("Elaborazione asincrona completata per la richiesta ID: {} in {} ms",
                            idRichSoftDelete, duration);
                }
            } catch (AppGenericPersistenceException agpex) {
                log.error("Errore nell'elaborazione asincrona della richiesta ID: {} - {}",
                        idRichSoftDelete, agpex.getMessage(), agpex);
                try {
                    registrazioneRichiesteService.updateStatoRichiestaToErrore(idRichSoftDelete,
                            idUserIam);
                } catch (Exception ex) {
                    log.error("Impossibile aggiornare lo stato della richiesta a ERRORE: {}",
                            ex.getMessage(), ex);
                }
            } catch (Exception e) {
                log.error("Errore durante l'elaborazione: {}", e.getMessage(), e);

                try {
                    registrazioneRichiesteService.updateStatoRichiestaToErrore(idRichSoftDelete,
                            idUserIam);
                } catch (Exception ex) {
                    log.error(LOG_MESSAGE_ERROR_UPDATE, ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     * Elabora gli item in una nuova transazione
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void createItemsInNewTransaction(Long idRichSoftDelete,
            InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt, Long idUserIam)
            throws AppGenericPersistenceException {
        // Recupera la richiesta
        AroRichSoftDelete richSoftDelete = entityManager.find(AroRichSoftDelete.class,
                idRichSoftDelete, LockModeType.PESSIMISTIC_WRITE);

        // Elabora gli item della richiesta con il metodo ottimizzato che usa stream JPA
        registrazioneRichiesteService.createItems(richSoftDelete, cancellazioneLogicaExt
                .getRichiestaCancellazioneLogica().getRichiesteDiCancellazione(), idUserIam);

        // Recupero gli stati associati alla richiesta
        TypedQuery<AroStatoRichSoftDelete> query = entityManager.createQuery(
                "SELECT a FROM AroStatoRichSoftDelete a JOIN FETCH a.aroRichSoftDelete rich WHERE rich = :aroRichSoftDelete",
                AroStatoRichSoftDelete.class);
        query.setParameter("aroRichSoftDelete", richSoftDelete);
        richSoftDelete.setAroStatoRichSoftDelete(query.getResultList());

        // Aggiorna lo stato della richiesta a ACQUISITA
        AroStatoRichSoftDelete nuovoStato = registrazioneRichiesteService
                .insertAroStatoRichSoftDelete(richSoftDelete,
                        CostantiDB.StatoRichSoftDelete.ACQUISITA.name(), LocalDateTime.now(),
                        idUserIam);

        richSoftDelete
                .setIdStatoRichSoftDeleteCor(new BigDecimal(nuovoStato.getIdStatoRichSoftDelete()));
        entityManager.merge(richSoftDelete);

        log.info("Stato della richiesta {} aggiornato a ACQUISITA", idRichSoftDelete);
    }

    /**
     * Finalizza una richiesta marcandola come EVASA
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void finalizeRequest(AroRichSoftDelete request) throws AppGenericPersistenceException {
        try {
            log.info("Finalizzazione richiesta ID: {}", request.getIdRichSoftDelete());

            // Ricarica la richiesta con lock pessimistico per evitare race condition
            request = entityManager.find(AroRichSoftDelete.class, request.getIdRichSoftDelete(),
                    LockModeType.PESSIMISTIC_WRITE);

            // Verifica che la richiesta sia ancora in stato ACQUISITA
            AroStatoRichSoftDelete currentStatus = entityManager.find(AroStatoRichSoftDelete.class,
                    request.getIdStatoRichSoftDeleteCor());

            if (!CostantiDB.StatoRichSoftDelete.ACQUISITA.name()
                    .equals(currentStatus.getTiStatoRichSoftDelete())) {
                log.info("Richiesta {} già in stato {}, skip finalizzazione",
                        request.getIdRichSoftDelete(), currentStatus.getTiStatoRichSoftDelete());
                return;
            }

            // Crea nuovo record di stato EVASA
            AroStatoRichSoftDelete newStatus = registrazioneRichiesteService
                    .insertAroStatoRichSoftDelete(request,
                            CostantiDB.StatoRichSoftDelete.EVASA.name(), LocalDateTime.now(),
                            currentStatus.getIamUser().getIdUserIam());
            entityManager.persist(newStatus);

            // Aggiorna il riferimento allo stato corrente
            request.setIdStatoRichSoftDeleteCor(
                    new BigDecimal(newStatus.getIdStatoRichSoftDelete()));
            entityManager.merge(request);

            // Modifica gli item ANNUL_VERS, REST_ARCH, SCARTO_ARCH assegnando stato
            // ELABORATO
            cancellazioneLogicaDao.updateStatoItemList(request.getIdRichSoftDelete(),
                    CostantiDB.StatoItemRichSoftDelete.ELABORATO.name());

            log.info("Richiesta ID: {} finalizzata con successo", request.getIdRichSoftDelete());
        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Errore durante la finalizzazione della richiesta "
                            + request.getIdRichSoftDelete() + ": "
                            + ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Worker loop che esegue polling per trovare item da elaborare Questo è il meccanismo che
     * bilancia il carico tra tutti i pod
     */
    @Scheduled(every = "5s", skipExecutionIf = ApplicationNotRunning.class, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void pollAndProcessItems() {
        if (!pollingEnabled) {
            return;
        }

        try {
            // Tenta di reclamare un batch di item con lock pessimistico
            List<Long> claimedItemIds = cancellazioneLogicaDao.claimBatch();

            if (claimedItemIds.isEmpty()) {
                return; // Nessun item disponibile
            }

            log.info("Worker loop ha reclamato {} item da elaborare", claimedItemIds.size());

            // Elabora ogni item in una transazione separata
            for (Long itemId : claimedItemIds) {
                try {
                    processItemById(itemId);
                } catch (Exception e) {
                    log.error("Errore durante l'elaborazione dell'item {}: {}", itemId,
                            ExceptionUtils.getRootCauseMessage(e), e);
                    markItemAsFailed(itemId, ExceptionUtils.getRootCauseMessage(e));
                }
            }
        } catch (Exception e) {
            log.error("Errore nel polling loop: {}", ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    /**
     * Job schedulato che verifica periodicamente le richieste da finalizzare (tutte quelle che
     * hanno tutti gli item elaborati ma non sono ancora nello stato EVASA)
     */
    @Scheduled(every = "10s", skipExecutionIf = ApplicationNotRunning.class, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void finalizeCompletedRequests() {
        if (!pollingEnabled) {
            return;
        }

        try {
            // Trova richieste che sono in stato ACQUISITA ma con tutti gli item già
            // elaborati
            List<AroRichSoftDelete> completedRequests = cancellazioneLogicaDao
                    .findRequestsToFinalize();

            if (completedRequests.isEmpty()) {
                return;
            }

            log.info("Trovate {} richieste da finalizzare", completedRequests.size());

            // Finalizza ogni richiesta in una transazione separata
            for (AroRichSoftDelete request : completedRequests) {
                try {
                    finalizeRequest(request);
                } catch (Exception e) {
                    log.error("Errore durante la finalizzazione della richiesta {}: {}",
                            request.getIdRichSoftDelete(), ExceptionUtils.getRootCauseMessage(e),
                            e);
                }
            }
        } catch (Exception e) {
            log.error("Errore nel job di finalizzazione richieste: {}",
                    ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    /**
     * Job schedulato che verifica i record Kafka scritti nel database e aggiorna lo stato delle
     * unità documentarie quando i conteggi coincidono. Utilizza approccio in due fasi.
     */
    @Scheduled(every = "3s", skipExecutionIf = ApplicationNotRunning.class, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void verifyKafkaRecordsLoaded() {
        if (!pollingEnabled) {
            return;
        }

        try {
            // FASE 1: Reclama un batch di UD e le marca come VERIFICA_IN_CORSO
            List<Long> udBatch = cancellazioneLogicaDao.claimUdBatchToVerify(kafkaVerifyBatchSize);

            if (udBatch.isEmpty()) {
                return;
            }

            log.info("Reclamate {} unità documentarie da verificare", udBatch.size());

            // FASE 2.1: Ottieni i conteggi effettivi per il batch reclamato
            Map<Long, Long> actualCounts = cancellazioneLogicaDao
                    .getActualRecordCountsForBatch(udBatch);

            // FASE 2.2: Ottieni i conteggi attesi per il batch reclamato
            Map<Long, Long> expectedCounts = cancellazioneLogicaDao
                    .getExpectedRecordCountsForBatch(udBatch);

            // FASE 3: Elabora i risultati e aggiorna lo stato delle UD
            List<Long> matchingIds = new ArrayList<>();
            List<Long> nonMatchingIds = new ArrayList<>();

            for (Long idUnitaDoc : udBatch) {
                Long expected = expectedCounts.get(idUnitaDoc);
                Long actual = actualCounts.get(idUnitaDoc);

                if (expected.equals(actual)) {
                    matchingIds.add(idUnitaDoc);
                    log.debug("Conteggi coincidono per UD {}: {} record", idUnitaDoc, actual);
                } else {
                    nonMatchingIds.add(idUnitaDoc);
                    log.debug("Conteggi non coincidono per UD {}: attesi {}, trovati {}",
                            idUnitaDoc, expected, actual);
                }
            }

            // Aggiorna gli stati in batch
            if (!matchingIds.isEmpty()) {
                cancellazioneLogicaDao.updateUnitaDocToCancellabileBatch(matchingIds);
            }

            if (!nonMatchingIds.isEmpty()) {
                cancellazioneLogicaDao.resetUnitaDocToDaCancellareBatch(nonMatchingIds);
            }

            log.info("Elaborate {} UD in batch, di cui {} aggiornate a CANCELLABILE",
                    udBatch.size(), matchingIds.size());
        } catch (Exception e) {
            log.error("Errore nel job di verifica records Kafka: {}",
                    ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void processItemById(Long idItem) throws AppGenericPersistenceException {
        AroItemRichSoftDelete item = entityManager.find(AroItemRichSoftDelete.class, idItem);

        processUnitaDocItems(item, item.getAroRichSoftDelete());
    }

    /**
     * Processamento per unità documentarie
     */
    private void processUnitaDocItems(AroItemRichSoftDelete item,
            AroRichSoftDelete richiestaCancellazione) throws AppGenericPersistenceException {

        // Verifica che l'istanza corrente sia quella che ha reclamato l'item
        if (!instanceId.equals(item.getCdInstanceId())) {
            log.warn("Item {} reclamato da un'altra istanza: {} (questa istanza: {})",
                    item.getIdItemRichSoftDelete(), item.getCdInstanceId(), instanceId);
            return;
        }

        log.debug("{} AroItemRichSoftDelete con idItemRichSoftDelete={}",
                LOG_MESSAGE_SOFT_DELETE_UD, item.getIdItemRichSoftDelete());
        log.debug("{} Procedo alla cancellazione logica di idUnitaDoc={}",
                LOG_MESSAGE_SOFT_DELETE_UD, item.getAroUnitaDoc().getIdUnitaDoc());

        softDeleteByItem(item,
                SoftDeleteMode.valueOf(richiestaCancellazione.getTiModCancellazione()));
    }

    /**
     * Marca un item come fallito
     */
    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public void markItemAsFailed(Long itemId, String errorMessage)
            throws AppGenericPersistenceException {
        try {
            AroItemRichSoftDelete item = entityManager.find(AroItemRichSoftDelete.class, itemId);

            if (item != null) {
                item.setTiStatoItem(StatoItemRichSoftDelete.ERRORE_ELABORAZIONE.name());
                item.setDtFineElab(LocalDateTime.now());
                item.setCdErrMsg(errorMessage); // TODO: Memorizzare errore nella
                // ARO_ERR_RICH_SOFT_DELETE
                entityManager.merge(item);
            }
        } catch (Exception e) {
            log.error("Errore durante la marcatura dell'item {} come fallito: {}", itemId,
                    ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    private String generaRisposta(RispostaWSInvioRichiestaCancellazioneLogica rispostaWs,
            Long numeroItems, Long numeroItemsNonCancellabili, AroRichSoftDelete richSoftDelete)
            throws IndexOutOfBoundsException, ValidationException, JAXBException,
            AppGenericPersistenceException {
        // Preparo la risposta
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesta()
                .setNumeroRichiesteDiCancellazione(
                        numeroItems != null ? BigInteger.valueOf(numeroItems) : null);
        rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesta()
                .setNumeroRichiesteDiCancellazioneNonElaborabili(numeroItemsNonCancellabili != null
                        ? BigInteger.valueOf(numeroItemsNonCancellabili)
                        : null);

        String tipoitem;
        if (CostantiDB.TiRichSoftDelete.ANNULLAMENTO.name()
                .equals(richSoftDelete.getTiRichSoftDelete())) {
            tipoitem = CostantiDB.TiItemRichSoftDelete.ANNUL_VERS.name();
        } else if (CostantiDB.TiRichSoftDelete.RESTITUZIONE.name()
                .equals(richSoftDelete.getTiRichSoftDelete())) {
            tipoitem = CostantiDB.TiItemRichSoftDelete.REST_ARCH.name();
        } else if (CostantiDB.TiRichSoftDelete.SCARTO.name()
                .equals(richSoftDelete.getTiRichSoftDelete())) {
            tipoitem = CostantiDB.TiItemRichSoftDelete.SCARTO_ARCH.name();
        } else {
            tipoitem = CostantiDB.TiItemRichSoftDelete.UNI_DOC.name();
        }
        // Determino gli item della richiesta tramite vista
        List<AroVLisItemRichSoftDelete> itemList = cancellazioneLogicaDao
                .getAroVLisItemRichSoftDelete(new BigDecimal(richSoftDelete.getIdRichSoftDelete()),
                        tipoitem);
        if (!itemList.isEmpty()) {
            rispostaWs.getEsitoRichiestaCancellazioneLogica().setRichiesteDiCancellazione(
                    new EsitoRichiestaCancellazioneLogica.RichiesteDiCancellazione());
            it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoCancellazioneType tiCancellazione = richSoftDelete
                    .getTiModCancellazione()
                    .equals(it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoCancellazioneType.CAMPIONE
                            .value())
                                    ? it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoCancellazioneType.CAMPIONE
                                    : it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoCancellazioneType.COMPLETA;
            rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesta()
                    .setTipoCancellazione(tiCancellazione);

            for (AroVLisItemRichSoftDelete item : itemList) {
                // Creo il record della richiesta da elaborare da dare in risposta
                ECRichiestaDiCancellazioneType richiestaDiCancellazione = new ECRichiestaDiCancellazioneType();
                if (item.getTiItemRichSoftDelete()
                        .equals(CostantiDB.TiItemRichSoftDelete.UNI_DOC.name())) {
                    richiestaDiCancellazione.setTipoRichiesta(
                            it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoRichiestaType.UNITA_DOCUMENTARIA);
                    //
                    richiestaDiCancellazione.setTipoRegistro(item.getCdRegistroKeyUnitaDoc());
                    richiestaDiCancellazione.setAnno(item.getAaKeyUnitaDoc().intValue());
                    richiestaDiCancellazione.setNumero(item.getCdKeyUnitaDoc());
                } else if (item.getTiItemRichSoftDelete()
                        .equals(CostantiDB.TiItemRichSoftDelete.ANNUL_VERS.name())) {
                    richiestaDiCancellazione.setTipoRichiesta(
                            it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoRichiestaType.ANNULLAMENTO_VERSAMENTO);
                    //
                    richiestaDiCancellazione
                            .setIDRichiestaSacer(item.getIdRichiestaSacer().toBigInteger());
                } else if (item.getTiItemRichSoftDelete()
                        .equals(CostantiDB.TiItemRichSoftDelete.SCARTO_ARCH.name())) {
                    richiestaDiCancellazione.setTipoRichiesta(
                            it.eng.parer.ws.xml.esitoRichSoftDelete.ECTipoRichiestaType.SCARTO_ARCHIVISTICO);
                    //
                    richiestaDiCancellazione
                            .setIDRichiestaSacer(item.getIdRichiestaSacer().toBigInteger());
                }

                richiestaDiCancellazione.setStato(item.getTiStatoItem());
                richiestaDiCancellazione.setErroriRilevati(item.getDsListaErr());
                rispostaWs.getEsitoRichiestaCancellazioneLogica().getRichiesteDiCancellazione()
                        .getRichiestaDiCancellazione().add(richiestaDiCancellazione);
            }
        }

        log.info(
                "{} --- Registro su DB gli XML di richiesta ed esito cancellazione logica unità documentarie",
                InvioRichiestaCancellazioneLogicaExt.class.getSimpleName());
        // Registro l'XML di risposta
        return marshallaEsitoRichiestaCancellazioneLogica(
                rispostaWs.getEsitoRichiestaCancellazioneLogica());
    }

    private String marshallaEsitoRichiestaCancellazioneLogica(
            EsitoRichiestaCancellazioneLogica esito) throws JAXBException {
        StringWriter sw = new StringWriter();
        Marshaller marshaller = xmlSoftDeleteCache.getEsitoRichSoftDeleteCtx().createMarshaller();
        marshaller.marshal(esito, sw);
        return sw.toString();
    }

    @Override
    public void softDeleteByItem(AroItemRichSoftDelete item, SoftDeleteMode mode)
            throws AppGenericPersistenceException {
        cancellazioneLogicaDao.softDeleteBottomUp(AroUnitaDoc.class,
                item.getAroUnitaDoc().getIdUnitaDoc(), mode, item);
    }

}
