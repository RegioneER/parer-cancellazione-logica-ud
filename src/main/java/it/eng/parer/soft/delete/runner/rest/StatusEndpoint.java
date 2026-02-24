/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.runner.rest;

import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_GET_STATUS;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/*
 * Endpoint introdotto secondo specifiche standard di pubblicazione WSO2 (ApiManager) (vedi
 * https://italia.github.io/api-oas-checker). Espone lo stesso contenuto dell'API standard
 * /q/health/live (vedi https://quarkus.io/guides/smallrye-health)
 */
@Tag(name = "Stato applicazione", description = "Stato dell'applicazione")
@SecurityScheme(securitySchemeName = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@RequestScoped
@Path(URL_GET_STATUS)
public class StatusEndpoint {

    private final SmallRyeHealthReporter reporter;

    public StatusEndpoint(SmallRyeHealthReporter reporter) {
        this.reporter = reporter;
    }

    @Operation(summary = "Stato applicazione", description = "Stato applicazione")
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Stato applicazione", content = @Content(mediaType = "application/problem+json")),
            @APIResponse(responseCode = "500", description = "Errore interno"),
            @APIResponse(responseCode = "503", description = "Servizio non disponibile") })
    @GET
    @Produces("application/problem+json")
    @Blocking
    public Response status() {
        // Get liveness payload
        return Response.ok(reporter.getLiveness().getPayload())
                .lastModified(
                        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .build();
    }
}
