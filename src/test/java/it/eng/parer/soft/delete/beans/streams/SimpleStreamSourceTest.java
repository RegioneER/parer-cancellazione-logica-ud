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

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import jakarta.persistence.Id;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class SimpleStreamSourceTest {

    private Class<?> testEntityClass;
    private Object testEntityId;
    private Field testIdField;

    @BeforeEach
    void setup() throws NoSuchFieldException {
        testEntityClass = TestEntity.class;
        testEntityId = 123L;
        testIdField = TestEntity.class.getDeclaredField("id");
    }

    @Test
    void constructor_initializesCorrectly() {
        SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                testIdField, 1);
        assertNotNull(source);
    }

    @Test
    void createStream_returnsSingleNodeWithCorrectProperties() {
        // Test combinato che verifica tutte le proprietà in un colpo solo
        int expectedLevel = 5;
        SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                testIdField, expectedLevel);

        Stream<EntityNode> stream = source.createStream();
        List<EntityNode> nodes = stream.toList();

        assertEquals(1, nodes.size());

        EntityNode node = nodes.get(0);
        assertEquals(testEntityClass, node.getEntityClass());
        assertEquals(testEntityId, node.getEntityId());
        assertEquals(testIdField, node.getIdField());
        assertEquals(expectedLevel, node.getLevel());
        assertNull(node.getParentClass());
        assertNull(node.getParentId());
        assertNull(node.getParentField());
        assertFalse(node.isDuplicate());
    }

    @Test
    void getLevel_returnsCorrectLevel() {
        SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                testIdField, 3);
        assertEquals(3, source.getLevel());
    }

    @Test
    void createStream_canBeCalledMultipleTimes() {
        SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                testIdField, 1);

        assertEquals(1, source.createStream().count());
        assertEquals(1, source.createStream().count());
    }

    @Test
    void createStream_withDifferentLevels() {
        for (int level : new int[] {
                0, 5, 10 }) {
            SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                    testIdField, level);

            EntityNode node = source.createStream().findFirst().orElseThrow();
            assertEquals(level, node.getLevel());
        }
    }

    @Test
    void createStream_withDifferentEntityIds() {
        Object[] ids = {
                1L, "ABC123", 999L };

        for (Object id : ids) {
            SimpleStreamSource source = new SimpleStreamSource(testEntityClass, id, testIdField, 1);

            EntityNode node = source.createStream().findFirst().orElseThrow();
            assertEquals(id, node.getEntityId());
        }
    }

    @Test
    void streamOperations_filterAndMap() {
        // Test combinato delle operazioni stream
        SimpleStreamSource source = new SimpleStreamSource(testEntityClass, testEntityId,
                testIdField, 1);

        List<Object> ids = source.createStream().filter(node -> node.getLevel() == 1)
                .map(EntityNode::getEntityId).toList();

        assertEquals(1, ids.size());
        assertEquals(testEntityId, ids.get(0));
    }

    // ==================== Classe di test ====================

    private static class TestEntity {
        @Id
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
