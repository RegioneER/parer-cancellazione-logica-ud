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

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;

/**
 * Interfaccia per la gestione delle entità duplicate.
 */
public interface IDuplicateEntityHandler {

    /**
     * Inserisce un record nella tabella ARO_DUP_RICH_SOFT_DELETE per un'entità duplicata
     *
     * @param childClass  Classe dell'entità figlio
     * @param childId     ID dell'entità figlio
     * @param parentId    ID dell'entità parent
     * @param parentField Campo di relazione con il parent
     * @param timestamp   Timestamp da utilizzare
     * @param level       Livello nella gerarchia
     * @param offset      Offset per il timestamp
     * @param rootId      ID dell'entità radice
     * @param item        Item di richiesta soft delete associato
     *
     * @return Numero di righe inserite
     */
    int insertDuplicateOneEntity(Class<?> childClass, Object childId, Object parentId,
            Field parentField, LocalDateTime timestamp, int level, long offset, Object rootId,
            AroItemRichSoftDelete item);

    /**
     * Inserisce record nella tabella ARO_DUP_RICH_SOFT_DELETE per tutti i figli duplicati
     *
     * @param childClass  Classe dell'entità figlio
     * @param childField  Campo ID della classe figlio
     * @param parentId    ID dell'entità parent
     * @param parentField Campo di relazione con il parent
     * @param timestamp   Timestamp da utilizzare
     * @param level       Livello nella gerarchia
     * @param offset      Offset per il timestamp
     * @param rootId      ID dell'entità radice
     * @param item        Item di richiesta soft delete associato
     *
     * @return Numero di righe inserite
     */
    int insertDuplicateAllEntityByParent(Class<?> childClass, Field childField, Object parentId,
            Field parentField, LocalDateTime timestamp, int level, long offset, Object rootId,
            AroItemRichSoftDelete item);
}
