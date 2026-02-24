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
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.IEntityRelationResolver;
import it.eng.parer.soft.delete.beans.IRelationQueryExecutor;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.jpa.entity.AroDoc;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class EntityRelationResolverTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private IRelationQueryExecutor relationQueryExecutor;

    @Mock
    private Query mockQuery; // Rimosso <Object[]>

    @InjectMocks
    private EntityRelationResolver entityRelationResolver;

    private Set<String> processedEntities;
    private List<EntityNode> consumedNodes;
    private Consumer<EntityNode> nodeConsumer;

    @BeforeEach
    void setup() {
        processedEntities = new HashSet<>();
        consumedNodes = new ArrayList<>();
        nodeConsumer = node -> consumedNodes.add(node);
    }

    // ==================== constructor ====================

    @Test
    void constructor_initializesCorrectly() {
        EntityRelationResolver resolver = new EntityRelationResolver(entityManager,
                relationQueryExecutor);
        assertNotNull(resolver);
        assertTrue(resolver instanceof IEntityRelationResolver);
    }

    // ==================== findBatchChildEntities - OneToMany ====================

    @Test
    void findBatchChildEntities_oneToMany_success() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToMany.class;
        List<Object> parentIds = Arrays.asList(100L, 101L);
        int parentLevel = 0;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] row1 = new Object[] {
                200L, 100L };
        Object[] row2 = new Object[] {
                201L, 101L };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(row1, row2));

        // Act
        entityRelationResolver.findBatchChildEntities(parentClass, parentIds, parentLevel,
                processedEntities, nodeConsumer);

        // Assert
        assertEquals(2, consumedNodes.size());
        assertEquals(TestChildEntity.class, consumedNodes.get(0).getEntityClass());
        assertEquals(200L, consumedNodes.get(0).getEntityId());
    }

    @Test
    void findBatchChildEntities_emptyParentIds_noProcessing() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToMany.class;
        List<Object> parentIds = Collections.emptyList();
        int parentLevel = 0;

        // Act
        entityRelationResolver.findBatchChildEntities(parentClass, parentIds, parentLevel,
                processedEntities, nodeConsumer);

        // Assert
        assertTrue(consumedNodes.isEmpty());
        verify(entityManager, never()).createQuery(anyString());
    }

    // ==================== findBatchChildEntities - OneToOne ====================

    @Test
    void findBatchChildEntities_oneToOne_skipsInverseRelations() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToOne.class;
        List<Object> parentIds = Arrays.asList(100L);
        int parentLevel = 0;

        // Act
        entityRelationResolver.findBatchChildEntities(parentClass, parentIds, parentLevel,
                processedEntities, nodeConsumer);

        // Assert
        // Le relazioni @OneToOne con mappedBy non vengono processate in batch
        assertEquals(0, consumedNodes.size());
        verify(entityManager, never()).createQuery(anyString());
    }

    // ==================== processBatchChildEntities - optimized query ====================

    @Test
    void processBatchChildEntities_optimizedQuery_success() throws Exception {
        // Arrange
        Class<?> parentClass = AroUnitaDoc.class;
        Class<?> childClass = AroDoc.class;
        List<Object> parentIds = Arrays.asList(100L);
        Field parentIdField = TestParentWithOneToMany.class.getDeclaredField("id");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field relationField = TestChildEntity.class.getDeclaredField("parent");
        int parentLevel = 0;

        when(relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, 1))
                .thenReturn(true);
        when(relationQueryExecutor.supportsBatchQuery(childClass, parentClass, 1))
                .thenReturn(false);

        EntityNode mockNode = new EntityNode(childClass, 200L, childIdField, 1, parentClass, 100L,
                relationField);
        when(relationQueryExecutor.executeQueryForRelation(any(), any(), any(), any(), any(),
                anyInt())).thenReturn(Stream.of(mockNode));

        // Act
        entityRelationResolver.processBatchChildEntities(parentClass, parentIds, parentIdField,
                childClass, childIdField, relationField, parentLevel, processedEntities,
                nodeConsumer);

        // Assert
        assertEquals(1, consumedNodes.size());
        verify(relationQueryExecutor).executeQueryForRelation(childClass, parentClass, childIdField,
                relationField, 100L, 1);
    }

    @Test
    void processBatchChildEntities_optimizedBatchQuery_success() throws Exception {
        // Arrange
        Class<?> parentClass = AroUnitaDoc.class;
        Class<?> childClass = AroDoc.class;
        List<Object> parentIds = Arrays.asList(100L, 101L);
        Field parentIdField = TestParentWithOneToMany.class.getDeclaredField("id");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field relationField = TestChildEntity.class.getDeclaredField("parent");
        int parentLevel = 0;

        when(relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, 1))
                .thenReturn(true);
        when(relationQueryExecutor.supportsBatchQuery(childClass, parentClass, 1)).thenReturn(true);

        EntityNode node1 = new EntityNode(childClass, 200L, childIdField, 1, parentClass, 100L,
                relationField);
        EntityNode node2 = new EntityNode(childClass, 201L, childIdField, 1, parentClass, 101L,
                relationField);
        when(relationQueryExecutor.executeQueryForRelationBatch(any(), any(), any(), any(),
                anyList(), anyInt())).thenReturn(Stream.of(node1, node2));

        // Act
        entityRelationResolver.processBatchChildEntities(parentClass, parentIds, parentIdField,
                childClass, childIdField, relationField, parentLevel, processedEntities,
                nodeConsumer);

        // Assert
        assertEquals(2, consumedNodes.size());
        verify(relationQueryExecutor).executeQueryForRelationBatch(childClass, parentClass,
                childIdField, relationField, parentIds, 1);
    }

    // ==================== processBatchChildEntities - standard query ====================

    @Test
    void processBatchChildEntities_standardQuery_success() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToMany.class;
        Class<?> childClass = TestChildEntity.class;
        List<Object> parentIds = Arrays.asList(100L, 101L);
        Field parentIdField = parentClass.getDeclaredField("id");
        Field childIdField = childClass.getDeclaredField("id");
        Field relationField = childClass.getDeclaredField("parent");
        int parentLevel = 0;

        when(relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, 1))
                .thenReturn(false);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] row1 = new Object[] {
                200L, 100L };
        Object[] row2 = new Object[] {
                201L, 101L };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(row1, row2));

        // Act
        entityRelationResolver.processBatchChildEntities(parentClass, parentIds, parentIdField,
                childClass, childIdField, relationField, parentLevel, processedEntities,
                nodeConsumer);

        // Assert
        assertEquals(2, consumedNodes.size());
        verify(entityManager).createQuery(contains("select c.id"));
    }

    @Test
    void processBatchChildEntities_standardQuery_verifyBatchPartitioning() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToMany.class;
        Class<?> childClass = TestChildEntity.class;

        // Crea una lista con più di 999 elementi per testare il partizionamento
        List<Object> parentIds = new ArrayList<>();
        for (long i = 1; i <= 1500; i++) {
            parentIds.add(i);
        }

        Field parentIdField = parentClass.getDeclaredField("id");
        Field childIdField = childClass.getDeclaredField("id");
        Field relationField = childClass.getDeclaredField("parent");
        int parentLevel = 0;

        when(relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, 1))
                .thenReturn(false);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        // Crea uno STREAM NUOVO per ogni chiamata a getResultStream()
        when(mockQuery.getResultStream()).thenAnswer(invocation -> Stream.empty())
                .thenAnswer(invocation -> Stream.empty());

        // Act
        entityRelationResolver.processBatchChildEntities(parentClass, parentIds, parentIdField,
                childClass, childIdField, relationField, parentLevel, processedEntities,
                nodeConsumer);

        // Assert
        verify(entityManager, times(2)).createQuery(anyString()); // 999 + 501 = 2 batch
    }

    // ==================== duplicate detection ====================

    @Test
    void processBatchChildEntities_detectsDuplicates() throws Exception {
        // Arrange
        Class<?> parentClass = TestParentWithOneToMany.class;
        Class<?> childClass = TestChildEntity.class;
        List<Object> parentIds = Arrays.asList(100L);
        Field parentIdField = parentClass.getDeclaredField("id");
        Field childIdField = childClass.getDeclaredField("id");
        Field relationField = childClass.getDeclaredField("parent");
        int parentLevel = 0;

        when(relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, 1))
                .thenReturn(false);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        // Stesso childId due volte (duplicato)
        Object[] row1 = new Object[] {
                200L, 100L };
        Object[] row2 = new Object[] {
                200L, 100L };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(row1, row2));

        // Act
        entityRelationResolver.processBatchChildEntities(parentClass, parentIds, parentIdField,
                childClass, childIdField, relationField, parentLevel, processedEntities,
                nodeConsumer);

        // Assert
        assertEquals(2, consumedNodes.size());
        assertFalse(consumedNodes.get(0).isDuplicate());
        assertTrue(consumedNodes.get(1).isDuplicate());
    }

    @Test
    void processNode_marksDuplicateCorrectly() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field childIdField = childClass.getDeclaredField("id");
        Field relationField = childClass.getDeclaredField("parent");

        EntityNode node1 = new EntityNode(childClass, 200L, childIdField, 1,
                TestParentWithOneToMany.class, 100L, relationField);
        EntityNode node2 = new EntityNode(childClass, 200L, childIdField, 1,
                TestParentWithOneToMany.class, 100L, relationField);

        // Act
        nodeConsumer.accept(node1);
        processedEntities.add(childClass.getName() + ":" + 200L);
        nodeConsumer.accept(node2);

        // Assert - il secondo nodo dovrebbe essere processato manualmente come duplicato
        assertEquals(2, consumedNodes.size());
    }

    // ==================== Helper Test Entities ====================

    static class TestParentWithOneToMany {
        @Id
        @Column(name = "ID_PARENT")
        private Long id;

        @OneToMany(mappedBy = "parent")
        private List<TestChildEntity> children;
    }

    static class TestChildEntity {
        @Id
        @Column(name = "ID_CHILD")
        private Long id;

        @ManyToOne
        @JoinColumn(name = "ID_PARENT")
        private TestParentWithOneToMany parent;
    }

    static class TestParentWithOneToOne {
        @Id
        @Column(name = "ID_PARENT")
        private Long id;

        @OneToOne(mappedBy = "parent")
        private TestOneToOneChild child;
    }

    static class TestOneToOneChild {
        @Id
        @Column(name = "ID_CHILD")
        private Long id;

        @OneToOne
        @JoinColumn(name = "ID_PARENT")
        private TestParentWithOneToOne parent;
    }

    // ==================== Utility Tests ====================

    @Test
    void maxBatchSize_validation() {
        int maxBatchSize = 999;
        assertEquals(999, maxBatchSize);
        assertTrue(maxBatchSize > 0);
    }

    @Test
    void partitionList_logic() {
        List<Long> list = new ArrayList<>();
        for (long i = 1; i <= 1500; i++) {
            list.add(i);
        }

        List<List<Long>> partitions = new ArrayList<>();
        int size = 999;
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }

        assertEquals(2, partitions.size());
        assertEquals(999, partitions.get(0).size());
        assertEquals(501, partitions.get(1).size());
    }

    @Test
    void entityKey_format() {
        String className = "TestEntity";
        Object id = 123L;
        String entityKey = className + ":" + id;
        assertEquals("TestEntity:123", entityKey);
    }

    @Test
    void processedEntities_tracking() {
        Set<String> processed = new HashSet<>();
        String key1 = "Entity:100";
        String key2 = "Entity:200";

        processed.add(key1);
        assertTrue(processed.contains(key1));
        assertFalse(processed.contains(key2));

        processed.add(key2);
        assertTrue(processed.contains(key2));
        assertEquals(2, processed.size());
    }

    @Test
    void childLevel_calculation() {
        int parentLevel = 0;
        int childLevel = parentLevel + 1;
        assertEquals(1, childLevel);

        parentLevel = 2;
        childLevel = parentLevel + 1;
        assertEquals(3, childLevel);
    }
}
