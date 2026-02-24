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

package it.eng.parer.soft.delete.beans.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class UUIDMdcLogUtil {

    private static final String UUID_LOG_MDC = "log_uuid";

    private UUIDMdcLogUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generate UUID and put it on MDC Log4j
     *
     * "Remember MDC is managed on a per thread basis and every child thread automatically inherits
     * a copy of the mapped diagnostic context from its parent. This is achieved by using
     * InheritableThreadLocal class, which is a subclass of the ThreadLocal class."
     *
     * L'application server in determinati contesti applicativi (N servlet coinvolte) potrebbe
     * gestire più worker (multi thread) non parenti, il meccanismo di ereditarietà dell'MDC non
     * sarebbe quindi garantito (l'UUID non è trasmesso tra thread che non sono parenti).
     */
    public static void genUuid() {
        MDC.put(UUID_LOG_MDC, UUID.randomUUID().toString());
    }

    /**
     * Get UUID from MDC (if is blank - it means new thread, create a new one)
     *
     * @return UUID
     *
     */
    public static String getUuid() {
        if (StringUtils.isBlank(MDC.get(UUIDMdcLogUtil.UUID_LOG_MDC))) {
            genUuid();
        }
        return MDC.get(UUIDMdcLogUtil.UUID_LOG_MDC);
    }
}
