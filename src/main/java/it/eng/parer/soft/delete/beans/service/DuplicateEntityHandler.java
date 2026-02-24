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

package it.eng.parer.soft.delete.beans.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IDuplicateEntityHandler;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;
import it.eng.parer.soft.delete.jpa.entity.AroDupRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Classe helper per la gestione delle entità duplicate.
 */
@ApplicationScoped
public class DuplicateEntityHandler implements IDuplicateEntityHandler {

    private static final Logger log = LoggerFactory.getLogger(DuplicateEntityHandler.class);

    private final EntityManager entityManager;

    @Inject
    public DuplicateEntityHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Inserisce un record nella tabella ARO_DUP_RICH_SOFT_DELETE per un'entità duplicata
     *
     * @return numero di righe inserite (1 o 0)
     */
    public int insertDuplicateOneEntity(Class<?> childClass, Object childId, Object parentId,
            Field parentField, LocalDateTime timestamp, int level, long offset, Object rootId,
            AroItemRichSoftDelete item) {

        Field childIdField = JpaEntityReflectionHelper.getIdField(childClass);
        String childTable = JpaEntityReflectionHelper.getTableName(childClass);
        String childRefColumn = JpaEntityReflectionHelper.getColumnName(childIdField);
        String parentRefColumn = JpaEntityReflectionHelper.getColumnName(parentField);

        // Calcola il timestamp con l'offset
        LocalDateTime timestampWithOffset = timestamp.plusNanos((offset * 1000));

        // Prepara il JSON per dmSoftDelete
        String dmSoftDelete = String.format(
                "{\"ID_UD\":%d,\"ID_PK\":%d,\"ID_FK\":%d,\"NI_LVL\":%d,\"NM_TAB\":\"%s\",\"NM_PK\":\"%s\",\"NM_FK\":\"%s\"}",
                (long) rootId, (long) childId, (long) parentId, level, childTable, childRefColumn,
                parentRefColumn);

        // Crea l'entità e imposta i suoi campi
        AroDupRichSoftDelete dupEntity = new AroDupRichSoftDelete();
        dupEntity.setAroItemRichSoftDelete(item);
        dupEntity.setIdUnitaDocRef(new BigDecimal((long) rootId));
        dupEntity.setNmChildTable(childTable);
        dupEntity.setTsSoftDelete(timestampWithOffset);
        dupEntity.setDmSoftDelete(dmSoftDelete);

        // Persisti l'entità usando EntityManager
        entityManager.persist(dupEntity);

        log.debug(
                "Inserito record duplicato per l'entità {} con id {} nella tabella ARO_DUP_RICH_SOFT_DELETE",
                childClass.getSimpleName(), childId);

        // Dato che abbiamo inserito esattamente un record, ritorniamo 1
        return 1;
    }

    /**
     * Inserisce record nella tabella ARO_DUP_RICH_SOFT_DELETE per tutti i figli duplicati
     *
     * @return numero di righe inserite
     */
    public int insertDuplicateAllEntityByParent(Class<?> childClass, Field childField,
            Object parentId, Field parentField, LocalDateTime timestamp, int level, long offset,
            Object rootId, AroItemRichSoftDelete item) {

        String childTable = JpaEntityReflectionHelper.getTableName(childClass);
        String childRefColumn = JpaEntityReflectionHelper.getColumnName(childField);
        String parentRefColumn = JpaEntityReflectionHelper.getColumnName(parentField);

        // Costruisci la query INSERT INTO ... SELECT nativa CON HINT PARALLEL.
        String sqlQuery = String.format(
                "INSERT /*+ PARALLEL(ARO_DUP_RICH_SOFT_DELETE, 4) */ INTO ARO_DUP_RICH_SOFT_DELETE "
                        + "(ID_DUP_RICH_SOFT_DELETE, ID_ITEM_RICH_SOFT_DELETE, ID_UNITA_DOC_REF, NM_CHILD_TABLE, TS_SOFT_DELETE, DM_SOFT_DELETE) "
                        + "SELECT /*+ PARALLEL(e, 4) */ " + "SARO_DUP_RICH_SOFT_DELETE.NEXTVAL, "
                        + "%d, " + "%d, " + "'%s', "
                        + "? + NUMTODSINTERVAL((ROWNUM + ?) / 1000000, 'SECOND'), "
                        + "'{\"ID_UD\":' || %d || " + "',\"ID_PK\":' || e.%s || "
                        + "',\"ID_FK\":' || %s || " + "',\"NI_LVL\":' || %d || "
                        + "',\"NM_TAB\":\"' || '%s' || '\"' || "
                        + "',\"NM_PK\":\"' || '%s' || '\"' || "
                        + "',\"NM_FK\":\"' || '%s' || '\"}' " + "FROM %s e " + "WHERE e.%s = ?",
                item.getIdItemRichSoftDelete(), (long) rootId, childTable, (long) rootId,
                childRefColumn, parentRefColumn, level, childTable, childRefColumn, parentRefColumn,
                childTable, parentRefColumn);

        // Crea la query nativa con i parametri rimanenti (timestamp, offset, parentId)
        Query query = entityManager.createNativeQuery(sqlQuery).setParameter(1, timestamp)
                .setParameter(2, offset).setParameter(3, parentId);

        // Esegui la query
        int inserted = query.executeUpdate();

        log.info(
                "Inseriti {} record duplicati per la tabella {} con parent_id {} nella tabella ARO_DUP_RICH_SOFT_DELETE",
                inserted, childTable, parentId);
        return inserted;
    }
}
