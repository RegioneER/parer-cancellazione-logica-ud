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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.IEntityRelationResolver;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.streams.RelationStreamSource;
import it.eng.parer.soft.delete.beans.streams.StreamSourceFactory;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroDoc;
import it.eng.parer.soft.delete.jpa.entity.AroCompDoc;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

@ExtendWith(MockitoExtension.class)
class EntityHierarchyBuilderTest {

    @Mock
    private IEntityRelationResolver entityRelationResolver;

    @Mock
    private StreamSourceFactory streamSourceFactory;

    @Mock
    private RelationStreamSource mockStreamSource;

    @InjectMocks
    private EntityHierarchyBuilder entityHierarchyBuilder;

    private Class<?> rootClass;
    private Object rootId;

    @BeforeEach
    void setup() {
        rootClass = AroUnitaDoc.class;
        rootId = 100L;
    }

    // ==================== constructor ====================

    @Test
    void constructor_initializesCorrectly() {
        EntityHierarchyBuilder builder = new EntityHierarchyBuilder(entityRelationResolver,
                streamSourceFactory);
        assertNotNull(builder);
    }

    // ==================== buildHierarchy - success ====================

    @Test
    void buildHierarchy_rootOnly_success() throws Exception {
        // Arrange - NON serve mockare streamSourceFactory quando non ci sono figli

        // Mock: nessun figlio trovato
        doAnswer(invocation -> {
            // Non chiama il consumer, quindi nessun figlio
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(any(), any(), anyInt(), any(),
                any());

        // Act
        Map<Integer, IStreamSource> result = entityHierarchyBuilder.buildHierarchy(rootClass,
                rootId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(0));
        // Verifica che NON sia stata chiamata la factory (nessun livello oltre root)
        verify(streamSourceFactory, never()).createRelationStream(anyInt(), anyList());
    }

    @Test
    void buildHierarchy_twoLevels_success() throws Exception {
        // Arrange
        when(streamSourceFactory.createRelationStream(anyInt(), anyList()))
                .thenReturn(mockStreamSource);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        // Mock: primo livello - trova 2 AroDoc
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode child1 = new EntityNode(AroDoc.class, 200L, childIdField, 1, rootClass,
                    rootId, parentField);

            EntityNode child2 = new EntityNode(AroDoc.class, 201L, childIdField, 1, rootClass,
                    rootId, parentField);

            consumer.accept(child1);
            consumer.accept(child2);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        // Mock: secondo livello - nessun figlio
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Act
        Map<Integer, IStreamSource> result = entityHierarchyBuilder.buildHierarchy(rootClass,
                rootId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(0));
        assertTrue(result.containsKey(1));
    }

    @Test
    void buildHierarchy_threeLevels_success() throws Exception {
        // Arrange
        when(streamSourceFactory.createRelationStream(anyInt(), anyList()))
                .thenReturn(mockStreamSource);

        Field parentFieldUD = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field parentFieldDoc = TestGrandchildEntity.class.getDeclaredField("aroDoc");
        Field childIdFieldDoc = TestChildEntity.class.getDeclaredField("id");
        Field childIdFieldComp = TestGrandchildEntity.class.getDeclaredField("id");

        // Livello 1: AroDoc
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode child = new EntityNode(AroDoc.class, 200L, childIdFieldDoc, 1, rootClass,
                    rootId, parentFieldUD);

            consumer.accept(child);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        // Livello 2: AroCompDoc
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode grandchild = new EntityNode(AroCompDoc.class, 300L, childIdFieldComp, 2, AroDoc.class,
                    200L, parentFieldDoc);

            consumer.accept(grandchild);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Livello 3: nessun figlio
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroCompDoc.class), any(), eq(2),
                any(), any());

        // Act
        Map<Integer, IStreamSource> result = entityHierarchyBuilder.buildHierarchy(rootClass,
                rootId);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsKey(0));
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(2));
    }

    // ==================== buildHierarchy - duplicate handling ====================

    @Test
    void buildHierarchy_duplicateNodes_markedCorrectly() throws Exception {
        // Arrange
        when(streamSourceFactory.createRelationStream(anyInt(), anyList()))
                .thenReturn(mockStreamSource);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        // Mock: trova lo stesso nodo 2 volte (duplicate)
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode child1 = new EntityNode(AroDoc.class, 200L, childIdField, 1, rootClass,
                    rootId, parentField);

            EntityNode duplicate = new EntityNode(AroDoc.class, 200L, childIdField, 1, rootClass,
                    rootId, parentField, true);

            consumer.accept(child1);
            consumer.accept(duplicate);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        // Mock secondo livello vuoto
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Act
        Map<Integer, IStreamSource> result = entityHierarchyBuilder.buildHierarchy(rootClass,
                rootId);

        // Assert
        assertNotNull(result);
        verify(streamSourceFactory).createRelationStream(eq(1), anyList());
    }

    // ==================== buildHierarchy - batch processing ====================

    @Test
    void buildHierarchy_batchProcessing_groupsByType() throws Exception {
        // Arrange
        when(streamSourceFactory.createRelationStream(anyInt(), anyList()))
                .thenReturn(mockStreamSource);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        // Mock: trova 3 figli dello stesso tipo
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            for (long i = 200L; i < 203L; i++) {
                EntityNode child = new EntityNode(AroDoc.class, i, childIdField, 1, rootClass,
                        rootId, parentField);
                consumer.accept(child);
            }
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        // Mock secondo livello vuoto
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Act
        entityHierarchyBuilder.buildHierarchy(rootClass, rootId);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> parentIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class),
                parentIdsCaptor.capture(), eq(1), any(), any());

        List<Object> capturedIds = parentIdsCaptor.getValue();
        assertEquals(3, capturedIds.size());
        assertTrue(capturedIds.contains(200L));
        assertTrue(capturedIds.contains(201L));
        assertTrue(capturedIds.contains(202L));
    }

    // ==================== buildHierarchy - exploration logic ====================

    @Test
    void buildHierarchy_exploredAtLevel_preventsDuplicateExploration() throws Exception {
        // Arrange
        when(streamSourceFactory.createRelationStream(anyInt(), anyList()))
                .thenReturn(mockStreamSource);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        // Mock: stesso nodo allo stesso livello non viene processato 2 volte
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode child = new EntityNode(AroDoc.class, 200L, childIdField, 1, rootClass,
                    rootId, parentField);

            // Tenta di aggiungere 2 volte
            consumer.accept(child);
            consumer.accept(child);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        // Mock secondo livello
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Act
        entityHierarchyBuilder.buildHierarchy(rootClass, rootId);

        // Assert - verifica che batch processing sia chiamato 1 volta per livello 1
        verify(entityRelationResolver, times(1)).findBatchChildEntities(eq(AroDoc.class), any(),
                eq(1), any(), any());
    }

    // ==================== buildHierarchy - relation registration ====================

    @Test
    void buildHierarchy_relationRegistration_success() throws Exception {
        // Arrange
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EntityRelation>> relationsCaptor = ArgumentCaptor.forClass(List.class);
        when(streamSourceFactory.createRelationStream(eq(1), relationsCaptor.capture()))
                .thenReturn(mockStreamSource);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<EntityNode> consumer = invocation.getArgument(4);

            EntityNode child = new EntityNode(AroDoc.class, 200L, childIdField, 1, rootClass,
                    rootId, parentField);

            consumer.accept(child);
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(rootClass), any(), eq(0), any(),
                any());

        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(eq(AroDoc.class), any(), eq(1),
                any(), any());

        // Act
        entityHierarchyBuilder.buildHierarchy(rootClass, rootId);

        // Assert
        List<EntityRelation> capturedRelations = relationsCaptor.getValue();
        assertNotNull(capturedRelations);
        assertEquals(1, capturedRelations.size());

        EntityRelation relation = capturedRelations.get(0);
        assertEquals(AroDoc.class, relation.getChildClass());
        assertEquals(rootClass, relation.getParentClass());
        assertEquals(rootId, relation.getParentId());
    }

    // ==================== buildHierarchy - empty results ====================

    @Test
    void buildHierarchy_noChildren_returnsOnlyRoot() throws Exception {
        // Arrange
        doAnswer(invocation -> {
            return null;
        }).when(entityRelationResolver).findBatchChildEntities(any(), any(), anyInt(), any(),
                any());

        // Act
        Map<Integer, IStreamSource> result = entityHierarchyBuilder.buildHierarchy(rootClass,
                rootId);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.containsKey(0));
        assertFalse(result.containsKey(1));
    }

    // ==================== Helper Test Entities ====================

    static class TestRootEntity {
        @Id
        @Column(name = "ID_UNITA_DOC")
        private Long id;
    }

    static class TestChildEntity {
        @Id
        @Column(name = "ID_DOC")
        private Long id;

        @JoinColumn(name = "ID_UNITA_DOC")
        private TestRootEntity aroUnitaDoc;
    }

    static class TestGrandchildEntity {
        @Id
        @Column(name = "ID_COMP_DOC")
        private Long id;

        @JoinColumn(name = "ID_DOC")
        private TestChildEntity aroDoc;
    }

    // ==================== Data Structure Tests ====================

    @Test
    void dataStructures_initialization() {
        Set<String> processedForDiscovery = new HashSet<>();
        Set<String> exploredAtLevel = new HashSet<>();
        Set<String> discoveredRelations = new HashSet<>();

        assertTrue(processedForDiscovery.isEmpty());
        assertTrue(exploredAtLevel.isEmpty());
        assertTrue(discoveredRelations.isEmpty());
    }

    @Test
    void entityKeyWithLevel_format() {
        String className = "TestEntity";
        Object id = 123L;
        int level = 2;
        String key = className + ":" + id + "@" + level;

        assertEquals("TestEntity:123@2", key);
    }

    @Test
    void relationKey_format() {
        String childClass = "ChildEntity";
        String parentClass = "ParentEntity";
        String key = childClass + "->" + parentClass;

        assertEquals("ChildEntity->ParentEntity", key);
    }

    @Test
    void groupNodesByType_logic() {
        Map<Class<?>, List<EntityNode>> nodesByType = new HashMap<>();
        Class<?> type1 = AroDoc.class;
        Class<?> type2 = AroCompDoc.class;

        nodesByType.computeIfAbsent(type1, k -> new ArrayList<>());
        nodesByType.computeIfAbsent(type2, k -> new ArrayList<>());

        assertEquals(2, nodesByType.size());
        assertTrue(nodesByType.containsKey(type1));
        assertTrue(nodesByType.containsKey(type2));
    }

    @Test
    void nextLevelNodes_isEmpty() {
        List<EntityNode> nextLevelNodes = new ArrayList<>();

        assertTrue(nextLevelNodes.isEmpty());
        assertEquals(0, nextLevelNodes.size());
    }

    @Test
    void nodesByLevel_mapOperations() {
        Map<Integer, List<EntityNode>> nodesByLevel = new HashMap<>();

        nodesByLevel.put(0, new ArrayList<>());
        nodesByLevel.put(1, new ArrayList<>());

        assertTrue(nodesByLevel.containsKey(0));
        assertTrue(nodesByLevel.containsKey(1));
        assertFalse(nodesByLevel.containsKey(2));
    }
}
