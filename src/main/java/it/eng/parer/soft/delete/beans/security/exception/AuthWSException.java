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

package it.eng.parer.soft.delete.beans.security.exception;

public class AuthWSException extends Exception {

    public enum CodiceErrore {
        LOGIN_FALLITO, UTENTE_NON_ATTIVO, UTENTE_SCADUTO, UTENTE_NON_AUTORIZZATO,
        PROBLEMA_ESTRAZIONE_APPLICAZIONE
    }

    private static final long serialVersionUID = 1L;

    private final CodiceErrore codiceErrore;
    private final String descrizioneErrore;

    public AuthWSException(CodiceErrore code, String msg) {
        super();
        this.codiceErrore = code;
        this.descrizioneErrore = msg;
    }

    public CodiceErrore getCodiceErrore() {
        return codiceErrore;
    }

    public String getDescrizioneErrore() {
        return descrizioneErrore;
    }

}
