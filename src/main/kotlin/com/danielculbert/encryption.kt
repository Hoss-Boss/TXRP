package com.danielculbert

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.util.Base64

object EncryptionUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun encrypt(plainText: String, passphrase: String): String {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val secretKey = secretKeyFactory.generateSecret(keySpec)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        val encryptedText = cipher.doFinal(plainText.toByteArray())
        val encryptedData = salt + iv + encryptedText

        return Base64.getEncoder().encodeToString(encryptedData)
    }

    fun decrypt(encryptedText: String, passphrase: String): String {
        val decodedData = Base64.getDecoder().decode(encryptedText)

        val salt = decodedData.copyOfRange(0, SALT_LENGTH)
        val iv = decodedData.copyOfRange(SALT_LENGTH, SALT_LENGTH + 16)
        val encryptedTextBytes = decodedData.copyOfRange(SALT_LENGTH + 16, decodedData.size)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val secretKey = secretKeyFactory.generateSecret(keySpec)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        val decryptedText = cipher.doFinal(encryptedTextBytes)
        return String(decryptedText)
    }
}
