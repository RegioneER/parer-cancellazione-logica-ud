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
package it.eng.parer.soft.delete.runner.util;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import it.eng.parer.soft.delete.beans.dto.base.BlockingFakeSession;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.runner.rest.input.CancellazioneLogicaOauth2MultipartForm;
import it.eng.parer.soft.delete.runner.rest.input.CancellazioneLogicaStdMultipartForm;

/**
 * Parser di request di tipo POST/MULTIPART FORM DATA
 *
 */
public class RequestPrsr {

    private static final Logger log = LoggerFactory.getLogger(RequestPrsr.class);

    private RequestPrsr() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Revisited: il metodo si limita a impostare le property dell'oggetto
     * {@link BlockingFakeSession} recepecita dal wrapper del multipart versato
     * {@link VersamentoFascicoloStdMultipartForm}
     *
     * @param syncFakeSession oggetto di sessione
     * @param avanzamentoWs   oggetto che traccia l'avanzamento logico dell'endpoint
     * @param formData        oggetto multipart/form-data generico
     *
     */
    public static void parseStdForm(BlockingFakeSession syncFakeSession,
            AvanzamentoWs avanzamentoWs, CancellazioneLogicaStdMultipartForm formData) {

        avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.TRASFERIMENTO_PAYLOAD_IN)
                .setFase("Payload verificato ricevuto").logAvanzamento();

        /*
         * verifica della struttura della chiamata al WS: non è un WS SOAP perciò la signature del
         * WS va controllata a mano, leggendo quanto effettivamente versato.
         */
        // verifica strutturale del campo VERSIONE e memorizzazione dello stesso nella
        // sessione finta

        String versione = formData.VERSIONE;
        log.debug("VERSIONE {}", versione);
        syncFakeSession.setVersioneWS(versione);

        // verifica strutturale del campo LOGINNAME e memorizzazione dello stesso nella
        // sessione finta
        String loginame = formData.LOGINNAME;
        log.debug("LOGINNAME {}", loginame);
        syncFakeSession.setLoginName(loginame);

        // verifica strutturale del campo PASSWORD e memorizzazione dello stesso nella
        // sessione finta
        String pwd = formData.PASSWORD;
        log.debug("PASSWORD {}", pwd);
        syncFakeSession.setPassword(pwd);

        // verifica strutturale del campo XMLREQ e memorizzazione dello stesso nella
        // sessione finta
        String xmlreq = formData.XMLREQ;
        log.debug("XMLREQ {}", xmlreq);
        syncFakeSession.setDatiRequestXml(xmlreq);
        syncFakeSession.setDatiDaSalvareRequest(xmlreq);

        // indice sip (xml serialized)
        syncFakeSession.setRichiestaCancellazioneLogica(formData.richiestaCancellazioneLogica);
    }

    public static void parseOAuth2Form(BlockingFakeSession syncFakeSession,
            AvanzamentoWs avanzamentoWs, CancellazioneLogicaOauth2MultipartForm formData,
            Principal principal) {

        avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.TRASFERIMENTO_PAYLOAD_IN)
                .setFase("Payload verificato ricevuto").logAvanzamento();

        /*
         * verifica della struttura della chiamata al WS: non è un WS SOAP perciò la signature del
         * WS va controllata a mano, leggendo quanto effettivamente versato.
         */
        // verifica strutturale del campo VERSIONE e memorizzazione dello stesso nella
        // sessione finta

        String versione = formData.VERSIONE;
        log.debug("VERSIONE {}", versione);
        syncFakeSession.setVersioneWS(versione);

        // verifica strutturale del campo LOGINNAME e memorizzazione dello stesso nella
        // sessione finta
        String loginame = principal.getName();
        log.debug("OAUTH2 LOGINNAME {}", loginame);
        syncFakeSession.setLoginName(loginame);

        // verifica strutturale del campo XMLREQ e memorizzazione dello stesso nella
        // sessione finta
        String xmlreq = formData.XMLREQ;
        log.debug("XMLREQ {}", xmlreq);
        syncFakeSession.setDatiRequestXml(xmlreq);
        syncFakeSession.setDatiDaSalvareRequest(xmlreq);

        // indice sip (xml serialized)
        syncFakeSession.setRichiestaCancellazioneLogica(formData.richCancellazioneLogica);
    }

    /**
     * lettura dell'indirizzo IP del chiamante. Si presuppone che il load balancer o il reverse
     * proxy impostino la variabile RERFwFor tra gli header HTTP della request. Questo è un tag
     * custom messo a punto dalla RER per compensare ai possibili rischi legati all'uso dell'header
     * X-FORWARDED-FOR di uso più comune. Da notare che qualora l'header RERFwFor non fosse
     * valorizzato, il codice ripiegherà cercando X-FORWARDED-FOR tra gli header HTTP della request.
     * Questo è l'unico sistema per recepire l'IP nel caso in cui l'application server non sia
     * esposto direttamente. NOTA: è ovvio che l'application server è esposto direttamente solo sui
     * PC di sviluppo.
     *
     * @param request       standard {@link HttpServerRequest}
     * @param avanzamentoWs avanzamento ws
     *
     * @return ip client letto ottenuto da request
     */
    public static String leggiIpVersante(HttpServerRequest request, AvanzamentoWs avanzamentoWs) {
        String ipVers = request.getHeader("RERFwFor");
        // cerco l'header custom della RER
        if (ipVers == null || ipVers.isEmpty()) {
            ipVers = request.getHeader("X-FORWARDED-FOR");
            // se non c'e`, uso l'header standard
        }
        if (ipVers == null || ipVers.isEmpty()) {
            ipVers = request.remoteAddress().hostAddress();
            // se non c'e` perche' la macchina e' esposta direttamente,
            // leggo l'IP fisico del chiamante
        }
        log.debug("Request, indirizzo di provenienza - IP: {}", ipVers);
        avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.LETTURA_IP_VERSANTE)
                .setFase("Lettura Ip Versante da header chiamata").setIndirizzoIp(ipVers)
                .logAvanzamento(true);
        return ipVers;
    }

}
