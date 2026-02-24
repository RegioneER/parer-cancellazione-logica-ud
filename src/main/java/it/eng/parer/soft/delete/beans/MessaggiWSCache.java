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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSHelper;
import it.eng.parer.soft.delete.jpa.entity.DecErrSacer;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Singleton
@Startup
public class MessaggiWSCache {

    private static Logger log = LoggerFactory.getLogger(MessaggiWSCache.class);

    MessaggiWSHelper messaggiWSHelper;

    Map<String, String> errorMap;

    @Inject
    public MessaggiWSCache(MessaggiWSHelper messaggiWSHelper) {
        this.messaggiWSHelper = messaggiWSHelper;
    }

    @PostConstruct
    public void initSingleton() {
        log.info("Inizializzazione singleton MessaggiWSCache...");
        try {

            List<DecErrSacer> list = messaggiWSHelper.caricaListaErrori();
            errorMap = new HashMap<>();
            for (DecErrSacer err : list) {
                errorMap.put(err.getCdErr(), StringEscapeUtils.unescapeJava(err.getDsErr())); // Unescape
                // java-like
                // every message
            }
        } catch (RuntimeException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
        log.info("Inizializzazione singleton MessaggiWSCache... completata.");
    }

    @Transactional(value = TxType.NOT_SUPPORTED)
    public String getString(String key) {
        return errorMap.get(key);
    }

    @Transactional(value = TxType.NOT_SUPPORTED)
    public String getString(String key, Object... params) {
        return MessageFormat.format(errorMap.get(key), cleanTextContent(params));
    }

    /*
     * Clean up placeholder (from XML)
     */
    private Object[] cleanTextContent(Object... params) {
        List<Object> cleanedTxtParams = new ArrayList<>();
        Stream.of(params).filter(Objects::nonNull).forEach(param -> {
            String sparam = param.toString(); // force toString()
            // erases all the ASCII control characters
            sparam = sparam.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "?");
            // removes non-printable characters from Unicode
            sparam = sparam.replaceAll("\\p{C}", "?");
            // unescape XML of sparam
            sparam = StringEscapeUtils.unescapeXml(sparam);
            // final result
            cleanedTxtParams.add(sparam);
        });
        return cleanedTxtParams.toArray(new Object[0]);
    }

}
