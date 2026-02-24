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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.runner.util;

public class EndPointCostants {

    private EndPointCostants() {
        throw new IllegalStateException("Utility class");
    }

    // URI endpoint
    public static final String URL_ADMIN_BASE = "/admin";
    public static final String URL_API_BASE = "/api";
    private static final String URL_CTX_PUBLIC = "/public";
    private static final String URL_CTX_OAUTH2 = "/oauth2";

    public static final String RESOURCE_INFOS = "/infos";
    public static final String RESOURCE_SOFT_DELETE = "/softdelete";

    public static final String URL_PUBLIC_SOFT_DELETE = URL_CTX_PUBLIC + RESOURCE_SOFT_DELETE;
    public static final String URL_OAUTH_2_SOFT_DELETE = URL_CTX_OAUTH2 + RESOURCE_SOFT_DELETE;

    //
    public static final String URL_GET_INFOS = URL_ADMIN_BASE + RESOURCE_INFOS;
    public static final String URL_GET_STATUS = "/status";

}
