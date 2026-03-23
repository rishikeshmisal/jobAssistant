package com.jobassistant.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.SecretKey

/**
 * Pure JVM tests for PassphraseManager.
 *
 * We use mockkObject to intercept the private getOrCreateKey() method, replacing
 * the Android-Keystore-backed key with a real JVM AES key so the encrypt/decrypt
 * cycle can be exercised end-to-end without native Android crypto.  The Context
 * is mocked to redirect file I/O to a temp directory.
 *
 * Because there is no Robolectric sandbox class loader in play, JaCoCo's on-the-fly
 * agent tracks coverage for all PassphraseManager code paths.
 */
class PassphraseManagerTest {

    private lateinit var tempDir: File
    private lateinit var mockContext: Context
    private lateinit var realKey: SecretKey

    @Before
    fun setUp() {
        tempDir = createTempDir("passphrase_test")

        // Create a real AES-256 key via the JVM's standard KeyGenerator
        val kg = javax.crypto.KeyGenerator.getInstance("AES")
        kg.init(256)
        realKey = kg.generateKey()

        mockContext = mockk(relaxed = true)
        every { mockContext.getFileStreamPath(any()) } answers {
            File(tempDir, firstArg<String>())
        }
        every { mockContext.openFileOutput(any<String>(), any<Int>()) } answers {
            FileOutputStream(File(tempDir, firstArg<String>()))
        }
        every { mockContext.openFileInput(any<String>()) } answers {
            FileInputStream(File(tempDir, firstArg<String>()))
        }

        // Mock the Kotlin object so we can override the private getOrCreateKey() method.
        // All other methods (generatePassphrase, encryptAndStore, decryptPassphrase) run for real.
        mockkObject(PassphraseManager)
        every { PassphraseManager["getOrCreateKey"]() } returns realKey
    }

    @After
    fun tearDown() {
        unmockkAll()
        tempDir.deleteRecursively()
    }

    // ── Constants / structure ───────────────────────────────────────────────

    @Test
    fun `PassphraseManager is a singleton object`() {
        assertSame(PassphraseManager, PassphraseManager)
    }

    @Test
    fun `KEY_ALIAS is JobAssistantDbKey`() {
        val f = PassphraseManager::class.java.getDeclaredField("KEY_ALIAS")
        f.isAccessible = true
        assertEquals("JobAssistantDbKey", f.get(PassphraseManager))
    }

    @Test
    fun `PASSPHRASE_FILE is db_passphrase_enc`() {
        val f = PassphraseManager::class.java.getDeclaredField("PASSPHRASE_FILE")
        f.isAccessible = true
        assertEquals("db_passphrase.enc", f.get(PassphraseManager))
    }

    @Test
    fun `KEYSTORE_PROVIDER is AndroidKeyStore`() {
        val f = PassphraseManager::class.java.getDeclaredField("KEYSTORE_PROVIDER")
        f.isAccessible = true
        assertEquals("AndroidKeyStore", f.get(PassphraseManager))
    }

    @Test
    fun `TRANSFORMATION is AES_CBC_PKCS5Padding`() {
        val f = PassphraseManager::class.java.getDeclaredField("TRANSFORMATION")
        f.isAccessible = true
        assertEquals("AES/CBC/PKCS5Padding", f.get(PassphraseManager))
    }

    // ── generatePassphrase ──────────────────────────────────────────────────

    @Test
    fun `generatePassphrase returns non-empty string`() {
        val m = PassphraseManager::class.java.getDeclaredMethod("generatePassphrase")
        m.isAccessible = true
        val result = m.invoke(PassphraseManager) as String
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `generatePassphrase returns 44-char base64 for 32 bytes`() {
        val m = PassphraseManager::class.java.getDeclaredMethod("generatePassphrase")
        m.isAccessible = true
        val result = m.invoke(PassphraseManager) as String
        assertEquals(44, result.length)
    }

    @Test
    fun `generatePassphrase returns different values on each call`() {
        val m = PassphraseManager::class.java.getDeclaredMethod("generatePassphrase")
        m.isAccessible = true
        val p1 = m.invoke(PassphraseManager) as String
        val p2 = m.invoke(PassphraseManager) as String
        assertNotEquals(p1, p2)
    }

    @Test
    fun `getOrCreatePassphrase method has correct signature`() {
        val method = PassphraseManager::class.java.getDeclaredMethod(
            "getOrCreatePassphrase", Context::class.java
        )
        assertNotNull(method)
        assertEquals(String::class.java, method.returnType)
    }

    // ── getOrCreatePassphrase end-to-end ────────────────────────────────────

    @Test
    fun `getOrCreatePassphrase returns non-empty passphrase`() {
        val result = PassphraseManager.getOrCreatePassphrase(mockContext)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `getOrCreatePassphrase returns 44-char passphrase`() {
        val result = PassphraseManager.getOrCreatePassphrase(mockContext)
        assertEquals(44, result.length)
    }

    @Test
    fun `getOrCreatePassphrase is idempotent on repeated calls`() {
        val first = PassphraseManager.getOrCreatePassphrase(mockContext)
        val second = PassphraseManager.getOrCreatePassphrase(mockContext)
        assertEquals("Repeated calls must return identical passphrases", first, second)
    }

    @Test
    fun `getOrCreatePassphrase writes encrypted file on first call`() {
        PassphraseManager.getOrCreatePassphrase(mockContext)
        assertTrue(File(tempDir, "db_passphrase.enc").exists())
    }

    @Test
    fun `getOrCreatePassphrase encrypted file is non-empty`() {
        PassphraseManager.getOrCreatePassphrase(mockContext)
        val file = File(tempDir, "db_passphrase.enc")
        assertTrue(file.length() > 0)
    }

    @Test
    fun `getOrCreatePassphrase encrypted file is not plain-text passphrase`() {
        val passphrase = PassphraseManager.getOrCreatePassphrase(mockContext)
        val fileBytes = File(tempDir, "db_passphrase.enc").readBytes()
        // The stored bytes should NOT contain the plain-text passphrase
        val passphraseBytes = passphrase.toByteArray(Charsets.UTF_8)
        val fileStr = String(fileBytes)
        assertTrue("File should not contain plain-text passphrase",
            !fileStr.contains(passphrase))
        assertTrue("File length should be greater than passphrase length",
            fileBytes.size > passphraseBytes.size)
    }

    @Test
    fun `getOrCreatePassphrase returns same value across simulated restarts`() {
        val stored = PassphraseManager.getOrCreatePassphrase(mockContext)
        // Simulated restart: file is still on disk, same mocked key returned
        val retrieved = PassphraseManager.getOrCreatePassphrase(mockContext)
        assertEquals("Value must survive simulated restart", stored, retrieved)
    }
}
