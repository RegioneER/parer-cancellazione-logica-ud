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

import java.util.Map;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;

/**
 * Interfaccia per la costruzione della gerarchia di entità.
 */
public interface IEntityHierarchyBuilder {

    /**
     * Costruisce una mappa di sorgenti di stream per ciascun livello della gerarchia
     *
     * @param rootClass Classe dell'entità radice
     * @param rootId    ID dell'entità radice
     *
     * @return Mappa di stream sources per livello
     *
     * @throws AppGenericPersistenceException in caso di errori durante la costruzione
     */
    Map<Integer, IStreamSource> buildHierarchy(Class<?> rootClass, Object rootId)
            throws AppGenericPersistenceException;
}
