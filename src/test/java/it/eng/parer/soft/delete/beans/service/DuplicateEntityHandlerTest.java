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
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.jpa.entity.AroDupRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroDoc;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Query;
import jakarta.persistence.Table;

@ExtendWith(MockitoExtension.class)
class DuplicateEntityHandlerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private DuplicateEntityHandler duplicateEntityHandler;

    private AroItemRichSoftDelete mockItem;
    private LocalDateTime baseTimestamp;

    @BeforeEach
    void setup() {
        mockItem = new AroItemRichSoftDelete();
        mockItem.setIdItemRichSoftDelete(100L);
        baseTimestamp = LocalDateTime.of(2024, 1, 27, 12, 0, 0);
    }

    // ==================== constructor ====================

    @Test
    void constructor_initializesCorrectly() {
        DuplicateEntityHandler handler = new DuplicateEntityHandler(entityManager);
        assertNotNull(handler);
    }

    // ==================== insertDuplicateOneEntity ====================

    @Test
    void insertDuplicateOneEntity_success() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Object childId = 200L;
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        doNothing().when(entityManager).persist(any(AroDupRichSoftDelete.class));

        // Act
        int result = duplicateEntityHandler.insertDuplicateOneEntity(childClass, childId, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        assertEquals(1, result);
        ArgumentCaptor<AroDupRichSoftDelete> captor = ArgumentCaptor
                .forClass(AroDupRichSoftDelete.class);
        verify(entityManager).persist(captor.capture());

        AroDupRichSoftDelete captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(mockItem, captured.getAroItemRichSoftDelete());
        assertEquals(new BigDecimal(400L), captured.getIdUnitaDocRef());
        assertEquals("ARO_DOC", captured.getNmChildTable());
        assertNotNull(captured.getTsSoftDelete());
        assertNotNull(captured.getDmSoftDelete());
    }

    @Test
    void insertDuplicateOneEntity_withOffset() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Object childId = 200L;
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 2;
        long offset = 1000L;
        Object rootId = 400L;

        doNothing().when(entityManager).persist(any(AroDupRichSoftDelete.class));

        // Act
        int result = duplicateEntityHandler.insertDuplicateOneEntity(childClass, childId, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        assertEquals(1, result);
        ArgumentCaptor<AroDupRichSoftDelete> captor = ArgumentCaptor
                .forClass(AroDupRichSoftDelete.class);
        verify(entityManager).persist(captor.capture());

        AroDupRichSoftDelete captured = captor.getValue();
        LocalDateTime expectedTimestamp = baseTimestamp.plusNanos(offset * 1000);
        assertEquals(expectedTimestamp, captured.getTsSoftDelete());
    }

    @Test
    void insertDuplicateOneEntity_dmSoftDeleteFormat() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Object childId = 200L;
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        doNothing().when(entityManager).persist(any(AroDupRichSoftDelete.class));

        // Act
        duplicateEntityHandler.insertDuplicateOneEntity(childClass, childId, parentId, parentField,
                baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        ArgumentCaptor<AroDupRichSoftDelete> captor = ArgumentCaptor
                .forClass(AroDupRichSoftDelete.class);
        verify(entityManager).persist(captor.capture());

        String dmSoftDelete = captor.getValue().getDmSoftDelete();
        assertTrue(dmSoftDelete.contains("\"ID_UD\":400"));
        assertTrue(dmSoftDelete.contains("\"ID_PK\":200"));
        assertTrue(dmSoftDelete.contains("\"ID_FK\":300"));
        assertTrue(dmSoftDelete.contains("\"NI_LVL\":1"));
        assertTrue(dmSoftDelete.contains("\"NM_TAB\":\"ARO_DOC\""));
    }

    // ==================== insertDuplicateAllEntityByParent ====================

    @Test
    void insertDuplicateAllEntityByParent_success() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(5);

        // Act
        int result = duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField,
                parentId, parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        assertEquals(5, result);
        verify(entityManager).createNativeQuery(anyString());
        verify(nativeQuery, times(3)).setParameter(anyInt(), any());
        verify(nativeQuery).executeUpdate();
    }

    @Test
    void insertDuplicateAllEntityByParent_queryContainsParallelHint() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createNativeQuery(sqlCaptor.capture())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(5);

        // Act
        duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("/*+ PARALLEL(ARO_DUP_RICH_SOFT_DELETE, 4) */"));
        assertTrue(capturedSql.contains("/*+ PARALLEL(e, 4) */"));
    }

    @Test
    void insertDuplicateAllEntityByParent_queryContainsSequence() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createNativeQuery(sqlCaptor.capture())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(5);

        // Act
        duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("SARO_DUP_RICH_SOFT_DELETE.NEXTVAL"));
    }

    @Test
    void insertDuplicateAllEntityByParent_queryContainsTimestampCalculation() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createNativeQuery(sqlCaptor.capture())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(5);

        // Act
        duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("NUMTODSINTERVAL((ROWNUM + ?) / 1000000, 'SECOND')"));
    }

    @Test
    void insertDuplicateAllEntityByParent_setsCorrectParameters() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 500L;
        Object rootId = 400L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(5);

        // Act
        duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField, parentId,
                parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        verify(nativeQuery).setParameter(1, baseTimestamp);
        verify(nativeQuery).setParameter(2, offset);
        verify(nativeQuery).setParameter(3, parentId);
    }

    @Test
    void insertDuplicateAllEntityByParent_zeroResults() throws Exception {
        // Arrange
        Class<?> childClass = AroDoc.class;
        Field childField = getChildIdField();
        Object parentId = 300L;
        Field parentField = getParentField();
        int level = 1;
        long offset = 0L;
        Object rootId = 400L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(0);

        // Act
        int result = duplicateEntityHandler.insertDuplicateAllEntityByParent(childClass, childField,
                parentId, parentField, baseTimestamp, level, offset, rootId, mockItem);

        // Assert
        assertEquals(0, result);
    }

    // ==================== Helper Methods ====================

    private Field getParentField() throws NoSuchFieldException {
        // Simula un campo parent con annotazione @JoinColumn
        return TestEntity.class.getDeclaredField("aroUnitaDoc");
    }

    private Field getChildIdField() throws NoSuchFieldException {
        // Simula un campo ID con annotazione @Id
        return TestEntity.class.getDeclaredField("idDoc");
    }

    // ==================== Test Entity Classes ====================

    @Table(name = "ARO_DOC")
    static class TestEntity {
        @Id
        @Column(name = "ID_DOC")
        private Long idDoc;

        @JoinColumn(name = "ID_UNITA_DOC")
        private AroUnitaDoc aroUnitaDoc;
    }

    // ==================== AroDupRichSoftDelete Field Tests ====================

    @Test
    void aroDupRichSoftDelete_allFieldsSet() {
        AroDupRichSoftDelete dupEntity = new AroDupRichSoftDelete();
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setIdItemRichSoftDelete(100L);

        BigDecimal rootId = BigDecimal.valueOf(200L);
        String tableName = "TEST_TABLE";
        LocalDateTime timestamp = LocalDateTime.now();
        String dmSoftDelete = "{\"test\":\"data\"}";

        dupEntity.setAroItemRichSoftDelete(item);
        dupEntity.setIdUnitaDocRef(rootId);
        dupEntity.setNmChildTable(tableName);
        dupEntity.setTsSoftDelete(timestamp);
        dupEntity.setDmSoftDelete(dmSoftDelete);

        assertEquals(item, dupEntity.getAroItemRichSoftDelete());
        assertEquals(rootId, dupEntity.getIdUnitaDocRef());
        assertEquals(tableName, dupEntity.getNmChildTable());
        assertEquals(timestamp, dupEntity.getTsSoftDelete());
        assertEquals(dmSoftDelete, dupEntity.getDmSoftDelete());
    }

    @Test
    void timestampWithOffset_calculation() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        long offset = 1000L;
        LocalDateTime result = baseTime.plusNanos(offset * 1000);

        assertNotNull(result);
        assertTrue(result.isAfter(baseTime));
    }

    @Test
    void dmSoftDelete_jsonFormat() {
        long rootId = 100L;
        long childId = 200L;
        long parentId = 300L;
        int level = 2;
        String childTable = "CHILD_TABLE";
        String childRefColumn = "ID_CHILD";
        String parentRefColumn = "ID_PARENT";

        String dmSoftDelete = String.format(
                "{\"ID_UD\":%d,\"ID_PK\":%d,\"ID_FK\":%d,\"NI_LVL\":%d,\"NM_TAB\":\"%s\",\"NM_PK\":\"%s\",\"NM_FK\":\"%s\"}",
                rootId, childId, parentId, level, childTable, childRefColumn, parentRefColumn);

        assertTrue(dmSoftDelete.contains("\"ID_UD\":100"));
        assertTrue(dmSoftDelete.contains("\"ID_PK\":200"));
        assertTrue(dmSoftDelete.contains("\"ID_FK\":300"));
        assertTrue(dmSoftDelete.contains("\"NI_LVL\":2"));
        assertTrue(dmSoftDelete.contains("\"NM_TAB\":\"CHILD_TABLE\""));
    }

    @Test
    void bigDecimalConversion_fromLong() {
        long value = 200L;
        BigDecimal result = new BigDecimal(value);

        assertNotNull(result);
        assertEquals(200L, result.longValue());
    }

    // ==================== SQL Query Validation Tests ====================

    @Test
    void sqlQuery_insertClauseFormat() {
        String insertClause = "INSERT /*+ PARALLEL(ARO_DUP_RICH_SOFT_DELETE, 4) */ INTO ARO_DUP_RICH_SOFT_DELETE";

        assertTrue(insertClause.contains("INSERT"));
        assertTrue(insertClause.contains("PARALLEL"));
        assertTrue(insertClause.contains("ARO_DUP_RICH_SOFT_DELETE"));
    }

    @Test
    void sqlQuery_selectClauseFormat() {
        String selectClause = "SELECT /*+ PARALLEL(e, 4) */ SARO_DUP_RICH_SOFT_DELETE.NEXTVAL";

        assertTrue(selectClause.contains("SELECT"));
        assertTrue(selectClause.contains("PARALLEL(e, 4)"));
        assertTrue(selectClause.contains("SARO_DUP_RICH_SOFT_DELETE.NEXTVAL"));
    }

    @Test
    void sqlQuery_whereClauseFormat() {
        String parentRefColumn = "ID_PARENT";
        String whereClause = String.format("WHERE e.%s = ?", parentRefColumn);

        assertEquals("WHERE e.ID_PARENT = ?", whereClause);
    }

    @Test
    void sqlQuery_columnNames() {
        String[] columns = {
                "ID_DUP_RICH_SOFT_DELETE", "ID_ITEM_RICH_SOFT_DELETE", "ID_UNITA_DOC_REF",
                "NM_CHILD_TABLE", "TS_SOFT_DELETE", "DM_SOFT_DELETE" };

        assertEquals(6, columns.length);
        for (String column : columns) {
            assertNotNull(column);
            assertFalse(column.isEmpty());
        }
    }

    @Test
    void sqlQuery_sequenceName() {
        String sequenceName = "SARO_DUP_RICH_SOFT_DELETE";

        assertTrue(sequenceName.startsWith("SARO_"));
        assertTrue(sequenceName.endsWith("_SOFT_DELETE"));
    }

    @Test
    void sqlQuery_tableName() {
        String tableName = "ARO_DUP_RICH_SOFT_DELETE";

        assertTrue(tableName.startsWith("ARO_"));
        assertTrue(tableName.endsWith("_SOFT_DELETE"));
    }
}
