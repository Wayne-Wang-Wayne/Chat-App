package com.example.chatapp.util

import android.util.Base64
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {

    fun decodeBase64(originString: String?): ByteArray {
        return Base64.decode(originString, Base64.DEFAULT)
    }

    fun encrypt(data: String?, key: String?, iv: String?): String {
        val ivSpec = IvParameterSpec(decodeBase64(iv))
        val newKey = SecretKeySpec(decodeBase64(key), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
        return Base64.encodeToString(cipher.doFinal(data?.toByteArray()), Base64.DEFAULT)
            .replace("\n", "")
    }

    fun decrypt(data: String, key: String, iv: String): String {
        val ivSpec: AlgorithmParameterSpec = IvParameterSpec(decodeBase64(iv))
        val newKey = SecretKeySpec(decodeBase64(key), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
        return String(cipher.doFinal(decodeBase64(data)))
    }
}