/**
 * Created by xingke on 2017/4/7.
 */
package com.xlvecle.socks5

import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

data class ServerOption(var localPort: Int = 1080, var password: String = "1234",
var remoteAddress: String, var remotePort: Int)

fun optionFromConfig(config: String): ServerOption {
    var jsonObject = JsonObject(config)
    var localPort = jsonObject.getInteger("local_port")
    var password = jsonObject.getString("password")
    var remoteAddress = jsonObject.getString("server")
    var remotePort = jsonObject.getInteger("server_port")
    var serverOption = ServerOption(localPort, password, remoteAddress, remotePort)
    return serverOption
}
