package com.xlvecle.socks5.Cipher

import encrypt.Encryptor
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


/**
 * Created by xingke on 2017/4/8.
 */

class RC4MD5Cipher(key: ByteArray, iv: ByteArray, operation: Encryptor.Operation) {
    val rc4Cipher: javax.crypto.Cipher

    init {
        val rc4Key = md5Digest(Arrays.asList<ByteArray>(key, iv))
        try {
            rc4Cipher = javax.crypto.Cipher.getInstance("RC4")
            val keySpec = SecretKeySpec(rc4Key, "RC4")
            if (operation === Encryptor.Operation.ENCRYPT) {
                rc4Cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec)
            } else if (operation === Encryptor.Operation.DECRYPT) {
                rc4Cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec)
            } else {
                throw IllegalArgumentException("illegal operation: " + operation)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}

fun md5Digest(input: ByteArray): ByteArray {
    try {
        val md5 = MessageDigest.getInstance("MD5")
        return md5.digest(input)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

}

fun md5Digest(inputs: List<ByteArray>): ByteArray {
    try {
        val md5 = MessageDigest.getInstance("MD5")
        inputs.forEach{ it -> md5.update(it)}
        return md5.digest()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

}
