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

import it.eng.parer.soft.delete.beans.dto.base.CSChiave;
import it.eng.parer.soft.delete.beans.dto.base.CSVersatore;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.utils.Costanti.TipiGestioneUDAnnullate;
import jakarta.validation.constraints.NotNull;

public interface IControlliSemanticiService {

    RispostaControlli caricaDefaultDaDB(
            @NotNull(message = "IControlliSemanticiService.caricaDefaultDaDB: tipoPar non inizializzato") String tipoPar);

    RispostaControlli checkIdStrut(
            @NotNull(message = "IControlliSemanticiService.checkIdStrut: vers non inizializzato") CSVersatore vers);

    RispostaControlli checkChiave(
            @NotNull(message = "IControlliSemanticiService.checkChiave: key non inizializzato") CSChiave key,
            long idStruttura,
            @NotNull(message = "IControlliSemanticiService.checkChiave: tguda non inizializzato") TipiGestioneUDAnnullate tguda);

}
