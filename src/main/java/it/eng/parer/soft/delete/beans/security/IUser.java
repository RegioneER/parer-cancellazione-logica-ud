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

package it.eng.parer.soft.delete.beans.security;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import it.eng.parer.soft.delete.beans.security.profile.Profile;

public interface IUser<T extends Profile> extends Serializable {

    public String getNome();

    public void setNome(String nome);

    public String getCognome();

    public void setCognome(String cognome);

    public long getIdUtente();

    public void setIdUtente(long idUtente);

    public String getUsername();

    public void setUsername(String username);

    public boolean isAttivo();

    public void setAttivo(boolean attivo);

    public Date getScadenzaPwd();

    public void setScadenzaPwd(Date scadenzaPwd);

    public T getProfile();

    public void setProfile(T profile);

    public List<String> getApplicazioni();

    public void setApplicazioni(List<String> applicazioni);

    // ***** Aggiunti per la gestione dei diversi tipi di IDP
    public enum UserType {
        SPID_FEDERA
    }

    public UserType getUserType();

    public void setUserType(UserType userType);

    public String getExternalId();

    public void setExternalId(String externalId);
    // *****
}
