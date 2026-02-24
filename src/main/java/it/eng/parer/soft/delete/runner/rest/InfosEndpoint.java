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

import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_ADMIN_BASE;
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.RESOURCE_INFOS;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.common.annotation.NonBlocking;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Tag(name = "Informazioni applicazione", description = "Informazioni applicazione")
@SecurityScheme(securitySchemeName = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer")
@SecurityScheme(securitySchemeName = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@RequestScoped
@Path(URL_ADMIN_BASE)
public class InfosEndpoint {

    /* constants */
    private static final String ETAG = "v1.0";
    private static final String QUARKUS_PROFILE_PROPS = "%";
    private static final String MAP_GIT = "git";
    private static final String MAP_QUARKUS = "quarkus";
    private static final String MAP_SEC = "security";
    private static final String MAP_OTHERS = "others";

    /* interfaces */
    private final SecurityContext securityCtx;

    Properties gitproperties = null;

    @ConfigProperty(name = "parer.soft.delete.env.propstoskip", defaultValue = " ")
    String propstoskip;

    @ConfigProperty(name = "parer.soft.delete.env.roottoskip", defaultValue = " ")
    String rootToSkip;

    private Map<String, Map<String, String>> infos = Collections
            .synchronizedMap(new LinkedHashMap<>());

    @Inject
    public InfosEndpoint(SecurityContext securityCtx) {
        this.securityCtx = securityCtx;
    }

    @PostConstruct
    public void init() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
            gitproperties = new Properties();
            // load a properties file
            gitproperties.load(input);
        }
    }

    @Operation(summary = "App infos", description = "App infos")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "basicAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Informazioni Applicazione", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "401", description = "Non autenticato"),
            @APIResponse(responseCode = "403", description = "Non autorizzato") })
    @GET
    @Path(RESOURCE_INFOS)
    @Produces(MediaType.APPLICATION_JSON)
    @NonBlocking
    public Response infos() {

        // infos
        // git
        Map<String, String> git = gitproperties.entrySet().stream()
                .filter(propName -> !String.valueOf(propName.getKey()).matches(propstoskip))
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new));

        if (!MAP_GIT.matches(rootToSkip)) {
            infos.put(MAP_GIT, git);
        }
        // quarkus
        Map<String, String> quarkus = Collections.synchronizedMap(new LinkedHashMap<>());
        StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                .sorted().filter(propName -> !propName.startsWith(QUARKUS_PROFILE_PROPS))
                .filter(propName -> propName.startsWith(MAP_QUARKUS))
                .filter(propName -> !propName.matches(propstoskip))
                .forEach(propName -> quarkus.put(propName,
                        ConfigProvider.getConfig().getConfigValue(propName).getValue()));

        if (!MAP_QUARKUS.matches(rootToSkip)) {
            infos.put(MAP_QUARKUS, quarkus);
        }

        // security infos
        Principal caller = securityCtx.getUserPrincipal();
        String name = caller == null ? "anonymous" : caller.getName();
        String security = String.format("hello %s, isSecure: %s, authScheme: %s", name,
                securityCtx.isSecure(), securityCtx.getAuthenticationScheme());

        if (!MAP_SEC.matches(rootToSkip)) {
            infos.put(MAP_SEC, Map.of("is_secure", security));
        }

        // others
        List<String> allcurrkeys = infos.values().stream().map(Map::keySet)
                .flatMap(Collection::stream).toList();
        Map<String, String> others = Collections.synchronizedMap(new LinkedHashMap<>());
        StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                .sorted().filter(propName -> !propName.startsWith(QUARKUS_PROFILE_PROPS))
                .filter(propName -> !allcurrkeys.contains(propName))
                .filter(propName -> !propName.matches(propstoskip))
                .forEach(propName -> others.put(propName,
                        ConfigProvider.getConfig().getConfigValue(propName).getValue()));

        if (!MAP_OTHERS.matches(rootToSkip)) {
            infos.put(MAP_OTHERS, others);
        }

        // preconditions are OK
        return Response.ok(infos)
                .lastModified(
                        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .tag(new EntityTag(ETAG)).build();
    }

}
