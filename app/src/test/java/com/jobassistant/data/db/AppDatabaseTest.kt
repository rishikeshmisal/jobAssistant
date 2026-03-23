package com.jobassistant.data.db

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for AppDatabase.
 *
 * SQLCipher's SupportFactory requires native (.so) libraries unavailable in a JVM
 * unit-test environment.  These tests use a plain in-memory Room database to verify
 * that Room initialises correctly.  The SQLCipher encryption is verified by the
 * Phase 1 MVP Checkpoint on an emulator/device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        // Force the database to open so isOpen returns true
        db.openHelper.writableDatabase
    }

    @After
    fun tearDown() {
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }

    @Test
    fun `AppDatabase builds successfully in-memory`() {
        assertNotNull(db)
    }

    @Test
    fun `AppDatabase is open after accessing writableDatabase`() {
        assertTrue(db.isOpen)
    }

    @Test
    fun `AppDatabase close leaves database closed`() {
        assertTrue(db.isOpen)
        db.close()
        assertFalse(db.isOpen)
    }

    @Test
    fun `AppDatabase companion object create method exists`() {
        // Companion object method is on AppDatabase$Companion in Java reflection
        val companionClass = AppDatabase::class.java.declaredClasses
            .firstOrNull { it.simpleName == "Companion" }
        assertNotNull("Companion class must exist", companionClass)
        val method = companionClass!!.getDeclaredMethod(
            "create", Context::class.java, String::class.java, Converters::class.java
        )
        assertNotNull("create method must exist on Companion", method)
        assertEquals(AppDatabase::class.java, method.returnType)
    }

    @Test
    fun `second in-memory database instance is independent`() {
        val db2 = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        db2.openHelper.writableDatabase
        assertNotNull(db2)
        assertTrue(db2.isOpen)
        db2.close()
    }
}
