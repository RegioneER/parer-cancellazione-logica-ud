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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.service.RelationQueryExecutor;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class RelationStreamSourceTest {

    @Inject
    EntityManager entityManager;

    @Inject
    RelationQueryExecutor relationQueryExecutor;

    @Test
    void constructor_initializesWithCorrectLevel() {
        int expectedLevel = 5;
        List<EntityRelation> relations = new ArrayList<>();

        RelationStreamSource source = new RelationStreamSource(expectedLevel, relations,
                relationQueryExecutor, entityManager);

        assertNotNull(source);
        assertEquals(expectedLevel, source.getLevel());
    }

    @Test
    @TestTransaction
    void createStream_withEmptyRelations_returnsEmptyStream() {
        List<EntityRelation> emptyRelations = new ArrayList<>();
        RelationStreamSource source = new RelationStreamSource(1, emptyRelations,
                relationQueryExecutor, entityManager);

        Stream<EntityNode> stream = source.createStream();

        assertNotNull(stream);
        assertEquals(0, stream.count());
    }

    @Test
    @TestTransaction
    void createStream_canBeCalledMultipleTimes() {
        List<EntityRelation> relations = new ArrayList<>();
        RelationStreamSource source = new RelationStreamSource(1, relations, relationQueryExecutor,
                entityManager);

        Stream<EntityNode> stream1 = source.createStream();
        Stream<EntityNode> stream2 = source.createStream();

        assertNotNull(stream1);
        assertNotNull(stream2);
    }

    @Test
    void createStream_handlesNullGracefully() {
        assertDoesNotThrow(() -> {
            RelationStreamSource source = new RelationStreamSource(1, null, relationQueryExecutor,
                    entityManager);
            assertNotNull(source);
        });
    }
}
