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

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // nome servizio
    public static final String SERVICE_NAME = "SoftDeleteMach";
    // IDP log accessi
    public static final String IDP_LOG_ACESSI_FUNCT = "Log accessi";
    // Costanti per il log dei login ws e la disattivazione automatica utenti
    public static final String IDP_MAX_TENTATIVI_FALLITI = "MAX_TENTATIVI_FALLITI";
    public static final String IDP_MAX_GIORNI = "MAX_GIORNI";
    public static final String IDP_QRY_DISABLE_USER = "QRY_DISABLE_USER";
    public static final String IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE = "QRY_VERIFICA_DISATTIVAZIONE_UTENTE";
    public static final String IDP_QRY_REGISTRA_EVENTO_UTENTE = "QRY_REGISTRA_EVENTO_UTENTE";

    public static final String DATE_FORMAT_TIMESTAMP_TYPE = "dd/MM/yyyy HH:mm:ss";

    public enum AppNameEnum {
        ANY, SACER, SACER_PREINGEST
    }

    public enum SoftDeleteEnum {
        UNITA_DOC
    }

    // Enum per tipo dato
    public enum TipoDato {
        REGISTRO, TIPO_UNITA_DOC, TIPO_DOC, TIPO_DOC_PRINC, SUB_STRUTTURA, TIPO_FASCICOLO,
        TIPO_OBJECT
    }

    /**
     * Modalità di cancellazione logica supportate
     */
    public enum SoftDeleteMode {
        CAMPIONE, COMPLETA
    }

}
