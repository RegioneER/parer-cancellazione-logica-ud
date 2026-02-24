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

import java.util.Objects;

/**
 *
 * @author DiLorenzo_F
 */
// Classe chiave composita
public class EntityRelationKey {
    final String childClassName;
    final String parentClassName; // Può essere null per entità root

    public EntityRelationKey(String childClassName, String parentClassName) {
        this.childClassName = childClassName;
        this.parentClassName = parentClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntityRelationKey that = (EntityRelationKey) o;
        return Objects.equals(childClassName, that.childClassName)
                && Objects.equals(parentClassName, that.parentClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childClassName, parentClassName);
    }

    public String getChildClassName() {
        return childClassName;
    }

    public String getParentClassName() {
        return parentClassName;
    }
}
