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
package it.eng.parer.soft.delete.runner.providers;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaService;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.BlockingFakeSession;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.VoceDiErrore;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.soft.delete.runner.util.ICancellazioneLogicaMultipartForm;
import it.eng.parer.soft.delete.runner.util.RequestPrsr;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/*
 * ExceptionMapper che gestisce l'http 404 qualora venga richiamata una risorsa non esistente
 * (risposta vuota con il relativo http error code)
 */
@Provider
public class ConstraintViolationExceptionMapperProvider
        implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory
            .getLogger(ConstraintViolationExceptionMapperProvider.class);

    ICancellazioneLogicaService cancellazioneLogicaService;

    @Context
    HttpServerRequest request;

    private RispostaWSInvioRichiestaCancellazioneLogica rispostaWS;
    private InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt;
    private BlockingFakeSession blockingFakeSession;

    @Inject
    public ConstraintViolationExceptionMapperProvider(
            ICancellazioneLogicaService cancellazioneLogicaService) {
        this.cancellazioneLogicaService = cancellazioneLogicaService;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // log
        log.atError().log("ConstraintViolationExceptionMapperProvider errore registrato",
                exception);
        // init
        AvanzamentoWs avanzamentoWs = init();
        // esito
        EsitoRichiestaCancellazioneLogica myEsito = rispostaWS
                .getEsitoRichiestaCancellazioneLogica();

        // check possible error after init
        if (rispostaWS.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
            cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY,
                    myEsito.getEsitoRichiesta().getCodiceErrore(),
                    myEsito.getEsitoRichiesta().getMessaggioErrore());
        }

        // default
        rispostaWS.setErrorType(IRispostaWS.ErrorTypeEnum.WS_SIGNATURE);

        // check violation errors
        exception.getConstraintViolations().forEach(c -> {
            if (c.getLeafBean() instanceof ICancellazioneLogicaMultipartForm) {
                cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY, MessaggiWSBundle.WS_CHECK,
                        c.getMessage());
            } else {
                // default
                cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY, MessaggiWSBundle.ERR_666,
                        c.getMessage());
            }
        });

        // calcolo errore principale
        VoceDiErrore tmpVdE = cancellazioneLogicaExt.calcolaErrorePrincipale();
        rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        rispostaWS.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
        rispostaWS.setErrorMessage(
                MessaggiWSBundle.getString(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage()));

        // gestione sessione
        blockingFakeSession.setTmApertura(ZonedDateTime.now());
        blockingFakeSession.setIpChiamante(RequestPrsr.leggiIpVersante(request, avanzamentoWs));
        blockingFakeSession.setTmChiusura(ZonedDateTime.now());

        avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.VERIFICA_STRUTTURA_CHIAMATA_WS)
                .setFase("completata").logAvanzamento();

        return Response.status(Response.Status.BAD_REQUEST).entity(myEsito).build();
    }

    /**
     *
     */
    private AvanzamentoWs init() {
        rispostaWS = new RispostaWSInvioRichiestaCancellazioneLogica();
        cancellazioneLogicaExt = new InvioRichiestaCancellazioneLogicaExt();
        blockingFakeSession = new BlockingFakeSession();

        // init
        AvanzamentoWs avanzamentoWs = cancellazioneLogicaService.init(rispostaWS,
                cancellazioneLogicaExt);
        avanzamentoWs.logAvanzamento(true);
        return avanzamentoWs;
    }

}
