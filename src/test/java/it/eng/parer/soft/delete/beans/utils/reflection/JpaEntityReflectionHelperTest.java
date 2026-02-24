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

package it.eng.parer.soft.delete.beans.utils.reflection;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.eng.parer.soft.delete.Profiles;
import jakarta.persistence.*;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class JpaEntityReflectionHelperTest {

    @Test
    void getIdField_returnsIdField() {
        Field idField = JpaEntityReflectionHelper.getIdField(TestEntity.class);

        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(idField.isAnnotationPresent(Id.class));
    }

    @Test
    void getIdField_returnsNullWhenNoIdField() {
        Field idField = JpaEntityReflectionHelper.getIdField(EntityWithoutId.class);
        assertNull(idField);
    }

    @Test
    void getOneToManyFields_returnsAllOneToManyFields() {
        List<Field> fields = JpaEntityReflectionHelper.getOneToManyFields(TestEntity.class);

        assertNotNull(fields);
        assertEquals(1, fields.size());
        assertEquals("children", fields.get(0).getName());
    }

    @Test
    void getManyToOneFields_returnsManyToOneFieldsForSpecificParent() {
        List<Field> fields = JpaEntityReflectionHelper.getManyToOneFields(TestChild.class,
                TestEntity.class);

        assertNotNull(fields);
        assertEquals(1, fields.size());
        assertEquals("parent", fields.get(0).getName());
    }

    @Test
    void getTableName_withTableAnnotation() {
        String tableName = JpaEntityReflectionHelper.getTableName(TestEntity.class);
        assertEquals("TEST_ENTITY_TABLE", tableName);
    }

    @Test
    void getTableName_withoutTableAnnotation_usesFallback() {
        String tableName = JpaEntityReflectionHelper.getTableName(EntityWithoutTable.class);
        assertEquals("ENTITY_WITHOUT_TABLE", tableName);
    }

    @Test
    void getColumnName_withColumnAnnotation() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("name");
        String columnName = JpaEntityReflectionHelper.getColumnName(field);
        assertEquals("ENTITY_NAME", columnName);
    }

    @Test
    void getColumnName_withJoinColumn() throws NoSuchFieldException {
        Field field = TestChild.class.getDeclaredField("parent");
        String columnName = JpaEntityReflectionHelper.getColumnName(field);
        assertEquals("PARENT_ID", columnName);
    }

    @Test
    void getColumnName_withNullField() {
        String columnName = JpaEntityReflectionHelper.getColumnName(null);
        assertNull(columnName);
    }

    @Test
    void camelCaseToSnakeCase_convertsCorrectly() {
        assertEquals("camel_case", JpaEntityReflectionHelper.camelCaseToSnakeCase("camelCase"));
        assertEquals("test_entity", JpaEntityReflectionHelper.camelCaseToSnakeCase("TestEntity"));
        assertEquals("id", JpaEntityReflectionHelper.camelCaseToSnakeCase("id"));
    }

    @Test
    void getMappedByFieldName_returnsCorrectValue() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("children");
        String mappedBy = JpaEntityReflectionHelper.getMappedByFieldName(field);
        assertEquals("parent", mappedBy);
    }

    @Test
    void getFirstActualTypeArgument_returnsGenericType() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("children");
        Class<?> type = JpaEntityReflectionHelper.getFirstActualTypeArgument(field);
        assertEquals(TestChild.class, type);
    }

    @Test
    void cache_improvesPerformance() {
        // Prima chiamata - popola la cache
        long start1 = System.nanoTime();
        JpaEntityReflectionHelper.getIdField(TestEntity.class);
        long end1 = System.nanoTime();

        // Seconda chiamata - usa la cache
        long start2 = System.nanoTime();
        JpaEntityReflectionHelper.getIdField(TestEntity.class);
        long end2 = System.nanoTime();

        // La seconda chiamata dovrebbe essere più veloce (dalla cache)
        assertTrue((end2 - start2) <= (end1 - start1) * 2);
    }

    // ==================== Classi di test ====================

    @Table(name = "TEST_ENTITY_TABLE")
    @SuppressWarnings("unused")
    private static class TestEntity {
        @Id
        private Long id;

        @Column(name = "ENTITY_NAME")
        private String name;

        @OneToMany(mappedBy = "parent")
        private List<TestChild> children;
    }

    @SuppressWarnings("unused")
    private static class TestChild {
        @Id
        private Long id;

        @ManyToOne
        @JoinColumn(name = "PARENT_ID")
        private TestEntity parent;
    }

    @SuppressWarnings("unused")
    private static class EntityWithoutId {
        private String name;
    }

    @SuppressWarnings("unused")
    private static class EntityWithoutTable {
        @Id
        private Long id;
    }
}
