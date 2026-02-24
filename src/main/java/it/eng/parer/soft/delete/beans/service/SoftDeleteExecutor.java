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

import it.eng.parer.soft.delete.beans.ISoftDeleteExecutor;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * Classe helper per eseguire le operazioni di cancellazione logica.
 */
@ApplicationScoped
public class SoftDeleteExecutor implements ISoftDeleteExecutor {

    private static final Logger log = LoggerFactory.getLogger(SoftDeleteExecutor.class);

    private final EntityManager entityManager;

    @Inject
    public SoftDeleteExecutor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Esegue l'operazione di soft delete su un'entità specifica
     *
     * @return numero di righe aggiornate
     */
    public int softDeleteRootEntity(Class<?> entityClass, Object entityId, LocalDateTime timestamp,
            int level, Object rootId) {

        Field idField = JpaEntityReflectionHelper.getIdField(entityClass);
        String refTable = JpaEntityReflectionHelper.getTableName(entityClass);
        String refColumn = JpaEntityReflectionHelper.getColumnName(idField);

        String dmSoftDelete = String.format(
                "{\"ID_UD\":%d,\"ID_PK\":%d,\"ID_FK\":%d,\"NI_LVL\":%d,\"NM_TAB\":\"%s\",\"NM_PK\":\"%s\",\"NM_FK\":\"%s\"}",
                (long) rootId, (long) rootId, (long) rootId, level, refTable, refColumn, refColumn);

        // Aggiorna la Entity impostando tsSoftDelete
        int updated = entityManager.createQuery(String.format(
                "update %s e set e.tsSoftDelete = :tsSoftDelete, e.dmSoftDelete = :dmSoftDelete where e.%s = :id",
                entityClass.getSimpleName(), idField.getName()))
                .setParameter("tsSoftDelete", timestamp).setParameter("dmSoftDelete", dmSoftDelete)
                .setParameter("id", entityId).executeUpdate();

        log.debug("Aggiornate {} righe per l'entità {} con id {}", updated,
                entityClass.getSimpleName(), entityId);
        return updated;
    }

    /**
     * Aggiorna un figlio specifico - aggiorna solo 1 riga usando il childId
     *
     * @return numero di righe aggiornate
     */
    public int softDeleteOneEntity(Class<?> childClass, Object childId, Object parentId,
            Field parentField, LocalDateTime timestamp, int level, long offset, Object rootId) {

        Field childIdField = JpaEntityReflectionHelper.getIdField(childClass);
        String childTable = JpaEntityReflectionHelper.getTableName(childClass);
        String childRefColumn = JpaEntityReflectionHelper.getColumnName(childIdField);
        String parentRefColumn = JpaEntityReflectionHelper.getColumnName(parentField);

        // Calcola il timestamp con l'offset usando Java
        LocalDateTime timestampWithOffset = timestamp.plusNanos((offset * 1000));

        // Prepara il JSON per dmSoftDelete
        String dmSoftDelete = String.format(
                "{\"ID_UD\":%d,\"ID_PK\":%d,\"ID_FK\":%d,\"NI_LVL\":%d,\"NM_TAB\":\"%s\",\"NM_PK\":\"%s\",\"NM_FK\":\"%s\"}",
                (long) rootId, (long) childId, (long) parentId, level, childTable, childRefColumn,
                parentRefColumn);

        // Query semplificata: usa solo il childId per identificare univocamente la riga
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format(
                "update %s e set e.tsSoftDelete = :tsSoftDelete, e.dmSoftDelete = :dmSoftDelete "
                        + "where e.%s = :childId",
                childClass.getSimpleName(), childIdField.getName()));

        Query query = entityManager.createQuery(queryBuilder.toString())
                .setParameter("tsSoftDelete", timestampWithOffset)
                .setParameter("dmSoftDelete", dmSoftDelete).setParameter("childId", childId);

        int updated = query.executeUpdate();

        log.debug("Aggiornate {} righe per l'entità {} con id {}", updated,
                childClass.getSimpleName(), childId);
        return updated;
    }

    /**
     * Aggiorna tutti i figli di un dato padre con un'unica query
     *
     * @return numero di righe aggiornate
     */
    public int softDeleteAllEntityByParent(Class<?> childClass, Field childField, Object parentId,
            Field parentField, LocalDateTime timestamp, int level, long offset, Object rootId) {

        // Ottieni i nomi delle tabelle/colonne reali nel DB
        String childTable = JpaEntityReflectionHelper.getTableName(childClass);
        String childRefColumn = JpaEntityReflectionHelper.getColumnName(childField);
        String parentRefColumn = JpaEntityReflectionHelper.getColumnName(parentField);

        // Costruisci la query SQL nativa per Oracle CON HINT PARALLEL e
        // ROW_LOCKING(EXCLUSIVE).
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(String.format(
                "UPDATE /*+ PARALLEL(%s, 4) ROW_LOCKING(EXCLUSIVE) */ %s SET ts_soft_delete = ? + NUMTODSINTERVAL((ROWNUM + ?) / 1000000, 'SECOND'), "
                        + "dm_soft_delete = '{\"ID_UD\":' || %d || " + "',\"ID_PK\":' || %s || "
                        + "',\"ID_FK\":' || %s || " + "',\"NI_LVL\":' || %d || "
                        + "',\"NM_TAB\":\"' || '%s' || " + "'\",\"NM_PK\":\"' || '%s' || "
                        + "'\",\"NM_FK\":\"' || '%s' || '\"}' " + "WHERE %s = ?",
                childTable, childTable, (long) rootId, childRefColumn, parentRefColumn, level,
                childTable, childRefColumn, parentRefColumn, parentRefColumn));

        Query query = entityManager.createNativeQuery(sqlBuilder.toString())
                .setParameter(1, timestamp).setParameter(2, offset).setParameter(3, parentId);

        int updated = query.executeUpdate();

        log.debug("Aggiornate {} righe per la tabella {} con parent_id {}", updated, childTable,
                parentId);
        return updated;
    }
}
