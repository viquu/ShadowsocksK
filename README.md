ShadowsocksK

---

最近玩vertx的副产物，kotlin编写

用法：

``mvn clean package`` 

``java -jar target/ShadowsocksK-1.0-SNAPSHOT.jar config.json``

当前状况：

+ 仅作为客户端
+ 仅支持rc4-md5加密
+ 仅支持无须认证的本地socks5服务
+ 日常使用性能良好
