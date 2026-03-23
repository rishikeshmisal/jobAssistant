package com.jobassistant.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object PassphraseManager {

    // "_gcm" suffix distinguishes this from any legacy CBC key created before this fix.
    private const val KEY_ALIAS = "JobAssistantDbKey_gcm"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val PASSPHRASE_FILE = "db_passphrase.enc"
    // AES/GCM/NoPadding is fully handled by the AndroidKeyStoreProvider on all
    // Android versions — it never falls through to BouncyCastle, which would fail
    // because hardware-backed keys return null from getEncoded().
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    fun getOrCreatePassphrase(context: Context): String {
        val passphraseFile = context.getFileStreamPath(PASSPHRASE_FILE)
        return if (passphraseFile.exists()) {
            runCatching { decryptPassphrase(context) }.getOrElse {
                // Stale file from a previous key (e.g. after reinstall that wiped Keystore).
                // Reset: delete the old file + old key and start fresh.
                passphraseFile.delete()
                context.getFileStreamPath("jobassistant.db").delete()
                val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
                if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
                val passphrase = generatePassphrase()
                encryptAndStore(context, passphrase)
                passphrase
            }
        } else {
            val passphrase = generatePassphrase()
            encryptAndStore(context, passphrase)
            passphrase
        }
    }

    private fun generatePassphrase(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encryptAndStore(context: Context, passphrase: String) {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))

        context.openFileOutput(PASSPHRASE_FILE, Context.MODE_PRIVATE).use { out ->
            // Write IV length (4 bytes), IV, then encrypted data (includes GCM auth tag)
            val ivLen = iv.size
            out.write(byteArrayOf(
                (ivLen shr 24).toByte(),
                (ivLen shr 16).toByte(),
                (ivLen shr 8).toByte(),
                ivLen.toByte()
            ))
            out.write(iv)
            out.write(encrypted)
        }
    }

    private fun decryptPassphrase(context: Context): String {
        val bytes = context.openFileInput(PASSPHRASE_FILE).use { it.readBytes() }
        val ivLen = ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
        val iv = bytes.copyOfRange(4, 4 + ivLen)
        val encrypted = bytes.copyOfRange(4 + ivLen, bytes.size)

        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
