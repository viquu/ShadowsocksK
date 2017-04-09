package com.xlvecle.socks5
import encrypt.EncryptMethod
import encrypt.Encryptor
import io.netty.buffer.Unpooled
import io.netty.handler.codec.socksx.v5.*
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetClientOptions
import tunnel.CuteTunnel
import tunnel.Socks5Stage
import tunnel.socks5Route
import java.io.File


/**
 * Created by xingke on 2017/4/5.
 */
class Server(serverOption: ServerOption) : io.vertx.core.AbstractVerticle()  {

    val LOCALPORT: Int = serverOption.localPort
    val REMOTEPORT: Int = serverOption.remotePort
    val REMOTEADDRESS: String = serverOption.remoteAddress
    val PASSWORD: String = serverOption.password

    init {

    }

    override fun start() {
        val clientToRemote = vertx.createNetClient(NetClientOptions().setConnectTimeout(200))
        val serverAtLocal = vertx.createNetServer()

        serverAtLocal.connectHandler({ clientSock ->
            var socks5HandShake: Buffer? = null
            clientSock.handler({
                buffer ->
                val socks5Stage = socks5Route(buffer)
                if (socks5Stage == Socks5Stage.AUTH) {
                    socks5HandShake = Buffer.buffer().appendByte(5).appendByte(0)
                }
            })
            clientToRemote.connect(REMOTEPORT, REMOTEADDRESS, {
                requestRemote ->
                if (requestRemote.succeeded()) {
                    if (socks5HandShake != null) {
                        clientSock.write(socks5HandShake)
                    } else {
                        return@connect
                    }
                    val encryptor = Encryptor(PASSWORD.toByteArray(), EncryptMethod.RC4_MD5)
                    val serverSock = requestRemote.result()
                    CuteTunnel(serverSock, clientSock, encryptor).tunnel()
                }
            })
        }).listen(LOCALPORT)

        println("Server started")
    }
}

fun decodeCommandRequestToResponse(buffer: Buffer): Socks5CommandResponse {
    try {
        val addressType = buffer.getBytes(3, 4)[0]
        val address = buffer.getBytes(4, buffer.length())
        val socks5AddressType = Socks5AddressType.valueOf(addressType)
        val finalAddress = Socks5AddressDecoder.DEFAULT.decodeAddress(socks5AddressType, Unpooled.copiedBuffer(address))
        val port = Utils.byteArrayToShort(buffer.getBytes(buffer.length()-2, buffer.length()))
        val status = Socks5CommandStatus.valueOf(0)

        println("connecting $finalAddress:$port")
        return DefaultSocks5CommandResponse(status, socks5AddressType, finalAddress, port)
    } catch (e: Exception) {
        throw RuntimeException("解析Socks5请求失败")
    }
}

fun encodeCommandResponseToByteBuf(msg: Socks5CommandResponse): Buffer {
    val out = Unpooled.directBuffer()
    val addressEncoder: Socks5AddressEncoder = Socks5AddressEncoder.DEFAULT

    out.writeByte(msg.version().byteValue().toInt())
    out.writeByte(0x00)
    out.writeByte(0x00)

    val bndAddrType = msg.bndAddrType()
    out.writeByte(bndAddrType.byteValue().toInt())
    addressEncoder.encodeAddress(bndAddrType, msg.bndAddr(), out)
    out.writeShort(msg.bndPort())
    return Buffer.buffer(out)
}

fun main(args: Array<String>) {
    if (args.size > 0) {
        val f = File(args[0])
        if (f.exists()) {
            try {
                val config = f.readText()
                var option = optionFromConfig(config = config)
                val vertx = Vertx.vertx()
                vertx.deployVerticle(Server(option))
            } catch (e: Exception) {
                println("配置文件不正确")
            }
        } else {
            println("文件不存在: ${args[0]}")
        }
    } else {
        println("请指定配置文件")
    }

}