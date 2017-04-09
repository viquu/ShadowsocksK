package encrypt

/**
 * Created by xingke on 2017/4/8.
 */
interface IEncrypt {

    fun encrypt(input: ByteArray): ByteArray

    fun decrypt(input: ByteArray): ByteArray

}