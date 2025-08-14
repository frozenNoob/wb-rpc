# wb-rpc 自定义协议的可扩展 RPC 框架
## 1. 一个简易版RPC框架开发
> 1. [手写 RPC 框架项目教程 - 编程导航教程](https://www.codefather.cn/course/1768543954720022530/section/1768545847093518337?contentType=text&tabKey=list&type=#)
> 2. [手写 RPC 框架 - 个人笔记+梳理+总结+扩展点实现（作者：Jools_Wakoo）](https://www.codefather.cn/post/1886747157767315457#heading-126)
### 1.1 基本设计
简易版RPC框架的流程图如下（按从绿到黑的箭头顺序查看）：
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



## 5. 注册中心基本实现

### 5.1 基本设计

已经可以先让服务提供者将服务地址等信息注册到注册中心，然后消费者通过服务发现从注册中心获取到这些信息。

通过实现这个过程，可以发现，实际上这种代理模式，本质上通过**同一个接口名（都来源于公共模块）**来传递实例对象：消费者将接口名放到请求体中，而**提供者在服务器启动前将<接口名: 实例>放到本地注册中心中**，然后在服务启动后通过请求处理器来获取这些实例。也就是说，推测服务提供者是必须用到公共模块的接口的，只是这一步代码我在用其他框架比如 Feign 时，都是自动处理好了，所以显得我并没有手动在一个java文件中导入并编写公共模块的接口。

当然，这并不违反代理模式的设计理念，解释如下：

在已有接口类以及对应的业务实现类中（比如service层的java类）需要更改：

- java实现类中import的POJO对象。

而在使用了本地注册中心（比如`LocalRegistry`）的用于专门启动某些配置的类中（比如`PrividerExample`)，需要更改：

- java实现类中import的POJO对象。
- java实现类中import的接口全类名。

以上都是**不需要变更已有的接口类和实现类中的内容**，只需要变更导入的方式，这就是代理模式的好处。

### 5.2 扩展设计



## 6. 注册中心优化

### 6.1 基本设计

成功实现 3 个注册中心的优化点：

1. 心跳检测和续期机制

2. 服务节点下线机制

3. 消费端服务缓存

   

4. 基于 ZooKeeper 的注册中心实现（出现一些错误，待解决）

### 6.2 扩展设计



## 7. 自定义协议
### 7.1 基本设计
采用TCP协议完成网络传输，并设计消息结构设计如下：
![消息结构设计图](docs/images/消息结构设计图.png)
通过**装饰者模式**来使用`RecordParser`对原有的 Buffer 处理器的能力进行增强，成功解决粘包和半包的问题。

### 7.2 扩展设计



## 8. 负载均衡

### 8.1 基本设计

在服务发现方法中，成功实现负载均衡算法中的：

1. 轮询（Round Robin)
2. 随机（Random）
3. 一致性哈希（Consistent Hashing）



使用轮询算法时，因为线程安全问题，所以使用的线程安全的原子类`AtomicInteger`。

### 8.2 扩展设计

#### 1）基于公平读写锁+双重检测锁 实现`setIfChanged`和`get`原子操作来保证采用一致性哈希算法的负载均衡类是线程安全的

##### 需求分析

为了确保 `ConsistentHashLoadBalancer` 的线程安全性，我们需要解决以下问题：

1. `TreeMap` 非线程安全，多线程并发修改会导致数据不一致或者由于树结构被破坏而直接报错。
2. 每次 `select()` 调用都会重建整个哈希环，效率低下且非必要。

##### 设计方案

1. **读写锁 (ReadWriteLock) 机制**：
   - 使用 `ReentrantReadWriteLock` 分离读/写操作。
   
   - 写锁：保护哈希环的重建过程（独占访问）。
   
   - 读锁：保护哈希环的查询操作（并发访问）。
   
2. **服务列表变更检测：**
   - 通过`hasServiceListChanged()`方法检测服务列表变化。该方法的最坏时间复杂度为O(n+m)，空间复杂度为O(n+m)。其中n和m分别为这两个列表的长度。
   
   - 比较服务地址哈希集合而非直接比较对象引用。
   
   - 避免不必要的哈希环重建。
   
