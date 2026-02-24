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
import static it.eng.parer.soft.delete.runner.util.EndPointCostants.URL_GET_STATUS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import it.eng.parer.soft.delete.Profiles;

@QuarkusTest
@TestProfile(Profiles.EndToEnd.class)
class StatusEndpointTest {

    @Test
    @TestSecurity(user = "test_microservizi", roles = {
            "admin" })
    void success() {
        given().when().get(URL_GET_STATUS).then().statusCode(200).body("$", hasKey("status"))
                .body("status", not(empty()));
    }

    @Test
    @TestSecurity(authorizationEnabled = true)
    void authRequest() {
        given().when().get(URL_GET_STATUS).then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "fakeuser", roles = {
            "not_admin" })
    void notAuthRequest() {
        given().when().get(URL_GET_STATUS).then().statusCode(403);
    }
}
