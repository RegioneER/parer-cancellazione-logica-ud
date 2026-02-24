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

package it.eng.parer.soft.delete.beans;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;

/**
 * Interfaccia per l'elaborazione di un livello nella gerarchia di entità.
 */
public interface ILevelProcessor {

    /**
     * Elabora un livello specifico nella gerarchia di entità
     *
     * @param streamSource Sorgente di stream per il livello
     * @param mode         Modalità di cancellazione (CAMPIONE o COMPLETA)
     * @param item         Item di richiesta soft delete associato
     *
     * @throws AppGenericPersistenceException in caso di errori durante l'elaborazione
     */
    void processLevel(IStreamSource streamSource, SoftDeleteMode mode, AroItemRichSoftDelete item)
            throws AppGenericPersistenceException;
}
