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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.service.RelationQueryExecutor;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class StreamSourceFactoryTest {

    @Inject
    StreamSourceFactory streamSourceFactory;

    @Inject
    EntityManager entityManager;

    @Inject
    RelationQueryExecutor relationQueryExecutor;

    @Test
    void createRelationStream_createsSourceWithCorrectLevel() {
        List<EntityRelation> relations = new ArrayList<>();
        int expectedLevel = 5;

        RelationStreamSource source = streamSourceFactory.createRelationStream(expectedLevel,
                relations);

        assertNotNull(source);
        assertEquals(expectedLevel, source.getLevel());
        assertTrue(source instanceof RelationStreamSource);
    }

    @Test
    void createRelationStream_differentInstancesForSameParameters() {
        List<EntityRelation> relations = new ArrayList<>();

        RelationStreamSource source1 = streamSourceFactory.createRelationStream(1, relations);
        RelationStreamSource source2 = streamSourceFactory.createRelationStream(1, relations);

        assertNotSame(source1, source2);
    }

    @Test
    void dependencies_areInjected() {
        assertNotNull(streamSourceFactory);
        assertNotNull(entityManager);
        assertNotNull(relationQueryExecutor);
    }
}
