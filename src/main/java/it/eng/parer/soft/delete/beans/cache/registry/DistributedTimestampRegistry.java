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

package it.eng.parer.soft.delete.beans.cache.registry;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Registry per la gestione distribuita dei timestamp tramite Infinispan Embedded. Mantiene una
 * cache locale per ottimizzare le prestazioni.
 */
@ApplicationScoped
@Startup
public class DistributedTimestampRegistry {

    private static final Logger log = LoggerFactory.getLogger(DistributedTimestampRegistry.class);

    /**
     * Cache locale per ridurre le chiamate alla cache distribuita
     */
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    /**
     * Validità della cache locale in millisecondi
     */
    private static final long LOCAL_CACHE_TTL_MS = 500;

    Cache<String, LocalDateTime> timestampCache;

    @Inject
    public DistributedTimestampRegistry(Cache<String, LocalDateTime> timestampCache) {
        this.timestampCache = timestampCache;
    }

    /**
     * Inizializza la cache
     */
    public void initialize() {
        try {
            log.info("Inizializzazione della cache distribuita dei timestamp");
            log.info("Cache '{}' inizializzata, dimensione attuale: {}", timestampCache.getName(),
                    timestampCache.size());

        } catch (Exception e) {
            log.error("Errore nell'inizializzazione della cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Ottiene il timestamp più recente per una tabella. Prima controlla nella cache locale, poi
     * nella cache distribuita.
     *
     * @param tableName        Nome della tabella
     * @param defaultTimestamp Timestamp di default se non presente in cache
     *
     * @return Il timestamp più recente
     */
    public LocalDateTime getLatestTimestamp(String tableName, LocalDateTime defaultTimestamp) {
        // Controlla prima la cache locale
        CacheEntry entry = localCache.get(tableName);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        try {
            // Se non presente nella cache locale o è scaduta, controlla la cache
            // distribuita
            LocalDateTime timestamp = timestampCache.get(tableName);

            if (timestamp != null) {
                // Aggiorna la cache locale
                localCache.put(tableName, new CacheEntry(timestamp));
                return timestamp;
            }
        } catch (Exception e) {
            log.warn("Errore nel recupero del timestamp per la tabella {}: {}", tableName,
                    e.getMessage());
        }

        return defaultTimestamp;
    }

    /**
     * Aggiorna il timestamp di una tabella nella cache distribuita. Il timestamp viene aggiornato
     * solo se è più recente di quello esistente.
     *
     * @param tableName Nome della tabella
     * @param timestamp Nuovo timestamp
     */
    public void updateTimestamp(String tableName, LocalDateTime timestamp) {
        try {
            // Aggiorna il timestamp in modo atomico
            timestampCache.compute(tableName, (key, existingValue) -> {
                // Se non esiste un valore o il nuovo timestamp è più recente, usa
                // il nuovo
                if (existingValue == null || timestamp.isAfter(existingValue)) {
                    return timestamp;
                }
                // Altrimenti mantieni quello esistente
                return existingValue;
            });

            // Aggiorna la cache locale
            localCache.put(tableName, new CacheEntry(timestamp));

            log.debug("Timestamp per tabella {} aggiornato: {}", tableName, timestamp);
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento del timestamp per la tabella {}: {}", tableName,
                    e.getMessage());
        }
    }

    /**
     * Classe interna per gestire la cache locale con TTL
     */
    private static class CacheEntry {
        private final LocalDateTime value;
        private final long expirationTime;

        public CacheEntry(LocalDateTime value) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + LOCAL_CACHE_TTL_MS;
        }

        public LocalDateTime getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
