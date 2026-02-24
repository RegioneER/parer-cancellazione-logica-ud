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

//
public class ParametroApplDB {

    public static final String SERVER_NAME_SYSTEM_PROPERTY = "SERVER_NAME_SYSTEM_PROPERTY";

    // Costati per URN (SISTEMA)
    public static final String NM_SISTEMACONSERVAZIONE = "SISTEMA_CONSERVAZIONE";
    public static final String VERSIONI_WS_PREFIX = "VERSIONI_";
    //
    // Costanti per il log dei login ws e la disattivazione automatica utenti
    public static final String IDP_MAX_TENTATIVI_FALLITI = "MAX_TENTATIVI_FALLITI";
    public static final String IDP_MAX_GIORNI = "MAX_GIORNI";
    public static final String IDP_QRY_DISABLE_USER = "QRY_DISABLE_USER";
    public static final String IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE = "QRY_VERIFICA_DISATTIVAZIONE_UTENTE";
    public static final String IDP_QRY_REGISTRA_EVENTO_UTENTE = "QRY_REGISTRA_EVENTO_UTENTE";

    public static final String TENANT_OBJECT_STORAGE = "TENANT_OBJECT_STORAGE";

    // Configurazioni Storage
    public static final String BACKEND_XML_SES_FASC_ERR_KO = "BACKEND_XML_SES_FASC_ERR_KO";
    public static final String BACKEND_VERSAMENTO_FASCICOLO = "BACKEND_VERSAMENTO_FASCICOLO";

    // Configurazioni S3
    public static final String S3_PRESIGNED_URL_DURATION = "S3_PRESIGNED_URL_DURATION";
    public static final String S3_CLIENT_MAX_CONNECTIONS = "S3_CLIENT_MAX_CONNECTIONS";
    public static final String S3_CLIENT_CONNECTION_TIMEOUT = "S3_CLIENT_CONNECTION_TIMEOUT";
    public static final String S3_CLIENT_SOCKET_TIMEOUT = "S3_CLIENT_SOCKET_TIMEOUT";

    /**
     * Flags (specializzazione) {@link AplParamApplic}
     *
     */
    public class ParametroApplFl {

        private ParametroApplFl() {
            throw new IllegalStateException("Utility class");
        }

        public static final String FL_ABILITA_CONTR_CLASSIF = "FL_ABILITA_CONTR_CLASSIF";
        public static final String FL_FORZA_CONTR_CLASSIF = "FL_FORZA_CONTR_CLASSIF";
        public static final String FL_ACCETTA_CONTR_CLASSIF_NEG = "FL_ACCETTA_CONTR_CLASSIF_NEG";
        public static final String FL_FORZA_CONTR_COLLEG = "FL_FORZA_CONTR_COLLEG";
        public static final String FL_ABILITA_CONTR_COLLEG = "FL_ABILITA_CONTR_COLLEG";
        public static final String FL_ABILITA_CONTR_NUMERO = "FL_ABILITA_CONTR_NUMERO";
        public static final String FL_ACCETTA_CONTR_NUMERO_NEG = "FL_ACCETTA_CONTR_NUMERO_NEG";
        public static final String FL_FORZA_CONTR_NUMERO = "FL_FORZA_CONTR_NUMERO";
        //
        public static final String FL_ACCETTA_CONTR_COLLEG_NEG_FAS = "FL_ACCETTA_CONTR_COLLEG_NEG_FAS";

    }

    /**
     * {@link AplParamApplic}
     *
     */
    public class TipoParametroAppl {

        private TipoParametroAppl() {
            throw new IllegalStateException("Utility class");
        }

        public static final String VERSAMENTO_DEFAULT = "Default di versamento";
        public static final String MAX_RESULT = "Paginazione risultati";
        public static final String PATH = "Gestione servizi asincroni";
        public static final String TPI = "Salvataggio su nastro";
        public static final String IMAGE = "Trasformazione componenti";
        public static final String LOG_APPLIC = "Log accessi";
        public static final String IAM = "Gestione utenti";
        public static final String TSA = "Firma e Marca";
        public static final String VERSIONI_WS = "Versioni servizi";

    }

    // vista da cui recuperare i valori
    public enum TipoAplVGetValAppart {
        AATIPOFASCICOLO, TIPOUNITADOC, STRUT, AMBIENTE, APPLIC;

        public static TipoAplVGetValAppart next(TipoAplVGetValAppart last) {
            switch (last) {
            case AATIPOFASCICOLO:
                return STRUT;
            case TIPOUNITADOC:
                return STRUT;
            case STRUT:
                return AMBIENTE;
            case AMBIENTE:
                return APPLIC;
            default:
                return null;
            }
        }
    }

}
