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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.eng.parer.soft.delete.Profiles;
import it.eng.parer.soft.delete.beans.annotations.ForceCompleteMode;
import jakarta.persistence.Id;

@QuarkusTest
@TestProfile(Profiles.Core.class)
class ForceCompleteModeHelperTest {

    @AfterEach
    void cleanup() {
        ForceCompleteModeHelper.clearCache();
    }

    @Test
    void isForceCompleteMode_withAnnotation_returnsTrue() {
        boolean result = ForceCompleteModeHelper.isForceCompleteMode(EntityWithForceComplete.class);
        assertTrue(result);
    }

    @Test
    void isForceCompleteMode_withoutAnnotation_returnsFalse() {
        boolean result = ForceCompleteModeHelper
                .isForceCompleteMode(EntityWithoutForceComplete.class);
        assertFalse(result);
    }

    @Test
    void isForceCompleteMode_withNullClass_returnsFalse() {
        boolean result = ForceCompleteModeHelper.isForceCompleteMode(null);
        assertFalse(result);
    }

    @Test
    void isForceCompleteMode_usesCache() {
        // Prima chiamata - popola la cache
        long start1 = System.nanoTime();
        boolean result1 = ForceCompleteModeHelper
                .isForceCompleteMode(EntityWithForceComplete.class);
        long end1 = System.nanoTime();

        // Seconda chiamata - usa la cache
        long start2 = System.nanoTime();
        boolean result2 = ForceCompleteModeHelper
                .isForceCompleteMode(EntityWithForceComplete.class);
        long end2 = System.nanoTime();

        assertEquals(result1, result2);
        // La seconda chiamata dovrebbe essere più veloce
        assertTrue((end2 - start2) <= (end1 - start1) * 2);
    }

    @Test
    void clearCache_removesAllEntries() {
        // Popola la cache
        ForceCompleteModeHelper.isForceCompleteMode(EntityWithForceComplete.class);
        ForceCompleteModeHelper.isForceCompleteMode(EntityWithoutForceComplete.class);

        // Pulisci la cache
        ForceCompleteModeHelper.clearCache();

        // Verifica che le chiamate successive funzionino ancora
        assertTrue(ForceCompleteModeHelper.isForceCompleteMode(EntityWithForceComplete.class));
        assertFalse(ForceCompleteModeHelper.isForceCompleteMode(EntityWithoutForceComplete.class));
    }

    @Test
    void isForceCompleteMode_multipleCallsSameResult() {
        Class<?> clazz = EntityWithForceComplete.class;

        boolean result1 = ForceCompleteModeHelper.isForceCompleteMode(clazz);
        boolean result2 = ForceCompleteModeHelper.isForceCompleteMode(clazz);
        boolean result3 = ForceCompleteModeHelper.isForceCompleteMode(clazz);

        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertTrue(result1);
    }

    @Test
    void cache_handlesMultipleClasses() {
        assertTrue(ForceCompleteModeHelper.isForceCompleteMode(EntityWithForceComplete.class));
        assertFalse(ForceCompleteModeHelper.isForceCompleteMode(EntityWithoutForceComplete.class));
        assertFalse(ForceCompleteModeHelper.isForceCompleteMode(AnotherEntity.class));
    }

    // ==================== Classi di test ====================

    @ForceCompleteMode
    @SuppressWarnings("unused")
    private static class EntityWithForceComplete {
        @Id
        private Long id;
    }

    @SuppressWarnings("unused")
    private static class EntityWithoutForceComplete {
        @Id
        private Long id;
    }

    @SuppressWarnings("unused")
    private static class AnotherEntity {
        @Id
        private Long id;
    }
}
