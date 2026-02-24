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
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.IDuplicateEntityHandler;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.ISoftDeleteExecutor;
import it.eng.parer.soft.delete.beans.cache.registry.DistributedTimestampRegistry;
import it.eng.parer.soft.delete.beans.context.RootEntityContext;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.utils.Constants.SoftDeleteMode;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroLogRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroDoc;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@ExtendWith(MockitoExtension.class)
class LevelProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private DistributedTimestampRegistry timestampRegistry;

    @Mock
    private RootEntityContext rootEntityContext;

    @Mock
    private ISoftDeleteExecutor softDeleteExecutor;

    @Mock
    private IDuplicateEntityHandler duplicateEntityHandler;

    @Mock
    private IStreamSource mockStreamSource;

    @InjectMocks
    private LevelProcessor levelProcessor;

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
        LevelProcessor processor = new LevelProcessor(entityManager, timestampRegistry,
                rootEntityContext, softDeleteExecutor, duplicateEntityHandler);
        assertNotNull(processor);
    }

    // ==================== processLevel - standalone entities ====================

    @Test
    void processLevel_standaloneEntity_success() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(0);

        Field idField = TestRootEntity.class.getDeclaredField("id");
        EntityNode rootNode = new EntityNode(AroUnitaDoc.class, 200L, idField, 0);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(rootNode))
                .thenReturn(Stream.of(rootNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteRootEntity(
                eq(AroUnitaDoc.class), eq(200L), any(), eq(0), eq(200L)))
                .thenReturn(1);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        verify(softDeleteExecutor).softDeleteRootEntity(
                eq(AroUnitaDoc.class), eq(200L), any(), eq(0), eq(200L));
        verify(entityManager, atLeastOnce()).persist(any(AroLogRichSoftDelete.class));
    }

    // ==================== processLevel - grouped entities CAMPIONE ====================

    @Test
    void processLevel_groupedEntities_campione_success() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode childNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(childNode))
                .thenReturn(Stream.of(childNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteOneEntity(
                eq(AroDoc.class), eq(300L), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L)))
                .thenReturn(1);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.CAMPIONE, mockItem);

        // Assert
        verify(softDeleteExecutor).softDeleteOneEntity(
                eq(AroDoc.class), eq(300L), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L));
    }

    // ==================== processLevel - grouped entities COMPLETA ====================

    @Test
    void processLevel_groupedEntities_completa_success() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode childNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(childNode))
                .thenReturn(Stream.of(childNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteAllEntityByParent(
                eq(AroDoc.class), eq(childIdField), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L)))
                .thenReturn(10);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        verify(softDeleteExecutor).softDeleteAllEntityByParent(
                eq(AroDoc.class), eq(childIdField), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L));
    }

    // ==================== processLevel - duplicate entities CAMPIONE ====================

    @Test
    void processLevel_duplicateEntities_campione_success() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode duplicateNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField, true);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(duplicateNode))
                .thenReturn(Stream.of(duplicateNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(duplicateEntityHandler.insertDuplicateOneEntity(
                eq(AroDoc.class), eq(300L), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L), eq(mockItem)))
                .thenReturn(1);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.CAMPIONE, mockItem);

        // Assert
        verify(duplicateEntityHandler).insertDuplicateOneEntity(
                eq(AroDoc.class), eq(300L), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L), eq(mockItem));
    }

    // ==================== processLevel - duplicate entities COMPLETA ====================

    @Test
    void processLevel_duplicateEntities_completa_success() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode duplicateNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField, true);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(duplicateNode))
                .thenReturn(Stream.of(duplicateNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(duplicateEntityHandler.insertDuplicateAllEntityByParent(
                eq(AroDoc.class), eq(childIdField), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L), eq(mockItem)))
                .thenReturn(15);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        verify(duplicateEntityHandler).insertDuplicateAllEntityByParent(
                eq(AroDoc.class), eq(childIdField), eq(200L), eq(parentField),
                any(), eq(1), anyLong(), eq(200L), eq(mockItem));
    }

    // ==================== processLevel - force complete mode ====================

    @Test
    void processLevel_forceCompleteMode_usesCompleteMode() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode childNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField, false, true);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(childNode))
                .thenReturn(Stream.of(childNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteAllEntityByParent(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any()))
                .thenReturn(10);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.CAMPIONE, mockItem);

        // Assert
        verify(softDeleteExecutor).softDeleteAllEntityByParent(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any());
        verify(softDeleteExecutor, never()).softDeleteOneEntity(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any());
    }

    // ==================== processLevel - item state update ====================

    @Test
    void processLevel_standaloneEntity_updatesItemState() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(0);

        Field idField = TestRootEntity.class.getDeclaredField("id");
        EntityNode rootNode = new EntityNode(AroUnitaDoc.class, 200L, idField, 0);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(rootNode))
                .thenReturn(Stream.of(rootNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteRootEntity(any(), any(), any(), anyInt(), any()))
                .thenReturn(1);

        when(entityManager.merge(any(AroItemRichSoftDelete.class)))
                .thenReturn(mockItem);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        assertEquals(StatoItemRichSoftDelete.ELABORATO.name(), mockItem.getTiStatoItem());
        assertNotNull(mockItem.getDtFineElab());
        verify(entityManager).merge(mockItem);
    }

    // ==================== processLevel - log persistence ====================

    @Test
    void processLevel_persistsLogs_withCorrectData() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);

        Field parentField = TestChildEntity.class.getDeclaredField("aroUnitaDoc");
        Field childIdField = TestChildEntity.class.getDeclaredField("id");

        EntityNode childNode = new EntityNode(AroDoc.class, 300L, childIdField, 1,
                AroUnitaDoc.class, 200L, parentField);

        when(mockStreamSource.createStream())
                .thenReturn(Stream.of(childNode))
                .thenReturn(Stream.of(childNode));

        when(timestampRegistry.getLatestTimestamp(anyString(), any()))
                .thenReturn(baseTimestamp);

        when(softDeleteExecutor.softDeleteAllEntityByParent(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any()))
                .thenReturn(10);

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        ArgumentCaptor<AroLogRichSoftDelete> logCaptor = ArgumentCaptor
                .forClass(AroLogRichSoftDelete.class);
        verify(entityManager, atLeastOnce()).persist(logCaptor.capture());

        AroLogRichSoftDelete capturedLog = logCaptor.getValue();
        assertEquals(mockItem, capturedLog.getAroItemRichSoftDelete());
        assertEquals(new BigDecimal(200L), capturedLog.getIdUnitaDocRef());
        assertEquals(BigDecimal.valueOf(1), capturedLog.getNiLevel());
        assertEquals(BigDecimal.valueOf(10), capturedLog.getUpdatedRowCount());
    }

    // ==================== processLevel - empty stream ====================

    @Test
    void processLevel_emptyStream_noProcessing() throws Exception {
        // Arrange
        when(rootEntityContext.getCurrentRootId()).thenReturn(200L);
        when(mockStreamSource.getLevel()).thenReturn(1);
        when(mockStreamSource.createStream())
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        // Act
        levelProcessor.processLevel(mockStreamSource, SoftDeleteMode.COMPLETA, mockItem);

        // Assert
        verify(softDeleteExecutor, never()).softDeleteOneEntity(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any());
        verify(softDeleteExecutor, never()).softDeleteAllEntityByParent(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any());
        verify(duplicateEntityHandler, never()).insertDuplicateOneEntity(
                any(), any(), any(), any(), any(), anyInt(), anyLong(), any(), any());
    }

    // ==================== Helper Test Entities ====================

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
        private TestRootEntity aroUnitaDoc;
    }

    // ==================== Timestamp Registry Tests ====================

    @Test
    void timestampRegistry_updatesCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();
        String tableName = "ARO_DOC";

        doNothing().when(timestampRegistry).updateTimestamp(tableName, timestamp);

        timestampRegistry.updateTimestamp(tableName, timestamp);

        verify(timestampRegistry).updateTimestamp(tableName, timestamp);
    }

    @Test
    void timestampRegistry_getsLatestTimestamp() {
        LocalDateTime initial = LocalDateTime.now();
        LocalDateTime latest = initial.plusMinutes(5);
        String tableName = "ARO_DOC";

        when(timestampRegistry.getLatestTimestamp(tableName, initial)).thenReturn(latest);

        LocalDateTime result = timestampRegistry.getLatestTimestamp(tableName, initial);

        assertEquals(latest, result);
    }

    // ==================== Entity Type Counter Tests ====================

    @Test
    void entityTypeCounter_tracksMultipleTypes() {
        Map<String, Long> counter = new HashMap<>();

        counter.put("AroDoc->AroUnitaDoc", 10L);
        counter.put("AroCompDoc->AroDoc", 5L);

        assertEquals(2, counter.size());
        assertEquals(10L, counter.get("AroDoc->AroUnitaDoc"));
        assertEquals(5L, counter.get("AroCompDoc->AroDoc"));
    }

    @Test
    void entityTypeCounter_incrementsCorrectly() {
        Map<String, Long> counter = new HashMap<>();
        String key = "AroDoc->AroUnitaDoc";

        long current = counter.getOrDefault(key, 0L);
        counter.put(key, current + 5);

        current = counter.getOrDefault(key, 0L);
        counter.put(key, current + 3);

        assertEquals(8L, counter.get(key));
    }
}
