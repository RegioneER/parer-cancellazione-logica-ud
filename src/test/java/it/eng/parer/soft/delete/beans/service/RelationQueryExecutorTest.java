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
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.annotations.RelationQuery;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

@ExtendWith(MockitoExtension.class)
class RelationQueryExecutorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private RootEntityContext rootEntityContext;

    @Mock
    private Query<Object[]> mockQuery;

    @InjectMocks
    private RelationQueryExecutor relationQueryExecutor;

    private Long rootId;
    private Long parentId;

    @BeforeEach
    void setup() {
        rootId = 100L;
        parentId = 200L;
    }

    // ==================== constructor ====================

    @Test
    void constructor_initializesCorrectly() {
        RelationQueryExecutor executor = new RelationQueryExecutor(entityManager,
                rootEntityContext);
        assertNotNull(executor);
    }

    // ==================== hasOptimizedQueryFor - with annotations ====================

    @Test
    void hasOptimizedQueryFor_withSingleAnnotation_returnsTrue()
            throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasOptimizedQueryFor_withMultipleAnnotations_returnsTrue()
            throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithMultipleQueries.class;
        Class<?> parentClass = TestParent.class;
        int level = 2;

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasOptimizedQueryFor_withGenericLevel_returnsTrue() throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithGenericQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 5; // Qualsiasi livello dovrebbe funzionare

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertTrue(result);
    }

    // ==================== hasOptimizedQueryFor - without annotations ====================

    @Test
    void hasOptimizedQueryFor_withNoAnnotations_returnsFalse()
            throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildNoQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasOptimizedQueryFor_withWrongParentClass_returnsFalse()
            throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestWrongParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasOptimizedQueryFor_withWrongLevel_returnsFalse() throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 99; // Livello non specificato

        // Act
        boolean result = relationQueryExecutor.hasOptimizedQueryFor(childClass, parentClass, level);

        // Assert
        assertFalse(result);
    }

    // ==================== supportsBatchQuery ====================

    @Test
    void supportsBatchQuery_withInClause_returnsTrue() throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithBatchQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.supportsBatchQuery(childClass, parentClass, level);

        // Assert
        assertTrue(result);
    }

    @Test
    void supportsBatchQuery_withoutInClause_returnsFalse() throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.supportsBatchQuery(childClass, parentClass, level);

        // Assert
        assertFalse(result);
    }

    @Test
    void supportsBatchQuery_noAnnotation_returnsFalse() throws AppGenericPersistenceException {
        // Arrange
        Class<?> childClass = TestChildNoQuery.class;
        Class<?> parentClass = TestParent.class;
        int level = 1;

        // Act
        boolean result = relationQueryExecutor.supportsBatchQuery(childClass, parentClass, level);

        // Assert
        assertFalse(result);
    }

    // ==================== executeQueryForRelation - success ====================

    @Test
    void executeQueryForRelation_success() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(rootId);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] row1 = new Object[] {
                300L, parentId };
        Object[] row2 = new Object[] {
                301L, parentId };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(row1, row2));

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            List<EntityNode> nodes = result.toList();
            assertEquals(2, nodes.size());
            assertEquals(300L, nodes.get(0).getEntityId());
            assertEquals(301L, nodes.get(1).getEntityId());
        }

        verify(mockQuery, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void executeQueryForRelation_noRootId_returnsEmpty() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(null);

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            assertEquals(0, result.count());
        }

        verify(entityManager, never()).createQuery(anyString());
    }

    @Test
    void executeQueryForRelation_noAnnotation_returnsEmpty() throws Exception {
        // Arrange
        Class<?> childClass = TestChildNoQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        // NON serve mockare rootEntityContext perché il metodo ritorna subito se non trova
        // annotation

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            assertEquals(0, result.count());
        }

        verify(entityManager, never()).createQuery(anyString());
        verify(rootEntityContext, never()).getCurrentRootId();
    }

    // ==================== executeQueryForRelationBatch - success ====================

    @Test
    void executeQueryForRelationBatch_success() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithBatchQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        List<Object> parentIds = Arrays.asList(200L, 201L, 202L);
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(rootId);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] row1 = new Object[] {
                300L, 200L };
        Object[] row2 = new Object[] {
                301L, 201L };
        Object[] row3 = new Object[] {
                302L, 202L };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(row1, row2, row3));

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelationBatch(
                childClass, parentClass, childIdField, parentField, parentIds, level)) {

            // Assert
            List<EntityNode> nodes = result.toList();
            assertEquals(3, nodes.size());
        }

        verify(mockQuery).setParameter("parentIds", parentIds);
    }

    @Test
    void executeQueryForRelationBatch_emptyParentIds_returnsEmpty() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithBatchQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        List<Object> parentIds = Collections.emptyList();
        int level = 1;

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelationBatch(
                childClass, parentClass, childIdField, parentField, parentIds, level)) {

            // Assert
            assertEquals(0, result.count());
        }

        verify(entityManager, never()).createQuery(anyString());
    }

    @Test
    void executeQueryForRelationBatch_nullParentIds_returnsEmpty() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithBatchQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelationBatch(
                childClass, parentClass, childIdField, parentField, null, level)) {

            // Assert
            assertEquals(0, result.count());
        }

        verify(entityManager, never()).createQuery(anyString());
    }

    // ==================== executeQueryForRelation - invalid results ====================

    @Test
    void executeQueryForRelation_nullRow_skipped() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(rootId);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] validRow = new Object[] {
                300L, parentId };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(validRow, null));

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            List<EntityNode> nodes = result.toList();
            assertEquals(1, nodes.size());
        }
    }

    @Test
    void executeQueryForRelation_incompleteRow_skipped() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(rootId);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] validRow = new Object[] {
                300L, parentId };
        Object[] incompleteRow = new Object[] {
                301L }; // Manca parentId
        when(mockQuery.getResultStream()).thenReturn(Stream.of(validRow, incompleteRow));

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            List<EntityNode> nodes = result.toList();
            assertEquals(1, nodes.size());
            assertEquals(300L, nodes.get(0).getEntityId());
        }
    }

    @Test
    void executeQueryForRelation_nullChildId_skipped() throws Exception {
        // Arrange
        Class<?> childClass = TestChildWithSingleQuery.class;
        Class<?> parentClass = TestParent.class;
        Field childIdField = TestChildNoQuery.class.getDeclaredField("id");
        Field parentField = TestChildNoQuery.class.getDeclaredField("parent");
        int level = 1;

        when(rootEntityContext.getCurrentRootId()).thenReturn(rootId);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setHint(anyString(), any())).thenReturn(mockQuery);

        Object[] validRow = new Object[] {
                300L, parentId };
        Object[] nullIdRow = new Object[] {
                null, parentId };
        when(mockQuery.getResultStream()).thenReturn(Stream.of(validRow, nullIdRow));

        // Act
        try (Stream<EntityNode> result = relationQueryExecutor.executeQueryForRelation(childClass,
                parentClass, childIdField, parentField, parentId, level)) {

            // Assert
            List<EntityNode> nodes = result.toList();
            assertEquals(1, nodes.size());
        }
    }

    // ==================== Test Entities with Annotations ====================

    @RelationQuery(parentClass = TestParent.class, query = "SELECT c.id, c.parent.id FROM TestChildWithSingleQuery c WHERE c.parent.id = :parentId AND c.root.id = :rootId", rootIdParam = "rootId", parentIdParam = "parentId", levels = {
            1 })
    static class TestChildWithSingleQuery {
        @Id
        private Long id;
        @JoinColumn(name = "ID_PARENT")
        private TestParent parent;
    }

    @RelationQuery(parentClass = TestParent.class, query = "SELECT c.id, c.parent.id FROM TestChildWithMultipleQueries c WHERE c.parent.id = :parentId AND c.root.id = :rootId", rootIdParam = "rootId", parentIdParam = "parentId", levels = {
            1 })
    @RelationQuery(parentClass = TestParent.class, query = "SELECT c.id, c.parent.id FROM TestChildWithMultipleQueries c WHERE c.parent.id = :parentId", rootIdParam = "rootId", parentIdParam = "parentId", levels = {
            2 })
    static class TestChildWithMultipleQueries {
        @Id
        private Long id;
        @JoinColumn(name = "ID_PARENT")
        private TestParent parent;
    }

    @RelationQuery(parentClass = TestParent.class, query = "SELECT c.id, c.parent.id FROM TestChildWithGenericQuery c WHERE c.parent.id = :parentId AND c.root.id = :rootId", rootIdParam = "rootId", parentIdParam = "parentId", levels = {})
    static class TestChildWithGenericQuery {
        @Id
        private Long id;
        @JoinColumn(name = "ID_PARENT")
        private TestParent parent;
    }

    @RelationQuery(parentClass = TestParent.class, query = "SELECT c.id, c.parent.id FROM TestChildWithBatchQuery c WHERE c.parent.id IN :parentIds AND c.root.id = :rootId", rootIdParam = "rootId", parentIdParam = "parentId", parentIdsParam = "parentIds", levels = {
            1 })
    static class TestChildWithBatchQuery {
        @Id
        private Long id;
        @JoinColumn(name = "ID_PARENT")
        private TestParent parent;
    }

    static class TestChildNoQuery {
        @Id
        @Column(name = "ID_CHILD")
        private Long id;
        @JoinColumn(name = "ID_PARENT")
        private TestParent parent;
    }

    static class TestParent {
        @Id
        @Column(name = "ID_PARENT")
        private Long id;
    }

    static class TestWrongParent {
        @Id
        private Long id;
    }
}
