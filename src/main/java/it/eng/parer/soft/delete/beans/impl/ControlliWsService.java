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
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.impl;

import static it.eng.parer.soft.delete.beans.utils.converter.DateUtilsConverter.convert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IControlliSemanticiService;
import it.eng.parer.soft.delete.beans.IControlliWsService;
import it.eng.parer.soft.delete.beans.IWsIdpLoggerService;
import it.eng.parer.soft.delete.beans.dto.base.IWSDesc;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.security.User;
import it.eng.parer.soft.delete.beans.security.auth.WSLoginHandler;
import it.eng.parer.soft.delete.beans.security.exception.AuthWSException;
import it.eng.parer.soft.delete.beans.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.soft.delete.beans.utils.Costanti.VersioneWS;
import it.eng.parer.soft.delete.beans.utils.ParametroApplDB.TipoParametroAppl;
import it.eng.parer.soft.delete.beans.utils.VerificaVersione;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.soft.delete.jpa.entity.IamUser;
import it.eng.parer.soft.delete.jpa.grantedEntity.UsrUser;
import it.eng.parer.idpjaas.logutils.LogDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ControlliWsService implements IControlliWsService {

    private static final Logger log = LoggerFactory.getLogger(ControlliWsService.class);

    private static final String LOG_AUTH_ERR = "Eccezione nella fase di autenticazione: ";
    private static final String LOG_USER_VERIFY_ERR = "Errore nella verifica delle autorizzazioni utente: ";

    IWsIdpLoggerService wsIdpLoggerService;

    IControlliSemanticiService controlliSemanticiService;

    WSLoginHandler wsLoginHandler;

    EntityManager entityManager;

    @Inject
    public ControlliWsService(EntityManager entityManager, IWsIdpLoggerService wsIdpLoggerService,
            IControlliSemanticiService controlliSemanticiService, WSLoginHandler wsLoginHandler) {
        this.entityManager = entityManager;
        this.wsIdpLoggerService = wsIdpLoggerService;
        this.controlliSemanticiService = controlliSemanticiService;
        this.wsLoginHandler = wsLoginHandler;
    }

    @Override
    @Transactional
    public RispostaControlli checkVersione(String versione, String versioniWsKey,
            Map<String, String> xmlDefaults) {
        RispostaControlli rcCheckVersione = new RispostaControlli();
        rcCheckVersione.setrBoolean(false);

        if (versione == null || versione.isEmpty()) {
            rcCheckVersione.setCodErr(MessaggiWSBundle.UD_001_010);
            rcCheckVersione.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_010));
            return rcCheckVersione;
        }

        List<String> versioniWs = VerificaVersione.getWsVersionList(versioniWsKey, xmlDefaults);
        if (versioniWs.isEmpty()) {
            rcCheckVersione.setCodErr(MessaggiWSBundle.UD_018_001);
            rcCheckVersione.setDsErr(
                    MessaggiWSBundle.getString(MessaggiWSBundle.UD_018_001, versioniWsKey));
            return rcCheckVersione;
        }

        // check if versione is supported
        rcCheckVersione
                .setrBoolean(versioniWs.contains(versione) && VersioneWS.issupported(versione));

        if (!rcCheckVersione.isrBoolean()) {
            rcCheckVersione.setCodErr(MessaggiWSBundle.UD_001_011);
            rcCheckVersione
                    .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_011, versione));
        }

        return rcCheckVersione;
    }

    @Override
    @Transactional
    public RispostaControlli checkCredenziali(String loginName, String password,
            String indirizzoIP) {
        User utente = null;
        RispostaControlli rcCheckCredenziali = new RispostaControlli();
        rcCheckCredenziali.setrBoolean(false);

        log.atDebug().log("Indirizzo IP del chiamante - access: ws - IP: {}", indirizzoIP);

        if (loginName == null || loginName.isEmpty()) {
            rcCheckCredenziali.setCodErr(MessaggiWSBundle.UD_001_004);
            rcCheckCredenziali.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_004));
            return rcCheckCredenziali;
        }

        // preparazione del log del login
        LogDto tmpLogDto = new LogDto();
        tmpLogDto.setNmAttore("Sacer WS");
        tmpLogDto.setNmUser(loginName);
        tmpLogDto.setCdIndIpClient(indirizzoIP);
        tmpLogDto.setTsEvento(convert(LocalDateTime.now()));
        // nota, non imposto l'indirizzo del server, verrà letto dal singleton da
        // WsIdpLoggerService

        try {
            // controlli std sull'utente effettuati sia con token OAuth2 che nel caso
            // di autenticazione std
            wsLoginHandler.login(loginName, password, indirizzoIP);
            // se l'autenticazione riesce, non va in eccezione.
            // passo quindi a leggere i dati dell'utente dal db
            IamUser iamUser;
            UsrUser usrUser;
            final String queryStr = "select iu, usr from IamUser iu "
                    + "join UsrUser usr on iu.idUserIam = usr.idUserIam "
                    + "where iu.nmUserid = :nmUseridIn";
            Query query = entityManager.createQuery(queryStr);
            query.setParameter("nmUseridIn", loginName);
            Object[] result = (Object[]) query.getSingleResult();
            iamUser = (IamUser) result[0]; // IamUser
            usrUser = (UsrUser) result[1]; // UsrUser
            //
            String sistemaVersante = null;
            if (usrUser.getAplSistemaVersante() != null) {
                sistemaVersante = usrUser.getAplSistemaVersante().getNmSistemaVersante();
            }
            //
            utente = new User();
            utente.setUsername(loginName);
            utente.setIdUtente(iamUser.getIdUserIam());
            utente.setSistemaVersante(sistemaVersante);
            // log della corretta autenticazione
            tmpLogDto.setTipoEvento(LogDto.TipiEvento.LOGIN_OK);
            tmpLogDto.setDsEvento("WS, login OK");
            //
            rcCheckCredenziali.setrObject(utente);
            rcCheckCredenziali.setrBoolean(true);
        } catch (AuthWSException e) {
            log.atWarn()
                    .log("ERRORE DI AUTENTICAZIONE WS." + " Funzionalità: {} Utente: {}"
                            + " Tipo errore: {} Indirizzo IP: {} Descrizione: {}",
                            TipiWSPerControlli.SOFT_DELETE.name(), loginName,
                            e.getCodiceErrore().name(), indirizzoIP, e.getDescrizioneErrore());

            if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                rcCheckCredenziali.setCodErr(MessaggiWSBundle.UD_001_006);
                rcCheckCredenziali.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                rcCheckCredenziali.setCodErr(MessaggiWSBundle.UD_001_007);
                rcCheckCredenziali.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                rcCheckCredenziali.setCodErr(MessaggiWSBundle.UD_001_012);
                rcCheckCredenziali.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_012,
                        e.getDescrizioneErrore()));
            }
            //
            // log dell'errore di autenticazione; ripeto la sequenza di if per
            // chiarezza.
            // Per altro nel caso sia stato invocato il ws di annullamento, la
            // distnizione
            // del tipo di errore non l'ho ancora eseguita.
            //
            if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                tmpLogDto.setTipoEvento(LogDto.TipiEvento.EXPIRED);
                tmpLogDto.setDsEvento("WS, " + e.getDescrizioneErrore());
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                tmpLogDto.setTipoEvento(LogDto.TipiEvento.LOCKED);
                tmpLogDto.setDsEvento("WS, " + e.getDescrizioneErrore());
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                // se l'autenticazione fallisce, devo capire se è stato sbagliata la
                // password
                // oppure
                // non esiste l'utente. Provo a caricarlo e verifico la cosa.
                final String queryStr = "select count(iu) from IamUser iu where iu.nmUserid = :nmUseridIn";
                Query query = entityManager.createQuery(queryStr);
                query.setParameter("nmUseridIn", loginName);
                long tmpNumUtenti = (Long) query.getSingleResult();
                if (tmpNumUtenti > 0) {
                    tmpLogDto.setTipoEvento(LogDto.TipiEvento.BAD_PASS);
                    tmpLogDto.setDsEvento("WS, bad password");
                } else {
                    tmpLogDto.setTipoEvento(LogDto.TipiEvento.BAD_USER);
                    tmpLogDto.setDsEvento("WS, utente sconosciuto");
                }
            }
        } catch (Exception e) {
            rcCheckCredenziali.setCodErr(MessaggiWSBundle.ERR_666);
            rcCheckCredenziali.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    LOG_AUTH_ERR + ExceptionUtils.getRootCauseMessage(e)));
            return rcCheckCredenziali;
        }

        // scrittura log
        // Nota: la scrittuwsIdpra di LogDto avviene solo dopo la logica di verifica e
        // recupero
        // di User il quale può portare anche nel blocco "catch" con relativa collezione
        // di informazioni
        // riguardanti particolari errori (vedi AuthWSException).
        // Attenzione: necessario refactoring una volta che il servizio sarà orientato
        // ai soli bearer token
        wsIdpLoggerService.scriviLog(tmpLogDto);
        //
        return rcCheckCredenziali;
    }

    @Override
    @Transactional
    public RispostaControlli checkAuthWS(User utente, IWSDesc descrizione) {
        RispostaControlli rcCheckAuthWS = new RispostaControlli();
        rcCheckAuthWS.setrBoolean(false);
        boolean checkOrgVersAuth = false;
        long numAbil = 0;
        Integer tmpIdOrganizz = utente.getIdOrganizzazioneFoglia() != null
                ? utente.getIdOrganizzazioneFoglia().intValue()
                : null;
        try {
            wsLoginHandler.checkAuthz(utente.getUsername(), tmpIdOrganizz, descrizione.getNomeWs());
            rcCheckAuthWS.setrBoolean(true);
        } catch (AuthWSException ex) {
            checkOrgVersAuth = true;
            rcCheckAuthWS.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_006);
            rcCheckAuthWS.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_006,
                    utente.getUsername()));
        } catch (Exception ex) {
            rcCheckAuthWS.setCodErr(MessaggiWSBundle.ERR_666);
            rcCheckAuthWS.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    LOG_USER_VERIFY_ERR + ex.getMessage()));
        }

        if (checkOrgVersAuth) {
            try {
                final String queryStr = "select count(org) from IamAbilOrganiz org join org.iamUser usr where "
                        + "usr.idUserIam = :idUserIamIn "
                        + "and org.idOrganizApplic = :idOrganizApplicIn";
                Query query = entityManager.createQuery(queryStr);
                query.setParameter("idUserIamIn", utente.getIdUtente());
                query.setParameter("idOrganizApplicIn", BigDecimal.valueOf(tmpIdOrganizz));
                numAbil = (Long) query.getSingleResult();
                if (numAbil > 0) {
                    rcCheckAuthWS.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                    rcCheckAuthWS.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                                    utente.getUsername(), descrizione.getNomeWs()));
                }
            } catch (Exception ex) {
                rcCheckAuthWS.setCodErr(MessaggiWSBundle.ERR_666);
                rcCheckAuthWS.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        LOG_USER_VERIFY_ERR + ex.getMessage()));
            }
        }
        return rcCheckAuthWS;
    }

    @Override
    @Transactional
    public RispostaControlli checkAuthWSNoOrg(User utente, IWSDesc descrizione) {
        RispostaControlli rcCheckAuthWSNoOrg = new RispostaControlli();
        rcCheckAuthWSNoOrg.setrBoolean(false);

        try {
            final String querString = "select count(iu) from IamUser iu "
                    + "join iu.iamAbilOrganizs iao " + "join iao.iamAutorServs ias  "
                    + "where iu.nmUserid = :nmUserid  " + "and ias.nmServizioWeb = :servizioWeb";
            Query query = entityManager.createQuery(querString);
            query.setParameter("nmUserid", utente.getUsername());
            query.setParameter("servizioWeb", descrizione.getNomeWs());
            long num = (long) query.getSingleResult();
            if (num > 0) {
                rcCheckAuthWSNoOrg.setrBoolean(true);
            } else {
                rcCheckAuthWSNoOrg.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                rcCheckAuthWSNoOrg
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                                utente.getUsername(), descrizione.getNomeWs()));
            }
        } catch (Exception e) {
            rcCheckAuthWSNoOrg.setCodErr(MessaggiWSBundle.ERR_666);
            rcCheckAuthWSNoOrg.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    LOG_AUTH_ERR + e.getMessage()));
        }

        return rcCheckAuthWSNoOrg;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public RispostaControlli loadWsVersions(IWSDesc descrizione) {
        RispostaControlli rs = controlliSemanticiService
                .caricaDefaultDaDB(TipoParametroAppl.VERSIONI_WS);
        // if positive ...
        if (rs.isrBoolean()) {
            Map<String, String> wsVersions = (Map<String, String>) rs.getrObject();
            // verify if my version exits
            if (VerificaVersione.getWsVersionList(descrizione.getNomeWs(), wsVersions).isEmpty()) {
                rs.setrBoolean(false);
                rs.setCodErr(MessaggiWSBundle.UD_018_001);
                rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_018_001,
                        VerificaVersione.elabWsKey(descrizione.getNomeWs())));
            }
        }
        return rs;
    }
}
