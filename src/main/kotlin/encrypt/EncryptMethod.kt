package encrypt

/**
 * Created by xingke on 2017/4/8.
 */
enum class EncryptMethod(val methodName: String, val keyLen: Int, val ivLen: Int)  {

    RC4_MD5("rc4-md5", 16, 16)

}