3. **双重检查锁定：**

   - 获取写锁前先进行无锁检查。

   - 获取写锁后再次验证变更状态。
   - 避免不必要的锁竞争。

##### 开发实现

参考本次Fix提交中的`ConsistentHashLoadBalancer.java`文件。
通过定义并实现扩展接口`LoadBalancerForHash`的方式在不破坏其他实现类的情况下进行功能的扩展，这样才做符合单一职责原则。

##### 测试
测试时又仔细看了下逻辑，发现`ConsistentHashLoadBalancer`类是线程安全的，
但是实际上**使用该类的类并不能完全说是线程安全**（和使用线程安全类`ConcurrentSkipListMap`一样），可能会存在一种几乎不会发生的情况：
线程1获取读锁前被卡住（相当于`Thread.sleep()`）并直到线程2获取写锁写完并且比线程1早一步获取到读锁才继续，
此时线程1获得的哈希环是可能更改过了的。但这种情况一般不会发生，原因如下：

- 获取写锁前执行`hasServiceListChanged()`方法进行检测需要时间。
- 这是公平的读写锁，只要线程1先执行获取锁的语句，就能在队列中优先获取到读锁。
- 节点的变更一般不会很频繁，大多时候都是只有并发读取。对一个进程来说，一个节点的变更一般只要生成一次新的哈希环。
也就是说只要一个线程获取写锁并更新一次类的静态实例（即负载均衡这个实例）中的哈希环，所以其他线程就不用再获取写锁来更新了，然后所有线程又会回归并发读取这种常态。


**最重要的是**，哪怕是失败了比如访问了挂掉的节点，也会有重试机制和容错机制（后几章会实现），所以其实这里不用太严格。
但是也不能直接不管，可以warn一下：
```java
if (hasServiceListChanged(serviceMetaInfoList)) {
    log.warn("此时的服务列表已经发生变化");
    //throw new RuntimeException("此时的服务列表已经发生变化");
}
```
如果要**完全解决这个问题**的话，可以不采用单例模式加载类`ConsistentHashLoadBalancer`
或者`setIfChanged`方法返回一个完整的深拷贝的哈希环并将其作为形参传入`select`方法中,但是这样消耗的内存会很多，
所以综合考量下，使用现在这种方法是最好的。



## 9. 重试机制

### 9.1 基本设计

基于Gu⁠ava-Retrying 库实现重试时间算法（重试策略）如下：

1. 不重试。
2. 固定重试间隔（Fixed Retry In⁠terval）。



### 9.2 扩展设计



## 10. 容错机制

### 10.1 基本设计

实现的容错策略如下：

1. Fail-Safe 静默处理：系统出现部分非重⁠要功能的异常时，直接⁠忽略掉，不做任何处理，就像错误没有发生过一样。
2. Fail-Fast 快速失败：系统出现调用⁠错误时，立刻报错，⁠交给外层调用方处理。



结合之前的重试机制，达成先重试后容错的目的。

### 10.2 扩展设计



## 11. 启动机制和注解驱动

### 11.1 基本设计

#### 启动机制

将服务提供者和消费者的所有启动代码都分别封装为一个专门启动的类，然后由服务提供者/消费者调用即可。但是这种方法也还是**不够方便**，且服务提供者无法动态地获取需要注册的实现类的信息。

#### 注解驱动

通过另一种**更为方便**的设计，即通过spring boot stater注解驱动的方式。具体来说，是新增一个模块并在这个模块中编写3种注解：

1. `@EnableRpc`：用于全局标识项目需要⁠引入 RPC 框架⁠、执行启动类方法。原理是使用了`@Import`导入其他3种启动类，第一种启动类`RpcInitBootstrap`能够**在所有Bean实例化前**执行以初始化 RPC 框架，而其余两种启动类（`RpcProviderBootstrap`和`RpcConsumerBootstrap `）能够在IOC容器的**每个Bean都初始化后**，执行对应的两种启动类方法（都是实现`BeanPostProcessor`接口的`postProcessAfterInitialization`方法），即**扫描是否被特定注解修饰，是的话才执行后续的操作**。
2. `@RpcService`：服务提供者注解，在⁠需要注册和提供的服务类⁠上使用。原理是将Bean对象注册到IOC容器中，类似与Spring中的注解`@Service`。
3. `@RpcReference`：服务消费者注解，在需要注入服⁠务代理对象的属性上使用。原理是将IOC容器中的Bean对象注入到类成员中，类似 Spring 定义的 注解`@Autowired`或 Java 定义的注解`@Resource` 。

