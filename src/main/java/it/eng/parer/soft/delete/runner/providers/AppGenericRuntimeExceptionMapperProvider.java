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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaService;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.VoceDiErrore;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.exceptions.xml.IXmlReqValidationException;
import it.eng.parer.soft.delete.beans.exceptions.xml.XmlReqNotWellFormedException;
import it.eng.parer.soft.delete.beans.exceptions.xml.XmlReqUnmarshalException;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.soft.delete.beans.utils.xml.XmlDateUtility;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/*
 * ExceptionMapper che gestisce gli errori di tipoo WebApplicationException, nel caso specifico si
 * deve verificare la logica del provider CustomJaxbMessageBodyReader il quale, come da default,
 * utilizza questo tipo di eccezioni.
 */
@Provider
public class AppGenericRuntimeExceptionMapperProvider
        implements ExceptionMapper<AppGenericRuntimeException> {

    private static final Logger log = LoggerFactory
            .getLogger(AppGenericRuntimeExceptionMapperProvider.class);

    ICancellazioneLogicaService cancellazioneLogicaService;

    @Context
    HttpServerRequest request;

    private RispostaWSInvioRichiestaCancellazioneLogica rispostaWS;
    private InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt;

    @Inject
    public AppGenericRuntimeExceptionMapperProvider(
            ICancellazioneLogicaService cancellazioneLogicaService) {
        this.cancellazioneLogicaService = cancellazioneLogicaService;
    }

    @Override
    public Response toResponse(AppGenericRuntimeException exception) {
        // log
        log.atError().log("AppGenericRuntimeExceptionMapperProvider errore registrato", exception);

        // default response status
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

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

        // xml not well formed
        if (exception.getCause() instanceof XmlReqNotWellFormedException) {
            // status
            status = Response.Status.BAD_REQUEST;
            //
            cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY, MessaggiWSBundle.XSD_001_001,
                    MessaggiWSBundle.getString(MessaggiWSBundle.XSD_001_001,
                            ExceptionUtils.getRootCauseMessage(exception)));

        } else if (exception.getCause() instanceof XmlReqUnmarshalException) {
            // status
            status = Response.Status.BAD_REQUEST;
            //
            cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY, MessaggiWSBundle.XSD_001_002,
                    MessaggiWSBundle.getString(MessaggiWSBundle.XSD_001_002,
                            ExceptionUtils.getRootCauseMessage(exception)));
        } else {
            // default
            cancellazioneLogicaExt.listErrAddError(StringUtils.EMPTY, MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            ExceptionUtils.getRootCauseMessage(exception)));

        }

        // calcolo errore principale
        VoceDiErrore tmpVdE = cancellazioneLogicaExt.calcolaErrorePrincipale();
        rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        rispostaWS.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
        rispostaWS.setErrorMessage(
                MessaggiWSBundle.getString(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage()));

        // xml del req recuperato dalla custom exception
        if (exception.getCause() instanceof IXmlReqValidationException) {
            String xmlReq = ((IXmlReqValidationException) exception.getCause()).getXmlReq();
            cancellazioneLogicaExt.setXmlRichiesta(xmlReq);
        } else {
            cancellazioneLogicaExt.setXmlRichiesta(StringUtils.EMPTY);
        }

        cancellazioneLogicaExt.setDataElaborazione(
                XmlDateUtility.xmlGregorianCalendarToDateOrNull(myEsito.getDataRichiesta()));

        avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.VERIFICA_XML).setFase("completata")
                .logAvanzamento();

        return Response.status(status).entity(myEsito).build();
    }

    /**
     *
     */
    private AvanzamentoWs init() {
        rispostaWS = new RispostaWSInvioRichiestaCancellazioneLogica();
        cancellazioneLogicaExt = new InvioRichiestaCancellazioneLogicaExt();

        // init
        AvanzamentoWs avanzamentoWs = cancellazioneLogicaService.init(rispostaWS,
                cancellazioneLogicaExt);
        avanzamentoWs.logAvanzamento(true);

        return avanzamentoWs;
    }

}
