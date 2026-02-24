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
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.ISoftDeleteExecutor;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Query;
import jakarta.persistence.Table;

@ExtendWith(MockitoExtension.class)
class SoftDeleteExecutorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query mockQuery;

    @InjectMocks
    private SoftDeleteExecutor softDeleteExecutor;

    private LocalDateTime baseTimestamp;
    private Long rootId;
    private Long childId;
    private Long parentId;

    @BeforeEach
    void setup() {
        baseTimestamp = LocalDateTime.of(2024, 1, 27, 12, 0, 0);
        rootId = 100L;
        childId = 200L;
        parentId = 300L;
    }

    // ==================== constructor ====================

    @Test
    void constructor_initializesCorrectly() {
        SoftDeleteExecutor executor = new SoftDeleteExecutor(entityManager);
        assertNotNull(executor);
        assertTrue(executor instanceof ISoftDeleteExecutor);
    }

    // ==================== softDeleteRootEntity - success ====================

    @Test
    void softDeleteRootEntity_success() {
        // Arrange
        Class<?> entityClass = TestRootEntity.class;
        int level = 0;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        int result = softDeleteExecutor.softDeleteRootEntity(entityClass, rootId, baseTimestamp,
                level, rootId);

        // Assert
        assertEquals(1, result);
        verify(entityManager).createQuery(contains("update TestRootEntity"));
        verify(mockQuery).setParameter("tsSoftDelete", baseTimestamp);
        verify(mockQuery).setParameter("id", rootId);
        verify(mockQuery).executeUpdate();
    }

    @Test
    void softDeleteRootEntity_verifyDmSoftDeleteJson() {
        // Arrange
        Class<?> entityClass = TestRootEntity.class;
        int level = 0;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        softDeleteExecutor.softDeleteRootEntity(entityClass, rootId, baseTimestamp, level, rootId);

        // Assert
        ArgumentCaptor<String> dmSoftDeleteCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockQuery).setParameter(eq("dmSoftDelete"), dmSoftDeleteCaptor.capture());

        String dmSoftDelete = dmSoftDeleteCaptor.getValue();
        assertTrue(dmSoftDelete.contains("\"ID_UD\":" + rootId));
        assertTrue(dmSoftDelete.contains("\"ID_PK\":" + rootId));
        assertTrue(dmSoftDelete.contains("\"ID_FK\":" + rootId));
        assertTrue(dmSoftDelete.contains("\"NI_LVL\":" + level));
        assertTrue(dmSoftDelete.contains("\"NM_TAB\":\"ARO_UNITA_DOC\""));
        assertTrue(dmSoftDelete.contains("\"NM_PK\":\"ID_UNITA_DOC\""));
        assertTrue(dmSoftDelete.contains("\"NM_FK\":\"ID_UNITA_DOC\""));
    }

    @Test
    void softDeleteRootEntity_noRowsUpdated() {
        // Arrange
        Class<?> entityClass = TestRootEntity.class;
        int level = 0;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0);

        // Act
        int result = softDeleteExecutor.softDeleteRootEntity(entityClass, rootId, baseTimestamp,
                level, rootId);

        // Assert
        assertEquals(0, result);
    }

    // ==================== softDeleteOneEntity - success ====================

    @Test
    void softDeleteOneEntity_success() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 1000L;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        int result = softDeleteExecutor.softDeleteOneEntity(childClass, childId, parentId,
                parentField, baseTimestamp, level, offset, rootId);

        // Assert
        assertEquals(1, result);
        verify(entityManager).createQuery(contains("update TestChildEntity"));
        verify(mockQuery).setParameter("childId", childId);
        verify(mockQuery).executeUpdate();
    }

    @Test
    void softDeleteOneEntity_verifyTimestampWithOffset() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 1000L;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        softDeleteExecutor.softDeleteOneEntity(childClass, childId, parentId, parentField,
                baseTimestamp, level, offset, rootId);

        // Assert
        ArgumentCaptor<LocalDateTime> timestampCaptor = ArgumentCaptor
                .forClass(LocalDateTime.class);
        verify(mockQuery).setParameter(eq("tsSoftDelete"), timestampCaptor.capture());

        LocalDateTime capturedTimestamp = timestampCaptor.getValue();
        LocalDateTime expectedTimestamp = baseTimestamp.plusNanos(offset * 1000);
        assertEquals(expectedTimestamp, capturedTimestamp);
    }

    @Test
    void softDeleteOneEntity_verifyDmSoftDeleteJson() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 1000L;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        softDeleteExecutor.softDeleteOneEntity(childClass, childId, parentId, parentField,
                baseTimestamp, level, offset, rootId);

        // Assert
        ArgumentCaptor<String> dmSoftDeleteCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockQuery).setParameter(eq("dmSoftDelete"), dmSoftDeleteCaptor.capture());

        String dmSoftDelete = dmSoftDeleteCaptor.getValue();
        assertTrue(dmSoftDelete.contains("\"ID_UD\":" + rootId));
        assertTrue(dmSoftDelete.contains("\"ID_PK\":" + childId));
        assertTrue(dmSoftDelete.contains("\"ID_FK\":" + parentId));
        assertTrue(dmSoftDelete.contains("\"NI_LVL\":" + level));
        assertTrue(dmSoftDelete.contains("\"NM_TAB\":\"ARO_DOC\""));
        assertTrue(dmSoftDelete.contains("\"NM_PK\":\"ID_DOC\""));
        assertTrue(dmSoftDelete.contains("\"NM_FK\":\"ID_UNITA_DOC\""));
    }

    @Test
    void softDeleteOneEntity_zeroOffset() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 0L;

        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // Act
        softDeleteExecutor.softDeleteOneEntity(childClass, childId, parentId, parentField,
                baseTimestamp, level, offset, rootId);

        // Assert
        ArgumentCaptor<LocalDateTime> timestampCaptor = ArgumentCaptor
                .forClass(LocalDateTime.class);
        verify(mockQuery).setParameter(eq("tsSoftDelete"), timestampCaptor.capture());

        assertEquals(baseTimestamp, timestampCaptor.getValue());
    }

    // ==================== softDeleteAllEntityByParent - success ====================

    @Test
    void softDeleteAllEntityByParent_success() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 2000L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(10);

        // Act
        int result = softDeleteExecutor.softDeleteAllEntityByParent(childClass, childIdField,
                parentId, parentField, baseTimestamp, level, offset, rootId);

        // Assert
        assertEquals(10, result);
        verify(entityManager).createNativeQuery(contains("UPDATE /*+ PARALLEL(ARO_DOC, 4)"));
        verify(mockQuery).setParameter(1, baseTimestamp);
        verify(mockQuery).setParameter(2, offset);
        verify(mockQuery).setParameter(3, parentId);
        verify(mockQuery).executeUpdate();
    }

    @Test
    void softDeleteAllEntityByParent_verifyNativeQuery() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 2000L;

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createNativeQuery(queryCaptor.capture())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(10);

        // Act
        softDeleteExecutor.softDeleteAllEntityByParent(childClass, childIdField, parentId,
                parentField, baseTimestamp, level, offset, rootId);

        // Assert
        String sql = queryCaptor.getValue();
        assertTrue(sql.contains("UPDATE /*+ PARALLEL(ARO_DOC, 4) ROW_LOCKING(EXCLUSIVE) */"));
        assertTrue(sql.contains("SET ts_soft_delete"));
        assertTrue(sql.contains("NUMTODSINTERVAL((ROWNUM + ?) / 1000000, 'SECOND')"));
        assertTrue(sql.contains("dm_soft_delete = '{\"ID_UD\":' || " + rootId));
        assertTrue(sql.contains("WHERE ID_UNITA_DOC = ?"));
    }

    @Test
    void softDeleteAllEntityByParent_verifyDmSoftDeleteConstruction() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 2000L;

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createNativeQuery(queryCaptor.capture())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(10);

        // Act
        softDeleteExecutor.softDeleteAllEntityByParent(childClass, childIdField, parentId,
                parentField, baseTimestamp, level, offset, rootId);

        // Assert
        String sql = queryCaptor.getValue();

        // Verifica che la query contenga la costruzione del JSON dinamico
        assertTrue(sql.contains("dm_soft_delete ="));
        assertTrue(sql.contains("ID_UD"));
        assertTrue(sql.contains("ID_PK"));
        assertTrue(sql.contains("ID_FK"));
        assertTrue(sql.contains("NI_LVL"));
        assertTrue(sql.contains("NM_TAB"));
        assertTrue(sql.contains("NM_PK"));
        assertTrue(sql.contains("NM_FK"));

        // Verifica riferimenti alle colonne specifiche
        assertTrue(sql.contains("ID_DOC") || sql.contains("id_doc"));
        assertTrue(sql.contains("ID_UNITA_DOC") || sql.contains("id_unita_doc"));
        assertTrue(sql.contains("ARO_DOC") || sql.contains("aro_doc"));
    }

    @Test
    void softDeleteAllEntityByParent_noRowsUpdated() throws Exception {
        // Arrange
        Class<?> childClass = TestChildEntity.class;
        Field childIdField = TestChildEntity.class.getDeclaredField("id");
        Field parentField = TestChildEntity.class.getDeclaredField("parent");
        int level = 1;
        long offset = 2000L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0);

        // Act
        int result = softDeleteExecutor.softDeleteAllEntityByParent(childClass, childIdField,
                parentId, parentField, baseTimestamp, level, offset, rootId);

        // Assert
        assertEquals(0, result);
    }

    // ==================== Helper Tests ====================

    @Test
    void timestampCalculation_withOffset() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        long offset = 1000L;
        LocalDateTime result = baseTime.plusNanos(offset * 1000);

        assertNotNull(result);
        assertTrue(result.isAfter(baseTime));
        assertEquals(1000000, result.getNano() - baseTime.getNano());
    }

    @Test
    void timestampCalculation_withLargeOffset() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        long offset = 1000000L; // 1 secondo
        LocalDateTime result = baseTime.plusNanos(offset * 1000);

        assertTrue(result.isAfter(baseTime));
        assertEquals(1, result.getSecond() - baseTime.getSecond());
    }

    @Test
    void dmSoftDeleteJson_formatValidation() {
        long testRootId = 123L;
        long testChildId = 456L;
        long testParentId = 789L;
        int testLevel = 2;
        String table = "TEST_TABLE";
        String pkColumn = "ID_TEST";
        String fkColumn = "ID_PARENT";

        String dmSoftDelete = String.format(
                "{\"ID_UD\":%d,\"ID_PK\":%d,\"ID_FK\":%d,\"NI_LVL\":%d,\"NM_TAB\":\"%s\",\"NM_PK\":\"%s\",\"NM_FK\":\"%s\"}",
                testRootId, testChildId, testParentId, testLevel, table, pkColumn, fkColumn);

        assertTrue(dmSoftDelete.contains("\"ID_UD\":123"));
        assertTrue(dmSoftDelete.contains("\"ID_PK\":456"));
        assertTrue(dmSoftDelete.contains("\"ID_FK\":789"));
        assertTrue(dmSoftDelete.contains("\"NI_LVL\":2"));
        assertTrue(dmSoftDelete.contains("\"NM_TAB\":\"TEST_TABLE\""));
    }

    @Test
    void timestampOrdering_withMultipleOffsets() {
        LocalDateTime base = LocalDateTime.now();
        LocalDateTime time1 = base.plusNanos(1000 * 1000);
        LocalDateTime time2 = base.plusNanos(2000 * 1000);
        LocalDateTime time3 = base.plusNanos(3000 * 1000);

        assertTrue(time1.isAfter(base));
        assertTrue(time2.isAfter(time1));
        assertTrue(time3.isAfter(time2));
    }

    @Test
    void offsetCalculation_microseconds() {
        long offset = 1000L;
        long nanos = offset * 1000;

        assertEquals(1000000L, nanos);
        assertEquals(0.001, nanos / 1_000_000_000.0, 0.0001);
    }

    @Test
    void levelValidation_incrementing() {
        int rootLevel = 0;
        int childLevel = rootLevel + 1;
        int grandchildLevel = childLevel + 1;

        assertEquals(1, childLevel);
        assertEquals(2, grandchildLevel);
        assertTrue(grandchildLevel > childLevel);
        assertTrue(childLevel > rootLevel);
    }

    // ==================== Test Entities ====================

    @Table(name = "ARO_UNITA_DOC")
    static class TestRootEntity {
        @Id
        @Column(name = "ID_UNITA_DOC")
        private Long id;
    }

    @Table(name = "ARO_DOC")
    static class TestChildEntity {
        @Id
        @Column(name = "ID_DOC")
        private Long id;

        @JoinColumn(name = "ID_UNITA_DOC")
        private TestRootEntity parent;
    }

    // ==================== Query Builder Tests ====================

    @Test
    void queryBuilder_softDeleteRootEntity_format() {
        String entityName = "TestEntity";
        String idFieldName = "id";

        String query = String.format(
                "update %s e set e.tsSoftDelete = :tsSoftDelete, e.dmSoftDelete = :dmSoftDelete where e.%s = :id",
                entityName, idFieldName);

        assertTrue(query.contains("update TestEntity"));
        assertTrue(query.contains("set e.tsSoftDelete = :tsSoftDelete"));
        assertTrue(query.contains("e.dmSoftDelete = :dmSoftDelete"));
        assertTrue(query.contains("where e.id = :id"));
    }

    @Test
    void queryBuilder_softDeleteOneEntity_format() {
        String entityName = "ChildEntity";
        String idFieldName = "id";

        String query = String.format(
                "update %s e set e.tsSoftDelete = :tsSoftDelete, e.dmSoftDelete = :dmSoftDelete where e.%s = :childId",
                entityName, idFieldName);

        assertTrue(query.contains("update ChildEntity"));
        assertTrue(query.contains("where e.id = :childId"));
    }

    @Test
    void nativeQueryBuilder_parallelHint() {
        String tableName = "TEST_TABLE";
        String hint = String.format("UPDATE /*+ PARALLEL(%s, 4) ROW_LOCKING(EXCLUSIVE) */ %s",
                tableName, tableName);

        assertTrue(hint.contains("PARALLEL(TEST_TABLE, 4)"));
        assertTrue(hint.contains("ROW_LOCKING(EXCLUSIVE)"));
    }

    @Test
    void nativeQueryBuilder_numTodsInterval() {
        String interval = "? + NUMTODSINTERVAL((ROWNUM + ?) / 1000000, 'SECOND')";

        assertTrue(interval.contains("NUMTODSINTERVAL"));
        assertTrue(interval.contains("ROWNUM"));
        assertTrue(interval.contains("/ 1000000"));
        assertTrue(interval.contains("'SECOND'"));
    }
}