### 11.2 扩展设计

#### 1）模仿 Feign框架 的方式实现远程调用（实现注解@WbRpcClient，类似于@FeignClient）并遵循开闭原则，让服务提供者只需新建接口类就能够暴露功能给消费者，这在重构项目架构为微服务架构和限制接口访问权限上尤其有用。


##### 需求分析

如果按照传统的RPC框架方式（比如Dubbo RPC），在重构项目架构为微服务架构时，需要把已有的接口和POJO移到另一个公共接口模块。但是这样做的话，服务提供者一般需要暴露一些其他消费者无需使用而只有提供者才使用的接口。综上会导致3个问题：

1. 移动时需要在接口文件中更改import的接口的包名（如果包名不同）不太方便（虽然IDEA提供了自动改包名的这个功能）。
2. 破坏了接口访问权限。
3. 打可执行Jar包时会存在一些用不到的接口，进而导致Jar包变得更大而浪费存储空间。

而我需要解决这些问题。

##### 设计方案

###### 思路

Feign框架的使用方式主要为在使用注解`@FeignClient`时提供了相关的接口信息，如下图：

![img](https://fcneheqzlq8n.feishu.cn/space/api/box/stream/download/asynccode/?code=OWI3ZDJkNmFiMTc4MGFmMTZkYWI2ODRiZGNkMGM0MGRfWlJLVTc1ejJjRXdDcUhwV21RZUUxdGdLZVFXVnliWHRfVG9rZW46QW1DbmJrOUNxb3VwR2l4cnJPZWNEY2xVbm9mXzE3NTQ5OTc4MTI6MTc1NTAwMTQxMl9WNA)

可以模仿Feign框架的方式实现远程调用，从而让服务提供者无需移动原有的接口类就能够暴露功能给消费者。

在我这个项目中，服务提供者的本地注册中心所需要的键值对为：<接口名，接口实现类的Class对象>。现在的情况是value已经存在了，但是key确实直接取实现类对应的第一个接口，这是需要更改的地方。所以我增加了寻找key的过程，即从公共模块对应的接口上寻找到对应的key，而这个key将由我自定义的注解提供，所以最终需要决定的是写一个**能够扫描该注解并获取相关值的方法**。

###### **技术选型**

####### **对比**

扫描该注解并获取相关值则可以用下面的几种方法：

1. **使用Spring容器提供的类路径扫描工具**，该工具可以很方便地扫描指定包下带有某注解的类。
2. 使用第三方库（如Reflections）。
3. 手写自定义扫描器（复杂且不推荐）。预测需要逐目录检索后缀，然后用调用方法`Class.forName`获取Class对象后再调用方法`getAnnotation`获取注解信息。

**2种方法大致对比如下：**

| 特性                   | Reflections                                      | Spring ClassPathScanningCandidateComponentProvider |
| ---------------------- | ------------------------------------------------ | -------------------------------------------------- |
| 是否能扫描接口上的注解 | 是                                               | 否（只扫描具体类）                                 |
| 扫描范围               | 类、接口、枚举等所有类型                         | 仅可实例化的类（Bean 候选者）                      |
| 性能                   | 可能较慢（视包大小而定）                         | 较快（优化为 Spring Bean 扫描）                    |
| 配置复杂性             | 简单，灵活                                       | 依赖 Spring 上下文，配置稍复杂                     |
| 使用场景               | 通用的类路径扫描，适合非 Spring 环境或自定义逻辑 | Spring Bean 注册和组件扫描                         |

所以这里选取**能够直接扫描接口上的注解**的Reflection库。



####### **Reflection**库使用方式

> [Reflections库指南](https://www.baeldung-cn.com/reflections-library)

1）通过Maven导入Reflection库到模块`wb-rpc-spring-boot-starter`中：

```XML
<!-- 为了编写自定义的注解WbRpcClient -->
<dependency>
    <groupId>org.reflections</groupId>
    <artifactId>reflections</artifactId>
    <version>0.9.11</version>
</dependency>
```

2）在模块`example-springboot-consumer`通过编写测试类`com.wb.examplespringbootconsumer.ScanAnnotationTest`成功验证了该工具的可行性。

新增接口`ExampleService`：

```Java
package com.wb.examplespringbootconsumer;

import com.wb.wbrpc.springboot.starter.annotation.EnableRpc;

@EnableRpc
public interface ExampleService {
}
```

新增单元测试：

```Java
package com.wb.examplespringbootconsumer;

import com.wb.wbrpc.springboot.starter.annotation.EnableRpc;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Set;

public class ScanAnnotationTest {
    /**
     * 查找包下的被注解@EnableRpc修饰的接口/类
     */
    @Test
    void testScanAnnotation(){
        Reflections reflections = new Reflections("com.wb.examplespringbootconsumer");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(EnableRpc.class);
        for (Class<?> clazz : annotated) {
            EnableRpc ann = clazz.getAnnotation(EnableRpc.class);
            System.out.println(clazz.getName() + " : " + ann.getClass());
        }
    }
}
```

结果如下，符合预期：

![img](https://fcneheqzlq8n.feishu.cn/space/api/box/stream/download/asynccode/?code=OTBkYmU2NGM0M2E4ZDc4MmRlOGIwZDRlZGQ3YTcyNjJfbUh4NHhPVjMxNVFBSGlrOXlBRFJQR3ZjQ0MzVjZtc1VfVG9rZW46RWhya2J4VW1qb2ozMGR4WUc3VWNEZm01bnJiXzE3NTQ5OTc4MTI6MTc1NTAwMTQxMl9WNA)

##### 开发实现

1. 设置自定义注解`@WbRpcClient`:

```Java
package com.wb.wbrpc.springboot.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端注解（标注在类上），服务提供者使用此注解来决定需要暴露接口的类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WbRpcClient {
    /**
     * 给服务提供者的本地注册中心的key
     * @return
     */
    String key() default "";
}
```

1）修改`UserServiceImpl`类并新增接口`UserService`，让该类实现该接口，这符合正常的逻辑。

如图所示：

![img](https://fcneheqzlq8n.feishu.cn/space/api/box/stream/download/asynccode/?code=YWQ1OTIzZTJkNjdlNGE1MzllNjc1NDQ1ZWU4ZjM4ZGFfWWc5UXNMVTI1WmV2QTNUQXkzR3B5ZUlvUnMzU0k2QXhfVG9rZW46WlY1MWJ6SjhSb3dwMEx4TUJWUmNyV0Q5blhnXzE3NTQ5OTc4MTI6MTc1NTAwMTQxMl9WNA)

在原有接口`UserService`修改import的POJO的包名，代码如下：

```Java
package com.wb.examplespringbootprovider.service;

import com.wb.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    default int getNumber(){
        return 1;
    }
}
```

在实现类`UserServiceImpl`修改import的POJO的包名，代码如下：

```Java
package com.wb.examplespringbootprovider.service.impl;

import com.wb.example.common.model.User;
import com.wb.examplespringbootprovider.service.UserService;
import com.wb.wbrpc.springboot.starter.annotation.RpcService;

/**
 * 用户服务实现类
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("用户名；"+ user.getName());
        return user;
    }
}
```

2）将公共接口模块的包名改为与服务提供者模块包名加上一层client文件夹后相同，如图所示：

![img](https://fcneheqzlq8n.feishu.cn/space/api/box/stream/download/asynccode/?code=ZDE0ODMyNjVlNjk2MTZkMGJkNjUwZWRkZGE1OWVhMTdfUGNMR2NVUzhDZ2ZyQVRQZDlRS0hTbFVvaDY2ZngzWG5fVG9rZW46SGVpTWJtcjh5b3FsNnF4MXlJNGN6VTVkbjViXzE3NTQ5OTc4MTI6MTc1NTAwMTQxMl9WNA)

这里服务提供者模块包名为com.wb.examplespringbootprovider.service，所以需要加上一层client。

注意在这一步后，因为我变更了`UserService`接口的位置，所以之前的`example-consumer`和`example-common`就无法使用了！

3）修改`RpcProviderBootstrap`，主要是新增`getLocalRegistryKeyFromAnnotation`方法，完整代码如下：

```Java
package com.wb.wbrpc.springboot.starter.bootstrap;

import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RegistryConfig;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.registry.LocalRegistry;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.springboot.starter.annotation.RpcService;
import com.wb.wbrpc.springboot.starter.annotation.WbRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Set;

/**
 * Rpc 服务提供者启动
 */
@Slf4j
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * 从注解 @WbRpcClient中获取本地注册所需key
     */
    public String getLocalRegistryKeyFromAnnotation(String basePackage, String className) throws ClassNotFoundException {

        // 在basePackage路径下进行搜索
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(WbRpcClient.class);
        // 获取本地注册所需key
        String localRegistryKey =null;
        for (Class<?> clazz : annotated) {
            WbRpcClient annotation = clazz.getAnnotation(WbRpcClient.class);
            // 获取到指定实现类对应的接口名字
            if(annotation.key().equals(className)){
                localRegistryKey = clazz.getName();
                break;
            }
        }
        return localRegistryKey;
    }
    /**
     * 每个 Bean 初始化后执行，注册服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 对每个Bean对象都查询是否被该注解修饰，
        // 如果是则获取该注解进而获取具体的成员值（即为注解赋的值，否则采用默认值）。
        // 不是则返回null
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 需要注册服务
            // 1. 获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 1.1 默认值处理（默认值是Bean对应的实现类实现的第一个接口的class对象）
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            // 1.2 根据服务接口和服务实现类类名获取服务名
            String serviceName = null;
            try {
                // 包名需要后面都加上client
                String basePackage = interfaceClass.getPackageName() + ".client";
                // 所以是需要同包名(即同package)，然后才能根据实现类的类名去作匹配
                serviceName = getLocalRegistryKeyFromAnnotation(basePackage, beanClass.getSimpleName());
            } catch (ClassNotFoundException e) {
                log.error("未扫描到该包名下的接口或者实现类类名匹配失败！");
                throw new RuntimeException(e);
            }
            String serviceVersion = rpcService.serviceVersion();
            // 2. 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
```

4）修改模块`example-common`中的`UserService`接口，代码如下：

```Java
package com.wb.examplespringbootprovider.service;

import com.wb.example.common.model.User;
import com.wb.wbrpc.springboot.starter.annotation.WbRpcClient;

/**
 * 用户服务
 */
@WbRpcClient(key = "UserServiceImpl")
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    default int getNumber(){
        return 1;
    }
}
```

可以看到，这里使用了我的自定义注解`@WbRpcClient`，并提供了key值表示对应的实现类名称，一般来说，在规范的开发下，这个相当于接口类名+“Impl”。

##### 测试

先启动服务提供者，再启动服务消费者的单元测试：

![img](https://fcneheqzlq8n.feishu.cn/space/api/box/stream/download/asynccode/?code=MjRhYTQxYjM2MjJkNzhmZDc4ODFlNDUwMWY5YmRhMTRfVEZvdkJpR2x0VVFOeUw1M0x0ekdZdTB6ODlMN29DTjFfVG9rZW46WFpvdmI3YXA1bzNkaHh4b21qM2NGZFVsbjZmXzE3NTQ5OTc4MTI6MTc1NTAwMTQxMl9WNA)

可以看到已经成功了！

##### 总结

以后服务提供者暴露新的功能只需以下几步：

1. 移动已有的POJO到此公共接口模块。
2. 更改原有接口及其实现类上的import的POJO的包名。（**前2步**在提前做好管理，即把用到的POJO移到一个公共模块上时是**可以去掉的**）
3. 模仿原有接口在一个公共接口模块上新增新的公共接口，注意包名需要加上一层client。
4. 用注解`@RpcService`标注原有接口对应的实现类。
5. 用注解`@WbRpcClient`标注新增的公共接口，注解上的key为服务提供者实现类的类名。

