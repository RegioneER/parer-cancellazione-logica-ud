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

package it.eng.parer.soft.delete.beans.streams;

import java.util.List;

import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.service.RelationQueryExecutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class StreamSourceFactory {

    EntityManager entityManager;

    RelationQueryExecutor relationQueryExecutor;

    @Inject
    public StreamSourceFactory(EntityManager entityManager,
            RelationQueryExecutor relationQueryExecutor) {
        this.entityManager = entityManager;
        this.relationQueryExecutor = relationQueryExecutor;
    }

    public RelationStreamSource createRelationStream(int level, List<EntityRelation> relations) {
        return new RelationStreamSource(level, relations, relationQueryExecutor, entityManager);
    }
}
