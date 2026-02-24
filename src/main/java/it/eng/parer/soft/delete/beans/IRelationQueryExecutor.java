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
import java.util.List;
import java.util.stream.Stream;

import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;

public interface IRelationQueryExecutor {

    // Metodo per singolo parent
    Stream<EntityNode> executeQueryForRelation(Class<?> childClass, Class<?> parentClass,
            Field childIdField, Field relationField, Object parentId, int level)
            throws AppGenericPersistenceException;

    // Metodo per batch di parent
    Stream<EntityNode> executeQueryForRelationBatch(Class<?> childClass, Class<?> parentClass,
            Field childIdField, Field relationField, List<Object> parentIds, int level)
            throws AppGenericPersistenceException;

    // Metodo per verificare se la query supporta batch
    boolean supportsBatchQuery(Class<?> childClass, Class<?> parentClass, int level)
            throws AppGenericPersistenceException;

    // Metodo per verificare se esiste una query ottimizzata
    boolean hasOptimizedQueryFor(Class<?> childClass, Class<?> parentClass, int level)
            throws AppGenericPersistenceException;
}
