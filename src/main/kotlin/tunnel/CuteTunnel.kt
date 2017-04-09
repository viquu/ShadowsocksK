package tunnel

import com.xlvecle.socks5.decodeCommandRequestToResponse
import com.xlvecle.socks5.encodeCommandResponseToByteBuf
import encrypt.Encryptor
import io.netty.handler.codec.socksx.v5.Socks5AddressType
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket


/**
 * Created by xingke on 2017/4/8.
 */

class CuteTunnel(var serverSocket: NetSocket, var clientSocket: NetSocket, var encryptor: Encryptor) {

    lateinit var header: ByteArray

    fun tunnel() {
        serverSocket.closeHandler { v -> clientSocket.close() }
        clientSocket.closeHandler { v -> serverSocket.close() }

        serverSocket.exceptionHandler { e ->
            e.printStackTrace()
            serverSocket.close()
        }
        clientSocket.exceptionHandler { e ->
            e.printStackTrace()
            clientSocket.close()
        }

        clientSocket.handler({ buffer ->
            val socks5Stage = socks5Route(buffer)
            when (socks5Stage) {
                Socks5Stage.AUTH -> {
                    val outBuffer = Buffer.buffer().appendByte(5).appendByte(0)
                    clientSocket.write(outBuffer)
//                        println("收到请求，认证通过，等待请求信息")
                }
                Socks5Stage.CONNECT -> {
                    val response = decodeCommandRequestToResponse(buffer)
                    val outBuffer = encodeCommandResponseToByteBuf(response)
                    clientSocket.write(outBuffer)
                    header = buffer.getBytes(3, buffer.length())
                    val encrypted = encryptor.encrypt(header)
                    val encryptedBuffer = Buffer.buffer().appendBytes(encrypted)
                    serverSocket.write(encryptedBuffer)

                }

                Socks5Stage.REQUEST -> {
                    val encrypted = encryptor.encrypt(buffer.bytes)
                    val encryptedBuffer = Buffer.buffer().appendBytes(encrypted)
                    serverSocket.write(encryptedBuffer)
                }

                else -> {
                    clientSocket.close()
                    serverSocket.close()
                }
            }
        })

        serverSocket.handler({ buffer ->
            val decryptBytes = encryptor.decrypt(buffer.bytes)
            Socks5AddressType.DOMAIN
            clientSocket.write(Buffer.buffer(decryptBytes))
        })

    }
}

fun socks5Route(buffer: Buffer): Socks5Stage {
    if (buffer.length() < 3) {
        return Socks5Stage.REQUEST
    }

    val VER = buffer.getByte(0)
    val CMD = buffer.getByte(1)
    val RSV = buffer.getByte(2)

    if (buffer.length() == 3) {
        //目前只支持不认证的socks5
        if (VER == 0x05.toByte() && CMD == 0x01.toByte() && RSV == 0x00.toByte()) {
            return Socks5Stage.AUTH
        }
    } else {
        if (VER == 0x05.toByte() && CMD == 0x01.toByte() && RSV == 0x00.toByte()) {
            //目前只支持CONNECT请求
            return Socks5Stage.CONNECT
        } else {
            return Socks5Stage.REQUEST
        }
    }

    return Socks5Stage.REQUEST
}

enum class Socks5Stage {
    AUTH, CONNECT, REQUEST, UNKNOWN
}
