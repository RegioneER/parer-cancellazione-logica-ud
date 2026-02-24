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

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;

import static it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle.UD_001_012;
import static it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle.UD_001_011;
import static it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle.WS_CHECK;
import static it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle.XSD_001_002;
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_API_BASE;
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_PUBLIC_SOFT_DELETE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.common.net.HttpHeaders;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import it.eng.parer.soft.delete.Profiles;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@TestProfile(Profiles.EndToEnd.class)
class CancellazioneLogicaEndpointTest {

    @Test
    @TestSecurity(authorizationEnabled = false)
    void wrongXmlRequest_fails() {
        given().config(RestAssured.config().encoderConfig(
                encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .urlEncodingEnabled(true).multiPart("LOGINNAME", "fake", MediaType.TEXT_PLAIN)
                .multiPart("VERSIONE", "9999", MediaType.TEXT_PLAIN)
                .multiPart("PASSWORD", "fake", MediaType.TEXT_PLAIN)
                .multiPart("XMLREQ",
                        "<RichiestaCancellazioneLogica></RichiestaCancellazioneLogica>",
                        MediaType.TEXT_XML)
                .when().post(URL_API_BASE + URL_PUBLIC_SOFT_DELETE).then().statusCode(400)
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceEsito",
                        is("NEGATIVO"))
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceErrore",
                        is(XSD_001_002));
    }

    @Test
    @TestSecurity(authorizationEnabled = false)
    void missingParams_fails() {
        given().config(RestAssured.config().encoderConfig(
                encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .urlEncodingEnabled(true).when().post(URL_API_BASE + URL_PUBLIC_SOFT_DELETE).then()
                .statusCode(400)
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceEsito",
                        is("NEGATIVO"))
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceErrore",
                        is(WS_CHECK));
    }

    @Test
    @TestSecurity(authorizationEnabled = false)
    void wrongCredentials_fails() {
        given().config(RestAssured.config().encoderConfig(
                encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .urlEncodingEnabled(true).multiPart("LOGINNAME", "not_exists", MediaType.TEXT_PLAIN)
                .multiPart("VERSIONE", "1.0", MediaType.TEXT_PLAIN)
                .multiPart("PASSWORD", "password", MediaType.TEXT_PLAIN)
                .multiPart("XMLREQ", xmlReq(), MediaType.TEXT_XML).when()
                .post(URL_API_BASE + URL_PUBLIC_SOFT_DELETE).then().statusCode(200)
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceEsito",
                        is("NEGATIVO"))
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceErrore",
                        is(UD_001_012));
    }

    @Test
    @TestSecurity(authorizationEnabled = false)
    void wrongVersion_fails() {
        given().config(RestAssured.config().encoderConfig(
                encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .urlEncodingEnabled(true)
                .multiPart("LOGINNAME", "admin_generale", MediaType.TEXT_PLAIN)
                .multiPart("VERSIONE", "9999999", MediaType.TEXT_PLAIN)
                .multiPart("PASSWORD", "password", MediaType.TEXT_PLAIN)
                .multiPart("XMLREQ", xmlReq(), MediaType.TEXT_XML).when()
                .post(URL_API_BASE + URL_PUBLIC_SOFT_DELETE).then().statusCode(200)
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceEsito",
                        is("NEGATIVO"))
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceErrore",
                        is(UD_001_011));
    }

    @Test
    @Timeout(value = 900, unit = TimeUnit.SECONDS)
    @TestSecurity(authorizationEnabled = false)
    @Disabled("Test E2E che modifica realmente il database. "
            + "Eseguire manualmente solo su ambiente di test dedicato. ")
    void success() {
        given().config(RestAssured.config()
                .encoderConfig(encoderConfig().encodeContentTypeAs("multipart/form-data",
                        ContentType.TEXT))
                .httpClient(httpClientConfig().setParam("http.socket.timeout", 900 * 1000) // 15
                        // minuti
                        .setParam("http.connection.timeout", 900 * 1000)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                .urlEncodingEnabled(true)
                .multiPart("LOGINNAME", "admin_generale", MediaType.TEXT_PLAIN)
                .multiPart("VERSIONE", "1.0", MediaType.TEXT_PLAIN)
                .multiPart("PASSWORD", "password", MediaType.TEXT_PLAIN)
                .multiPart("XMLREQ", xmlReq(), MediaType.TEXT_XML).when()
                .post(URL_API_BASE + URL_PUBLIC_SOFT_DELETE).then().statusCode(200)
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceEsito.text()",
                        is("POSITIVO"))
                .body("EsitoRichiestaCancellazioneLogica.EsitoRichiesta.CodiceErrore",
                        is(not("NEGATIVO")));
    }

    private String xmlReq() {
        try {
            return IOUtils.toString(this.getClass().getResourceAsStream("/richiestaCorretta.xml"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
