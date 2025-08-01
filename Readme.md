# wb-rpc 自定义协议的可扩展 RPC 框架
## 1. 一个简易版RPC框架开发
> 1. [手写 RPC 框架项目教程 - 编程导航教程](https://www.codefather.cn/course/1768543954720022530/section/1768545847093518337?contentType=text&tabKey=list&type=#)
> 2. [手写 RPC 框架 - 个人笔记+梳理+总结+扩展点实现（作者：Jools_Wakoo）](https://www.codefather.cn/post/1886747157767315457#heading-126)
### 1.1 基本设计

![手写RPC框架流程图](docs/images/手写RPC框架图.drawio.png)

### 1.2 扩展设计

![扩展](docs/images/扩展简易版RPC框架设计图.png)

### 1.3 RPC框架与Feign的区别和联系

> 1. [Spring Cloud OpenFeign](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#spring-cloud-feign)
> 2. [为什么 Feign 要用 HTTP 而不是 RPC？](https://blog.csdn.net/jam_yin/article/details/142371004)

**主要区别：**
Feign是基于HTTP实现的框架，而RPC框架是可以基于TCP/HTTP实现的框架，
RPC框架可以使用TCP作为网络协议来提高传输通讯性能，这是Feign做不到的。
Feign 选择使用 HTTP 而不是 RPC 是有其合理性的。
HTTP 具有通用性、兼容性、跨语言跨平台性、可扩展性、灵活性、安全性以及丰富的生态系统和工具支持等优势，
适合在多语言环境、快速开发与迭代、对通用性和兼容性要求高的场景下使用。
而 RPC 则具有高效性、强类型接口和丰富的功能等优势，适合在高性能要求、强类型接口需求和复杂业务逻辑场景下使用。
在实际应用中，开发者应根据项目的具体需求和特点，选择合适的服务通信方式，以实现高效、可靠的微服务架构。

**关系：**
Feign和PRC框架都高度依赖**代理模式**，
所以是可以在不改变原来的接口和实现类的情况下，创建一个新的接口类来决定哪些接口可以被简单地直接调用。
这对于把单体架构重构为微服务架构来说意义重大，可以让这个过程不再繁琐。

**总结：**

| 特性             | Feign                                          | RPC 框架 (如 Dubbo, gRPC)                      |
| :--------------- | :--------------------------------------------- | :--------------------------------------------- |
| **本质**         | **声明式 HTTP 客户端库**                       | **远程过程调用框架**                           |
| **核心协议**     | HTTP (通常 RESTful)                            | 灵活 (TCP/HTTP/自定义二进制, gRPC-HTTP/2等)    |
| **序列化**       | 通常文本 (JSON/XML)                            | 通常高性能二进制 (Protobuf, Thrift, Hessian等) |
| **服务治理**     | 依赖外部组件 (Ribbon, Eureka, Hystrix等)       | **通常内置** (服务发现, LB, 熔断, 监控等)      |
| **接口定义**     | Java Interface + HTTP 注解                     | IDL (如 .proto) 或严格 Java Interface          |
| **透明调用实现** | **动态代理 (运行时生成)**                      | **代理/Stub (通常编译时或运行时生成)**         |
| **适用场景**     | 跨语言/简单 HTTP API 调用, 微服务 RESTful 集成 | 高性能内部服务调用, 强治理需求, 复杂通信模型   |



### 1.4  其他设计

除了上面几个经典设计外，如果想要做一个优秀的 RPC 框架，还要考虑很多问题。
比如：

- 服务提供者下线了怎么办？需要一个失效节点剔除机制。
- 服务消费者每次都从注册中心拉取信息，性能会不会很差？可以使用缓存来优化性能。
- 如何优化 RPC 框架的传输通讯性能？比如选择合适的网络框架、自定义协议头、节约传输体积等。
- 如何让整个框架更利于扩展？比如使用 Java 的 SPI 机制、配置化等等。
  

在这个项目中，都会把这些问题解决。但是实际上做个完美的RPC项目需要解决的问题还有更多，这里不做讨论。

## 2. 全局配置加载

### 2.1 基本设计

通过双检索单例模式支持`application.properties`文件中的属性懒加载，支持以下属性：

- name 名称
- version 版本号
- serverHost 服务器主机名
- serverPort 服务器端口号

通过在`application.properties`文件中导入属性：

```properties
rpc.name=wbrpc
rpc.version=3.0
rpc.serverPort=8082
```

### 2.2 扩展设计

## 3. 接口Mock

### 3.1 基本设计

目前能让所有的接口得到对应的Mock，只是这些采取的是简单默认值，比如`int`的默认值是0，所有对象的默认值为null。

### 3.2 扩展设计

## 4. 序列化器与SPI机制

### 4.1 基本设计

能够通过简单的配置直接选用序列化器：修改消费者和生产者示例项目中的配置文件（注意此时生产者使用的是`rpc-core`的配置文件)，指定**相同的**序列化器，比如 `hessian`：

```Properties
rpc.serializer=hessian
```

然后依次启动生产者和消费者，发现能够正常完成RPC请求和响应。

### 4.2 扩展设计

