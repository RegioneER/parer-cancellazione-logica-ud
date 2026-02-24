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
package it.eng.parer.soft.delete.runner.qute;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import it.eng.parer.soft.delete.runner.qute.model.ApplicationInfo;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("welcome")
public class Welcome {

    Template welcome;

    ApplicationInfo appinfo;

    @Inject
    public Welcome(Template welcome, ApplicationInfo appinfo) {
        this.welcome = welcome;
        this.appinfo = appinfo;
    }

    @Operation(hidden = true)
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return welcome.data("appinfo", appinfo);
    }

}
