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

import java.util.stream.Stream;

import it.eng.parer.soft.delete.beans.dto.EntityNode;

/**
 * Interfaccia per generatori di stream che possono essere valutati on-demand
 */
public interface IStreamSource {

    /**
     * Crea uno stream di nodi entità
     *
     * @return lo stream di nodi entità
     */
    Stream<EntityNode> createStream();

    /**
     * Restituisce il livello del generatore di stream
     *
     * @return il livello del generatore di stream
     */
    int getLevel();
}
