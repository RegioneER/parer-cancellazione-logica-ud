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

package it.eng.parer.soft.delete.beans.utils.reflection;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.annotations.ForceCompleteMode;

/**
 * Cache per memorizzare le decisioni sulla forzatura della modalità completa per le classi di
 * entità.
 */
public class ForceCompleteModeHelper {

    private static final Logger log = LoggerFactory.getLogger(ForceCompleteModeHelper.class);

    // Cache concorrente per supportare accessi da thread multipli
    private static final ConcurrentHashMap<String, Boolean> CACHE = new ConcurrentHashMap<>();

    private ForceCompleteModeHelper() {
        // Costruttore privato per evitare istanziazione
    }

    /**
     * Verifica se una classe di entità deve forzare la modalità completa. Utilizza la cache per
     * migliorare le prestazioni.
     *
     * @param entityClass La classe dell'entità da verificare
     *
     * @return true se la classe forza la modalità completa, false altrimenti
     */
    public static boolean isForceCompleteMode(Class<?> entityClass) {
        if (entityClass == null) {
            return false;
        }

        String className = entityClass.getName();

        // Prova a ottenere dalla cache
        Boolean result = CACHE.get(className);
        if (result != null) {
            return result;
        }

        // Se non è in cache, verifica l'annotazione
        boolean hasAnnotation = entityClass.isAnnotationPresent(ForceCompleteMode.class);

        // Memorizza il risultato in cache
        CACHE.put(className, hasAnnotation);

        if (hasAnnotation) {
            log.debug("Classe {} configurata per forzare la modalità COMPLETA",
                    entityClass.getSimpleName());
        }

        return hasAnnotation;
    }

    /**
     * Pulisce la cache (utile per i test o per ricaricare le configurazioni)
     */
    public static void clearCache() {
        CACHE.clear();
    }
}
