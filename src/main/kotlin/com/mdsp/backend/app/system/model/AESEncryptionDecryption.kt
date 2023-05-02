//package com.mdsp.backend.app.system.model
//
//import java.nio.charset.StandardCharsets
//import java.security.MessageDigest
//import java.security.NoSuchAlgorithmException
//import java.util.*
//import javax.crypto.Cipher
//import javax.crypto.spec.SecretKeySpec
//
//
///**
// * Java String Encryption Decryption Example
// * @author Ramesh Fadatare
// */
//class AESEncryptionDecryption {
//    fun prepareSecreteKey(myKey: String) {
//        var sha: MessageDigest? = null
//        try {
//            key = myKey.toByteArray(StandardCharsets.UTF_8)
//            sha = MessageDigest.getInstance("SHA-1")
//            key = sha.digest(key)
//            key = Arrays.copyOf(key, 16)
//            secretKey = SecretKeySpec(key, ALGORITHM)
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun encrypt(strToEncrypt: String, secret: String): String? {
//        try {
//            prepareSecreteKey(secret)
//            val cipher = Cipher.getInstance(ALGORITHM)
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))))
//        } catch (e: Exception) {
//            println("Error while encrypting: $e")
//        }
//        return null
//    }
//
//    fun decrypt(strToDecrypt: String?, secret: String): String? {
//        try {
//            prepareSecreteKey(secret)
//            val cipher = Cipher.getInstance(ALGORITHM)
//            cipher.init(Cipher.DECRYPT_MODE, secretKey)
//            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
//        } catch (e: Exception) {
//            println("Error while decrypting: $e")
//        }
//        return null
//    }
//
//    companion object {
//        private var secretKey: SecretKeySpec? = null
//        private lateinit var key: ByteArray
//        private const val ALGORITHM = "AES"
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val secretKey = "secrete"
//            val originalString = "javaguides"
//            val aesEncryptionDecryption = AESEncryptionDecryption()
//            val encryptedString = aesEncryptionDecryption.encrypt(originalString, secretKey)
//            val decryptedString = aesEncryptionDecryption.decrypt(encryptedString, secretKey)
//            println(originalString)
//            println(encryptedString)
//            println(decryptedString)
//        }
//    }
//}
