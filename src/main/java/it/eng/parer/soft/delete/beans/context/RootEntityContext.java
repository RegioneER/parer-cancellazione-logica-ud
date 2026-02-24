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

package it.eng.parer.soft.delete.beans.context;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe di supporto per gestire il contesto dell'entità root durante la cancellazione logica
 */
@ApplicationScoped
public class RootEntityContext {

    private static final Logger log = LoggerFactory.getLogger(RootEntityContext.class);

    private final ThreadLocal<Object> currentRootIdContext = new ThreadLocal<>();

    /**
     * Imposta l'ID dell'entità root nel contesto corrente
     */
    public void setCurrentRootId(Object rootId) {
        log.debug("Impostato ID root nel contesto: {}", rootId);
        currentRootIdContext.set(rootId);
    }

    /**
     * Recupera l'ID dell'entità root dal contesto corrente
     */
    public Object getCurrentRootId() {
        return currentRootIdContext.get();
    }

    /**
     * Pulisce il contesto corrente
     */
    public void clear() {
        currentRootIdContext.remove();
    }
}
