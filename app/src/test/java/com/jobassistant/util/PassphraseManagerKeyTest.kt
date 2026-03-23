package com.jobassistant.util

import android.security.keystore.KeyGenParameterSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Tests for PassphraseManager.getOrCreateKey() specifically.
 *
 * This class does NOT use mockkObject(PassphraseManager), so the real getOrCreateKey
 * implementation runs and is tracked by JaCoCo.  We mock the Android Keystore provider
 * (static KeyStore/KeyGenerator factories) and, where necessary, the KeyGenParameterSpec
 * Builder constructor, to avoid touching real hardware-backed crypto.
 */
class PassphraseManagerKeyTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun buildRealAesKey(): SecretKey {
        val kg = javax.crypto.KeyGenerator.getInstance("AES")
        kg.init(256)
        return kg.generateKey()
    }

    // Helper: mock KeyStore so that the given alias IS already present.
    private fun setupKeyStoreWithExistingKey(realKey: SecretKey): KeyStore {
        val mockKs = mockk<KeyStore>(relaxed = true)
        val mockEntry = mockk<KeyStore.SecretKeyEntry>()

        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKs
        every { mockKs.load(null) } just runs
        every { mockKs.containsAlias("JobAssistantDbKey") } returns true
        every { mockKs.getEntry("JobAssistantDbKey", null) } returns mockEntry
        every { mockEntry.secretKey } returns realKey

        return mockKs
    }

    @Test
    fun `getOrCreateKey returns existing key from keystore`() {
        val realKey = buildRealAesKey()
        setupKeyStoreWithExistingKey(realKey)

        val method = PassphraseManager::class.java.getDeclaredMethod("getOrCreateKey")
        method.isAccessible = true
        val result = method.invoke(PassphraseManager) as SecretKey

        assertNotNull(result)
    }

    @Test
    fun `getOrCreateKey returns same key instance when key already in keystore`() {
        val realKey = buildRealAesKey()
        setupKeyStoreWithExistingKey(realKey)

        val method = PassphraseManager::class.java.getDeclaredMethod("getOrCreateKey")
        method.isAccessible = true
        val result = method.invoke(PassphraseManager) as SecretKey

        // Returned key should equal the one we put in the mock keystore
        assert(result === realKey) { "Returned key should be the same instance from the keystore" }
    }

    @Test
    fun `getOrCreateKey creates new key when none exists`() {
        val realKey = buildRealAesKey()

        val mockKs = mockk<KeyStore>(relaxed = true)
        val mockKg = mockk<KeyGenerator>(relaxed = true)

        mockkStatic(KeyStore::class)
        mockkStatic(KeyGenerator::class)
        // Mock the Android KeyGenParameterSpec.Builder constructor chain so the
        // production code's `keyGenerator.init(KeyGenParameterSpec.Builder(...).build())`
        // doesn't crash on an Android stub that throws RuntimeException("Stub!").
        mockkConstructor(KeyGenParameterSpec.Builder::class)

        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKs
        every { mockKs.load(null) } just runs
        // First call: key does not exist → triggers key generation path
        every { mockKs.containsAlias("JobAssistantDbKey") } returns false

        every { KeyGenerator.getInstance("AES", "AndroidKeyStore") } returns mockKg
        every { mockKg.generateKey() } returns realKey
        every { mockKg.init(any<java.security.spec.AlgorithmParameterSpec>()) } just runs

        // Builder chain — each method returns a fresh relaxed mock so chaining works
        every { anyConstructed<KeyGenParameterSpec.Builder>().setBlockModes(any()) } returns mockk(relaxed = true)
        every { anyConstructed<KeyGenParameterSpec.Builder>().setEncryptionPaddings(any()) } returns mockk(relaxed = true)
        every { anyConstructed<KeyGenParameterSpec.Builder>().setKeySize(any()) } returns mockk(relaxed = true)
        every { anyConstructed<KeyGenParameterSpec.Builder>().build() } returns mockk(relaxed = true)

        val method = PassphraseManager::class.java.getDeclaredMethod("getOrCreateKey")
        method.isAccessible = true
        val result = method.invoke(PassphraseManager)

        assertNotNull(result)
    }
}
