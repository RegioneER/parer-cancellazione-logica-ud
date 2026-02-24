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

package it.eng.parer.soft.delete.runner.rest;

import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_API_BASE;
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_OAUTH_2_SOFT_DELETE;
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_PUBLIC_SOFT_DELETE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.common.annotation.Blocking;
import io.vertx.core.http.HttpServerRequest;
import it.eng.parer.soft.delete.runner.rest.input.CancellazioneLogicaStdMultipartForm;
import it.eng.parer.soft.delete.beans.ICancellazioneLogicaService;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import it.eng.parer.soft.delete.runner.rest.input.CancellazioneLogicaOauth2MultipartForm;
import it.eng.parer.soft.delete.runner.util.RequestPrsr;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.BlockingFakeSession;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS.SeverityEnum;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Tag(name = "Cancellazione logica Unità Documentarie", description = "Servizio di cancellazione logica delle Unità Documentarie")
@SecurityScheme(securitySchemeName = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer")
@RequestScoped
@Path(URL_API_BASE)
public class CancellazioneLogicaEndpoint {

    /* constants */
    private static final String ETAG = "cancellazione-logica-ud-v1.0";

    /* interfaces */
    private final ICancellazioneLogicaService cancellazioneLogicaService;
    private final SecurityContext securityCtx;
    private final HttpServerRequest request;

    private RispostaWSInvioRichiestaCancellazioneLogica rispostaWs;
    private InvioRichiestaCancellazioneLogicaExt richCancellazioneLogicaExt;
    private BlockingFakeSession blockingFakeSession;
    private AvanzamentoWs avanzamentoWs;

    @ConfigProperty(name = "quarkus.uuid")
    String instanceUUID;

    @Inject
    public CancellazioneLogicaEndpoint(ICancellazioneLogicaService cancellazioneLogicaService,
            SecurityContext securityCtx, HttpServerRequest request) {
        this.cancellazioneLogicaService = cancellazioneLogicaService;
        this.securityCtx = securityCtx;
        this.request = request;
    }

    @Operation(summary = "Cancellazione logica Unità Documentarie (con OAuth2)", description = "Cancellazione logica Unità Documentarie autenticata con token OAuth2")
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Cancellazione logica ud effettuata con successo", content = @Content(mediaType = "application/xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))),
            @APIResponse(responseCode = "400", description = "Richiesta non valida (XML non valido, errore di validazione con xsd)", content = @Content(mediaType = "application/problem+xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))),
            @APIResponse(responseCode = "401", description = "Autenticazione fallita"),
            @APIResponse(responseCode = "403", description = "Non autorizzato ad accedere al servizio"),
            @APIResponse(responseCode = "500", description = "Errore generico (richiesta non valida secondo specifiche)", content = @Content(mediaType = "application/problem+xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))) })
    @POST
    @Path(URL_OAUTH_2_SOFT_DELETE)
    @Produces("application/xml; charset=UTF-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Response oauth2softdelete(@Valid CancellazioneLogicaOauth2MultipartForm formData) {

        // init
        LocalDateTime start = init();
        // do something .....
        Object results = doCancellazioneLogica(start, null, formData);
        //
        return Response.ok(results)
                .lastModified(
                        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .tag(new EntityTag(ETAG)).build();
    }

    @Operation(summary = "Cancellazione logica Unità Documentarie (pubblico)", description = "Cancellazione logica Unità Documentarie pubblico, meccanismi di sicurezza standard")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Cancellazione logica ud effettuata con successo", content = @Content(mediaType = "application/xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))),
            @APIResponse(responseCode = "400", description = "Richiesta non valida (XML non valido, errore di validazione con xsd)", content = @Content(mediaType = "application/problem+xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))),
            @APIResponse(responseCode = "500", description = "Errore generico (richiesta non valida secondo specifiche)", content = @Content(mediaType = "application/problem+xml", schema = @Schema(implementation = EsitoRichiestaCancellazioneLogica.class))) })
    @POST
    @Path(URL_PUBLIC_SOFT_DELETE)
    @Produces("application/xml; charset=UTF-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Response publicsoftdelete(@Valid CancellazioneLogicaStdMultipartForm formData) {

        // init
        LocalDateTime start = init();
        // do something .....
        EsitoRichiestaCancellazioneLogica results = doCancellazioneLogica(start, formData, null);
        //
        return Response.ok(results)
                .lastModified(
                        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .tag(new EntityTag(ETAG)).build();
    }

    private EsitoRichiestaCancellazioneLogica doCancellazioneLogica(LocalDateTime start,
            CancellazioneLogicaStdMultipartForm unsecuredFormData,
            CancellazioneLogicaOauth2MultipartForm securedformData) {

        // blocco try/catch per gestire qualunque eccezione runtime non prevista dalla
        // logica
        // il provider provvederà a fornire una risposta 666-standard
        try {
            EsitoRichiestaCancellazioneLogica myEsito = rispostaWs
                    .getEsitoRichiestaCancellazioneLogica();

            if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.OK) {

                avanzamentoWs.setFase("inzio logica di cancellazione ud (soft delete)")
                        .logAvanzamento();

                avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.TRASFERIMENTO_PAYLOAD_IN)
                        .setFase("pronto a ricevere").logAvanzamento();
                // validazione formale dell'oggetta multipart
                // effettuta per validare la presenza dei campi (==null)
                if (unsecuredFormData != null) {
                    RequestPrsr.parseStdForm(blockingFakeSession, avanzamentoWs, unsecuredFormData);
                } else {
                    RequestPrsr.parseOAuth2Form(blockingFakeSession, avanzamentoWs, securedformData,
                            securityCtx.getUserPrincipal());
                }
                //
                avanzamentoWs
                        .setCheckPoint(AvanzamentoWs.CheckPoints.VERIFICA_STRUTTURA_CHIAMATA_WS)
                        .setFase("completata").logAvanzamento();

                /*
                 * ***************************************************************** ************
                 * fine della verifica della struttura/signature del web service. Verifica dei dati
                 * effettivamente versati
                 * ***************************************************************** ************
                 * ***
                 */
                /*
                 * Logica di verifica della versione SIP
                 */
                if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                    avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.VERIFICA_SEMANTICA)
                            .setFase("verifica versione").logAvanzamento();

                    cancellazioneLogicaService.verificaVersione(blockingFakeSession.getVersioneWS(),
                            rispostaWs, richCancellazioneLogicaExt);
                }

                /*
                 * Logica condivisa tra verifica "standard" e con token (OAut2+...futuri
                 * improvements)
                 */
                if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                    avanzamentoWs.setFase("verifica credenziali").logAvanzamento();
                    cancellazioneLogicaService.verificaCredenziali(
                            blockingFakeSession.getLoginName(), blockingFakeSession.getPassword(),
                            blockingFakeSession.getIpChiamante(), rispostaWs,
                            richCancellazioneLogicaExt);
                }

                // verifica formale e semantica dell'XML di versamento
                if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                    avanzamentoWs.setFase("verifica xml").logAvanzamento();
                    cancellazioneLogicaService.parseXML(blockingFakeSession.getDatiRequestXml(),
                            rispostaWs, richCancellazioneLogicaExt);
                }

                /* PREPARA LA RISPOSTA */
                if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
                    avanzamentoWs.setFase("generazione xml").logAvanzamento();
                    Long idRichiesta = cancellazioneLogicaService
                            .esaminaRichiesteCancellazioneLogica(rispostaWs,
                                    richCancellazioneLogicaExt);
                    blockingFakeSession.setIdRichiestaCancellazioneLogica(idRichiesta);
                }

                /* ELABORAZIONE ASINCRONA */
                if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
                    avanzamentoWs.setFase("elaborazione asincrona").logAvanzamento();
                    // se l'id della richiesta è stato generato, allora la
                    // richiesta è stata
                    // registrata
                    // Avvia l'elaborazione asincrona
                    cancellazioneLogicaService.avviaElaborazioneAsincrona(
                            blockingFakeSession.getIdRichiestaCancellazioneLogica(),
                            richCancellazioneLogicaExt,
                            richCancellazioneLogicaExt.getUtente().getIdUtente());
                }

                blockingFakeSession.setTmChiusura(ZonedDateTime.now());
            }

            myEsito = rispostaWs.getEsitoRichiestaCancellazioneLogica();

            long totalTime = Duration.between(start, LocalDateTime.now()).toMillis();
            avanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.FINE).setFase("")
                    .setTotalTime(totalTime).logAvanzamento(true);

            return myEsito;
        } catch (Exception e) {
            throw new AppGenericRuntimeException(
                    "Errore generico in fase di versamento CancellazioneLogicaEndpoint.doCancellazioneLogica",
                    e, ErrorCategory.INTERNAL_ERROR);
        }

    }

    private LocalDateTime init() {
        // blocco try/catch per gestire qualunque eccezione runtime non prevista dalla
        // logica
        // il provider provvederà a fornire una risposta 666-standard
        try {
            final LocalDateTime start = LocalDateTime.now();

            // init
            rispostaWs = new RispostaWSInvioRichiestaCancellazioneLogica();
            richCancellazioneLogicaExt = new InvioRichiestaCancellazioneLogicaExt();
            blockingFakeSession = new BlockingFakeSession();

            // init
            avanzamentoWs = cancellazioneLogicaService.init(rispostaWs, richCancellazioneLogicaExt);
            // visual log
            avanzamentoWs.logAvanzamento(true);
            //
            blockingFakeSession.setTmApertura(ZonedDateTime.now());
            blockingFakeSession.setIpChiamante(RequestPrsr.leggiIpVersante(request, avanzamentoWs));

            return start;
        } catch (Exception e) {
            throw new AppGenericRuntimeException(
                    "Errore generico in fase di versamento CancellazioneLogicaEndpoint.init", e,
                    ErrorCategory.INTERNAL_ERROR);
        }
    }

}
