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
package it.eng.parer.soft.delete.beans;

import java.util.Map;

import it.eng.parer.soft.delete.beans.dto.base.IWSDesc;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.security.User;
import jakarta.validation.constraints.NotNull;

public interface IControlliWsService {

    RispostaControlli checkVersione(String versione,
            @NotNull(message = "IControlliWsService.checkVersione: versioniWsKey non inizializzato") String versioniWsKey,
            Map<String, String> xmlDefaults);

    RispostaControlli checkCredenziali(String loginName, String password,
            @NotNull(message = "IControlliWsService.checkCredenziali: versione non inizializzato") String indirizzoIP);

    RispostaControlli checkAuthWS(
            @NotNull(message = "IControlliWsService.checkAuthWS: utente non inizializzato") User utente,
            @NotNull(message = "IControlliWsService.checkAuthWS: descrizione non inizializzato") IWSDesc descrizione);

    RispostaControlli checkAuthWSNoOrg(
            @NotNull(message = "IControlliWsService.checkAuthWSNoOrg: utente non inizializzato") User utente,
            @NotNull(message = "IControlliWsService.checkAuthWSNoOrg: descrizione non inizializzato") IWSDesc descrizione);

    RispostaControlli loadWsVersions(
            @NotNull(message = "IControlliWsService.loadWsVersions: descrizione non inizializzato") IWSDesc descrizione);

}
