package encrypt

import com.xlvecle.socks5.Cipher.RC4MD5Cipher
import com.xlvecle.socks5.Cipher.md5Digest
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.crypto.Cipher


/**
 * Created by xingke on 2017/4/8.
 */

class Encryptor(password: ByteArray, encryptMethod: EncryptMethod): IEncrypt {

    enum class Operation {
        ENCRYPT,
        DECRYPT
    }

    var ivSent = false
    var cipher: Cipher
    var decipher: Cipher? = null
    var cipherIv: ByteArray = randomByteArray(EncryptMethod.RC4_MD5.ivLen)
    var decipherIv: ByteArray? = null
    var key: ByteArray = evpBytesToKey(password, encryptMethod.keyLen, encryptMethod.ivLen)

    init {
        cipher = RC4MD5Cipher(key, cipherIv, Operation.ENCRYPT).rc4Cipher
    }

    override fun encrypt(input: ByteArray): ByteArray {
        if (input.isEmpty()) {
            return input
        }

        if (ivSent) {
            return cipher.update(input)

        } else {
            ivSent = true
            return cipherIv.plus(cipher.update(input))
        }

    }

    override fun decrypt(input: ByteArray): ByteArray {
        var inputBody = input
        if (decipherIv == null) {
            decipherIv = input.slice(0 until EncryptMethod.RC4_MD5.ivLen).toByteArray()
            inputBody = input.slice(EncryptMethod.RC4_MD5.ivLen until input.size).toByteArray()
            decipher = RC4MD5Cipher(key, decipherIv!!, Operation.DECRYPT).rc4Cipher
        }
        val final = decipher!!.update(inputBody)
        return final
    }
}

fun evpBytesToKey(password: ByteArray, keyLen: Int, ivLen: Int): ByteArray {
    var m = arrayOf<ByteArray>()
    var i = 0
    var count = 0
    while (count < keyLen + ivLen) {
        var data = password
        if (i > 0) {
            data = m[i - 1].plus(password)
        }

        val dataDigest = md5Digest(data)
        m = m.plus(dataDigest)
        count += dataDigest.size
        i++
    }

    val ms = m.reduce(ByteArray::plus)
    val key = ms.slice(0 until keyLen).toByteArray()
    val iv = ms.slice(keyLen until ivLen).toByteArray()
    return key
}

fun randomByteArray(length: Int): ByteArray {
    var bytes = ByteArray(length)
    ThreadLocalRandom.current().nextBytes(bytes)
    return bytes
}

fun main(args: Array<String>) {
    val plain = "hello".toByteArray()
    val encryptor = Encryptor("key".toByteArray(), EncryptMethod.RC4_MD5)
    val decryptor = Encryptor("key".toByteArray(), EncryptMethod.RC4_MD5)
    val cipher = encryptor.encrypt(plain)
    val plain2 = decryptor.decrypt(cipher)
    println(Arrays.equals(plain, plain2))
}