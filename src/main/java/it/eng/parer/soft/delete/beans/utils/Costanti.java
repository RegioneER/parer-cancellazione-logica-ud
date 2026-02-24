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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.utils;

import java.util.stream.Stream;

public class Costanti {

    //
    /**
     * Si reperiscono da DB (parametri applicativi)
     */
    public static final String WS_SOFT_DELETE_VRSN = "1.0";
    /**
     * Si reperiscono da DB (parametri applicativi)
     */
    public static final String WS_SOFT_DELETE_NOME = "CancellazioneLogicaMach";

    public static final String TMP_FILE_SUFFIX = "-softdelxml.tmp";

    public class UrnFormatter {

        private UrnFormatter() {
            throw new IllegalStateException("Utility class");
        }

        public static final char URN_STD_SEPARATOR = ':';
        public static final String VERS_FMT_STRING = "{0}:{1}:{2}";
        public static final String UD_FMT_STRING = "{0}-{1}-{2}";
        public static final String DOC_FMT_STRING = "{0}-{1}";
        public static final String SPATH_COMP_FMT_STRING = "{0}-{1}-{2}-{3}";

    }

    //
    public enum ModificatoriWS {

    }

    public enum EsitoServizio {

        OK, KO, WARN
    }

    public enum TipiWSPerControlli {
        SOFT_DELETE
    }

    /**
     * Versione WS_SIP supportata dal backend
     *
     */
    public enum VersioneWS {

        V_EMPTY(""), V1_0("1.0");

        private String version;

        private VersioneWS(String version) {
            this.version = version;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        public static VersioneWS calculate(String versione) {
            return Stream.of(values()).filter(v -> v.getVersion().equalsIgnoreCase(versione))
                    .findAny().orElse(V_EMPTY);
        }

        public static boolean issupported(String versione) {
            return Stream.of(values()).anyMatch(v -> v.getVersion().equalsIgnoreCase(versione));
        }
    }

    public enum TipiGestioneFascAnnullati {

        CARICA, CONSIDERA_ASSENTE
    }

    public enum TipiGestioneUDAnnullate {

        CARICA, CONSIDERA_ASSENTE
    }

    public enum ErrorCategory {

        INTERNAL_ERROR, USER_ERROR, VALIDATION_ERROR, PERSISTENCE;
    }

}
