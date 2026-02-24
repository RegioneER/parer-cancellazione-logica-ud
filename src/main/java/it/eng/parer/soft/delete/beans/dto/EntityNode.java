/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.beans.dto;

import java.lang.reflect.Field;

import it.eng.parer.soft.delete.beans.utils.reflection.ForceCompleteModeHelper;

/**
 *
 * @author DiLorenzo_F
 */
/**
 * Classe per rappresentare un nodo dell'albero di entità Aggiunto supporto per relazioni
 * parent-child
 */
public class EntityNode {
    private final Class<?> entityClass;
    private final Object entityId;
    private final Field idField;
    private final int level;
    private final Class<?> parentClass; // Classe del parent
    private final Object parentId; // ID del parent
    private final Field parentField; // Campo che punta al parent
    private final boolean isDuplicate; // Campo per marcare le entità duplicate
    private final boolean forceCompleteMode; // Campo per forzare la modalità completa

    // Constructor per entità root (livello 0)
    public EntityNode(Class<?> entityClass, Object entityId, Field idField, int level) {
        this(entityClass, entityId, idField, level, null, null, null, false,
                checkForceCompleteMode(entityClass));
    }

    // Constructor esistente con informazioni sul parent
    public EntityNode(Class<?> entityClass, Object entityId, Field idField, int level,
            Class<?> parentClass, Object parentId, Field parentField) {
        this(entityClass, entityId, idField, level, parentClass, parentId, parentField, false,
                checkForceCompleteMode(entityClass));
    }

    // Nuovo constructor con flag isDuplicate
    public EntityNode(Class<?> entityClass, Object entityId, Field idField, int level,
            Class<?> parentClass, Object parentId, Field parentField, boolean isDuplicate) {
        this(entityClass, entityId, idField, level, parentClass, parentId, parentField, isDuplicate,
                checkForceCompleteMode(entityClass));
    }

    // Costruttore secondario per compatibilità
    public EntityNode(Class<?> entityClass, Object entityId, Field idField, int level,
            Class<?> parentClass, Object parentId, Field parentField, boolean isDuplicate,
            boolean forceCompleteMode) {
        this.entityClass = entityClass;
        this.entityId = entityId;
        this.idField = idField;
        this.level = level;
        this.parentClass = parentClass;
        this.parentId = parentId;
        this.parentField = parentField;
        this.isDuplicate = isDuplicate;
        this.forceCompleteMode = forceCompleteMode;
    }

    // Nuovo getter
    public boolean isDuplicate() {
        return isDuplicate;
    }

    public boolean isForceCompleteMode() {
        return forceCompleteMode;
    }

    // Getters
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Object getEntityId() {
        return entityId;
    }

    public Field getIdField() {
        return idField;
    }

    public int getLevel() {
        return level;
    }

    public Class<?> getParentClass() {
        return parentClass;
    }

    public Object getParentId() {
        return parentId;
    }

    public Field getParentField() {
        return parentField;
    }

    public boolean hasParent() {
        return parentClass != null && parentId != null && parentField != null;
    }

    // Chiave per identificare univocamente una relazione parent-child
    public String getRelationKey() {
        if (hasParent()) {
            return entityClass.getName() + ":" + parentClass.getName() + ":" + parentId + ":"
                    + level;
        }
        return null;
    }

    // Metodo statico per verificare la presenza dell'annotazione con cache
    private static boolean checkForceCompleteMode(Class<?> entityClass) {
        return ForceCompleteModeHelper.isForceCompleteMode(entityClass);
    }
}
