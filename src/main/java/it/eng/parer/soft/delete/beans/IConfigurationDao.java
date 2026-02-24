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

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

public interface IConfigurationDao {

    String getParamApplicValue(
            @NotNull(message = "IConfigurationDao.getParamApplicValue: nmParamApplic non inizializzato") String nmParamApplic);

    String getParamApplicValue(
            @NotNull(message = "IConfigurationDao.getParamApplicValue: nmParamApplic non inizializzato") String nmParamApplic,
            long idStrut, long idAmbiente, long idTipoUnitaDoc);

    String getParamApplicValue(
            @NotNull(message = "IConfigurationDao.getParamApplicValue: nmParamApplic non inizializzato") String nmParamApplic,
            long idStrut, long idAmbiente, long idTipoUnitaDoc, long idAaTipoFascicolo);

    Map<String, String> getValoreParamApplicByTiParamApplicAsMap(
            @NotNull(message = "IConfigurationDao.getValoreParamApplicByTiParamApplicAsMap: nmParamApplic non inizializzato") List<String> tiParamApplics);

    String getValoreParamApplicByApplic(
            @NotNull(message = "IConfigurationDao.getValoreParamApplicByApplic: nmParamApplic non inizializzato") String nmParamApplic);

    String getValoreParamApplicByAaTipoFasc(
            @NotNull(message = "IConfigurationDao.getValoreParamApplicByAaTipoFasc: nmParamApplic non inizializzato") String nmParamApplic,
            long idStrut, long idAmbiente, long idAaTipoFascicolo);

    /**
     *
     * Gestione FLAG true = 1, false = 0
     *
     * @param nmParamApplic     nome parametro applicativo
     * @param idStrut           id struttura
     * @param idAmbiente        id ambiente
     * @param idTipoUnitaDoc    id tipo unita doc
     * @param idAaTipoFascicolo id anno tipo fascicolo
     *
     * @return Valore del flag indicato secondo nome
     */
    String getParamApplicValueAsFl(
            @NotNull(message = "IConfigurationDao.nmParamApplic: nmParamApplic non inizializzato") String nmParamApplic,
            long idStrut, long idAmbiente, long idTipoUnitaDoc, long idAaTipoFascicolo);

}
