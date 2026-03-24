package com.jobassistant.data.db

import com.jobassistant.data.db.AppDatabase.Companion.MIGRATION_1_2
import com.jobassistant.data.db.AppDatabase.Companion.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit-level tests for AppDatabase migration objects.
 *
 * Full on-device migration tests require Room MigrationTestHelper (androidTest).
 * These tests verify the migration SQL statements are defined and the version numbers
 * are correct without needing a real SQLite database.
 */
class JobApplicationMigrationTest {

    @Test
    fun `MIGRATION_1_2 starts at version 1`() {
        assertEquals(1, MIGRATION_1_2.startVersion)
    }

    @Test
    fun `MIGRATION_1_2 ends at version 2`() {
        assertEquals(2, MIGRATION_1_2.endVersion)
    }

    @Test
    fun `MIGRATION_2_3 starts at version 2`() {
        assertEquals(2, MIGRATION_2_3.startVersion)
    }

    @Test
    fun `MIGRATION_2_3 ends at version 3`() {
        assertEquals(3, MIGRATION_2_3.endVersion)
    }

    @Test
    fun `migrations form a sequential chain from 1 to 3`() {
        assertEquals(MIGRATION_1_2.endVersion, MIGRATION_2_3.startVersion)
    }
}
