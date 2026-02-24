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

package it.eng.parer.soft.delete.beans.cache.producer;

import java.time.LocalDateTime;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ApplicationScoped
public class TimestampCacheProducer {

    private static final Logger log = LoggerFactory.getLogger(TimestampCacheProducer.class);
    private static final String CACHE_NAME = "timestampCache";

    EmbeddedCacheManager cacheManager;

    @Inject
    public TimestampCacheProducer(EmbeddedCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void initializeCache() {
        try {
            log.info("Inizializzazione della cache '{}'", CACHE_NAME);

            // Verifica se la cache esiste già
            if (!cacheManager.cacheExists(CACHE_NAME)) {
                log.info("Cache '{}' non esistente, creazione programmatica", CACHE_NAME);

                // Crea una configurazione base
                ConfigurationBuilder builder = new ConfigurationBuilder();

                // Configura la cache come distribuita se in cluster
                if (cacheManager.getCacheManagerConfiguration().transport().transport() != null) {
                    log.info("Configurazione cache distribuita - clustering attivo");
                    builder.clustering().cacheMode(CacheMode.DIST_SYNC).hash().numOwners(2);
                } else {
                    log.info("Configurazione cache locale - clustering non attivo");
                }

                // Configurazione comune
                builder.memory().storage(StorageType.HEAP).maxCount(1000L)
                        .whenFull(EvictionStrategy.REMOVE).statistics().enabled(true);

                // La configurazione delle transazioni è obbligatoria
                builder.transaction().transactionMode(TransactionMode.NON_TRANSACTIONAL);

                cacheManager.defineConfiguration(CACHE_NAME, builder.build());
                log.info("Cache '{}' creata con successo", CACHE_NAME);
            } else {
                log.info("Cache '{}' già esistente", CACHE_NAME);
            }
        } catch (Exception e) {
            throw new AppGenericRuntimeException(
                    "Errore nella creazione della cache " + CACHE_NAME + ": " + e.getMessage(),
                    ErrorCategory.INTERNAL_ERROR);
        }
    }

    @Produces
    @Singleton
    public Cache<String, LocalDateTime> timestampCache() {
        return cacheManager.getCache(CACHE_NAME);
    }
}
