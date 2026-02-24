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
package it.eng.parer.soft.delete.runner.qute.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ApplicationInfo {

    @ConfigProperty(name = "quarkus.application.name")
    String name;

    @ConfigProperty(name = "quarkus.http.static-resources.index-page")
    String index;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "-")
    Optional<String> version;

    @ConfigProperty(name = "quarkus.swagger-ui.path", defaultValue = "q/swagger-ui")
    Optional<String> swagger;

    Properties git;

    @PostConstruct
    public void init() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
            git = new Properties();
            // load a properties file
            git.load(input);
        }
    }

    public Optional<String> getVersion() {
        return version;
    }

    public Optional<String> getSwagger() {
        return swagger;
    }

    public String getName() {
        return name;
    }

    public Properties getGit() {
        return git;
    }

    public String getIndex() {
        return index;
    }

}
