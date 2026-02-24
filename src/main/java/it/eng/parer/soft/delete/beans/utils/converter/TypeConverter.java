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

package it.eng.parer.soft.delete.beans.utils.converter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe di utilità per la generica conversione dei tipi dato.
 *
 *
 * @author DiLorenzo_F
 */
public class TypeConverter {

    private TypeConverter() {
        // Costruttore privato
    }

    /**
     * Converte un valore Long in BigDecimal.
     *
     * @param numero il valore Long da convertire
     *
     * @return il valore BigDecimal corrispondente o null se il valore è null
     */
    public static BigDecimal bigDecimalFromLong(Long numero) {
        return numero == null ? null : BigDecimal.valueOf(numero);
    }

    /**
     * Converte un valore Integer in BigDecimal.
     *
     * @param numero il valore Integer da convertire
     *
     * @return il valore BigDecimal corrispondente o null se il valore è null
     */
    public static BigDecimal bigDecimalFromInteger(Integer numero) {
        return numero == null ? null : BigDecimal.valueOf(numero);
    }

    /**
     * Converte una lista di valori Long in una lista di BigDecimal.
     *
     * @param longList la lista di valori Long da convertire
     *
     * @return la lista di valori BigDecimal corrispondenti
     */
    public static List<BigDecimal> bigDecimalFromLong(Collection<Long> longList) {
        return longList.stream().map(BigDecimal::valueOf).toList();
    }

    /**
     * Converte una lista di BigDecimal in una lista di Long.
     *
     * @param idElencoVersFascSelezionatiList la lista di BigDecimal da convertire
     *
     * @return la lista di valori Long corrispondenti
     */
    public static List<Long> longListFrom(
            Collection<? extends BigDecimal> idElencoVersFascSelezionatiList) {
        return idElencoVersFascSelezionatiList.stream().map(BigDecimal::longValue).toList();
    }

    /**
     * Converte un valore BigDecimal in Long.
     *
     * @param bigDecimal il valore BigDecimal da convertire
     *
     * @return il valore Long corrispondente o null se il valore è null
     */
    public static Long longFromBigDecimal(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.longValue();
    }

    /**
     * Converte un valore Integer in Long.
     *
     * @param integer il valore Integer da convertire
     *
     * @return il valore Long corrispondente o null se il valore è null
     */
    public static Long longFromInteger(Integer integer) {
        return integer == null ? null : integer.longValue();
    }

}
