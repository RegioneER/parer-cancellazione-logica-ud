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

/**
 *
 * @author DiLorenzo_F
 */
/**
 * Memorizza informazioni su una relazione tra entità genitori e figli
 */
public class EntityRelation {
    final Class<?> childClass;
    final Class<?> parentClass;
    final Object parentId;
    final Field parentField;
    final Field childIdField;
    final boolean isDuplicate;

    public EntityRelation(Class<?> childClass, Class<?> parentClass, Object parentId,
            Field parentField, Field childIdField, boolean isDuplicate) {
        this.childClass = childClass;
        this.parentClass = parentClass;
        this.parentId = parentId;
        this.parentField = parentField;
        this.childIdField = childIdField;
        this.isDuplicate = isDuplicate;
    }

    public Class<?> getChildClass() {
        return childClass;
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

    public Field getChildIdField() {
        return childIdField;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

}
