# Spring 高阶面试宝典（50题·源码级深度）

> 本宝典所有题目均源自 `spring_learning_highlevel` 项目的 27 个 Demo 模块，涵盖 Spring Core、AOP、MVC、Boot 四大领域的源码级知识点。每题答案均可从项目代码中找到验证。

---

## 一、Spring 核心容器（1-12题）

### 1. BeanFactory 和 ApplicationContext 的区别是什么？BeanFactory 是如何工作的？

**答：** BeanFactory 是 Spring 最底层的 IoC 容器接口，`DefaultListableBeanFactory` 是其核心实现。它维护一个 `beanDefinitionMap`（ConcurrentHashMap），存储所有 `BeanDefinition`。

核心工作流程：
1. 通过 `BeanDefinitionBuilder.genericBeanDefinition()` 构建 `AbstractBeanDefinition`，设置 scope、initMethod、factoryMethod 等元信息
2. 通过 `beanFactory.registerBeanDefinition("name", beanDefinition)` 注册到容器
3. 调用 `AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory)` 注册内置的 `BeanFactoryPostProcessor` 和 `BeanPostProcessor`
4. 执行 `BeanFactoryPostProcessor.postProcessBeanFactory()` 对 BeanDefinition 进行后处理
5. `getBean()` 时真正创建 bean 实例，完成依赖注入和初始化

ApplicationContext 是 BeanFactory 的子接口，额外提供了事件发布、国际化（MessageSource）、资源加载、环境抽象等能力。

**对应项目：** `demo02/TestBeanFactory.java`

---

### 2. BeanDefinition 是什么？Spring 有哪些方式加载 BeanDefinition？

**答：** `BeanDefinition` 是 Spring 中描述 bean 的元数据对象，包含：bean 的 class 类型、scope（singleton/prototype）、initMethod/destroyMethod、工厂方法、构造器参数、属性值、是否懒加载等。

Spring Boot 启动时通过三种 Reader 加载 BeanDefinition：

```java
// 1. 注解方式：读取 @Configuration 类的 @Bean 方法
AnnotatedBeanDefinitionReader reader1 = new AnnotatedBeanDefinitionReader(beanFactory);
reader1.register(Config.class);

// 2. XML 方式：读取传统 XML 配置文件
XmlBeanDefinitionReader reader2 = new XmlBeanDefinitionReader(beanFactory);
reader2.loadBeanDefinitions(new ClassPathResource("b03.xml"));

// 3. 包扫描方式：扫描指定包下的 @Component 等注解
ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);
scanner.scan("com.tuoheng.demo39.sub");
```

**对应项目：** `demo02/TestBeanFactory.java`, `demo39/Demo39_03.java`

---

### 3. Bean 的完整生命周期是怎样的？

**答：** Spring Bean 的生命周期分为以下阶段：

```
构造 → 依赖注入 → 初始化 → 销毁
```

详细顺序：
1. **实例化**：调用构造器创建对象
2. **依赖注入**：`@Autowired`、`@Value` 等注入依赖
3. **初始化**（三阶段，按优先级）：
   - `@PostConstruct` 注解的方法（最先执行）
   - `InitializingBean.afterPropertiesSet()` 接口方法（其次）
   - `@Bean(initMethod="xxx")` 指定的自定义方法（最后）
4. **使用**：bean 就绪
5. **销毁**（三阶段，按优先级）：
   - `@PreDestroy` 注解的方法（最先执行）
   - `DisposableBean.destroy()` 接口方法（其次）
   - `@Bean(destroyMethod="xxx")` 指定的自定义方法（最后）

**关键验证**：`destroySingletons()` 后，销毁的 singleton 不会从容器中删除——再次 `getBean()` 会重新创建。

**对应项目：** `demo03/LifeCycleBean.java`, `demo07/Bean1.java`, `demo07/Bean2.java`, `demo07/Demo2.java`

---

### 4. BeanFactoryPostProcessor 和 BeanPostProcessor 的区别？各自的应用场景？

**答：**

| | BeanFactoryPostProcessor | BeanPostProcessor |
|---|---|---|
| **操作对象** | BeanDefinition（元数据） | Bean 实例（对象） |
| **执行时机** | 所有 BeanDefinition 加载完成后，实例化之前 | 每个 bean 初始化前后 |
| **典型应用** | `ConfigurationClassPostProcessor` 解析 `@Configuration` 类；MyBatis 的 `MapperScannerConfigurer` | `AutowiredAnnotationBeanPostProcessor` 处理 `@Autowired`；AOP 代理创建 |

**项目中的模拟实现**：
- `demo05/AtBeanPostProcessor.java`：实现 `BeanDefinitionRegistryPostProcessor`，读取 class 文件中的 `@Bean` 方法元数据，为每个 `@Bean` 方法创建对应的 `BeanDefinition`，模拟了 `ConfigurationClassPostProcessor` 的核心逻辑
- `demo05/MapperPostProcessor.java`：扫描 `@Mapper` 接口，创建 `MapperFactoryBean` 的 BeanDefinition，模拟 MyBatis-Spring 的自动配置

**对应项目：** `demo02/TestBeanFactory.java`, `demo05/AtBeanPostProcessor.java`

---

### 5. @Configuration 配置类本身也是一个 Bean，它的生命周期有何特殊之处？

**答：** `@Configuration` 类本身会被注册为一个 Bean，但它有特殊处理：

1. 在 `@Configuration` 类中，`@Autowired ApplicationContext` 可以注入（即使在当前 context 尚未完全初始化时）
2. `@PostConstruct` 标注的方法会被执行
3. **但** `@Autowired` 和 `@PostConstruct` 标注的方法若定义在 `@Configuration` 类中**不会被自动调用**——因为 `@Configuration` 类是被 CGLIB 代理的，代理类重写了 `@Bean` 方法（保证单例），但不会继承方法上的注解
4. 实现 `InitializingBean` 和 `ApplicationContextAware` 接口的方法**可以被正确回调**——Spring 在创建配置类代理时保留了这些接口回调

**对应项目：** `demo06/MyConfig1.java`, `demo06/MyConfig2.java`

---

### 6. Spring 的 Bean 作用域有哪些？prototype 注入 singleton 有什么坑？如何解决？

**答：** Spring 支持的作用域：
- **singleton**（默认）：整个容器只有一个实例
- **prototype**：每次获取都创建新实例
- **request**：每个 HTTP 请求一个实例
- **session**：每个 HTTP 会话一个实例
- **application**：每个 ServletContext 一个实例

**prototype 注入 singleton 的坑**：singleton bean 在初始化时只会注入一次依赖，所以 prototype bean 实际上也变成了"单例"——后续调用 `getF1()` 每次返回的都是同一个对象。

**四种解决方案**（项目中全部演示）：

| 方案 | 代码 | 原理 |
|------|------|------|
| `@Lazy` | `@Lazy @Autowired private F1 f1` | 每次访问代理对象时重新获取 |
| Scoped Proxy | `@Scope(value="prototype", proxyMode=ScopedProxyMode.TARGET_CLASS)` | 注入的是 CGLIB 代理 |
| `ObjectFactory` | `@Autowired private ObjectFactory<F3> f3; f3.getObject()` | 通过工厂延迟获取 |
| `ApplicationContext` | `context.getBean(F4.class)` | 直接从容器获取 |

**对应项目：** `demo08/Demo08_02.java`, `demo08/sub/E.java`

---

### 7. Spring 事件机制是如何工作的？

**答：** Spring 事件机制基于观察者模式：

1. **定义事件**：继承 `ApplicationEvent`（如 `UserRegisteredEvent`）
2. **发布事件**：通过 `ApplicationEventPublisher.publishEvent(event)` 发布
3. **监听事件**：使用 `@EventListener` 注解标注监听方法

```java
// 发布者
@Component
public class ComponentA {
    @Autowired
    private ApplicationEventPublisher publisher;
    public void register() {
        publisher.publishEvent(new UserRegisteredEvent(this));
    }
}

// 监听者
@Component
public class ComponentB {
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // 处理逻辑
    }
}
```

底层原理：`ApplicationContext` 在 `refresh()` 时初始化事件广播器 `SimpleApplicationEventMulticaster`，发布事件时遍历所有匹配的 `ApplicationListener` 并调用。

**对应项目：** `demo01/ComponentA.java`, `demo01/ComponentB.java`, `demo01/UserRegisteredEvent.java`

---

### 8. Spring 国际化的底层原理是什么？

**答：** Spring 通过 `MessageSource` 接口实现国际化，核心实现是 `ResourceBundleMessageSource`：

```java
GenericApplicationContext context = new GenericApplicationContext();
ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
messageSource.setBasename("messages"); // 加载 messages.properties 系列文件
context.registerBean("messageSource", MessageSource.class, () -> messageSource);

// 多语言解析
messageSource.getMessage("hi", null, Locale.ENGLISH);  // "hello"
messageSource.getMessage("hi", null, Locale.CHINESE);  // "你好"
messageSource.getMessage("hi", null, Locale.JAPANESE); // "こんにちは"
```

底层：`ResourceBundleMessageSource` 内部使用 Java 标准 `ResourceBundle`，根据 locale 查找对应后缀的 properties 文件（`messages_zh.properties`、`messages_ja.properties` 等），最终实现键值匹配。

**对应项目：** `demo01/TestMessageSource.java`

---

### 9. @ConfigurationProperties 是如何实现属性绑定的？什么是宽松绑定？

**答：** `@ConfigurationProperties(prefix="java")` 可以将外部配置属性绑定到 Java 对象上。

**宽松绑定（Relaxed Binding）** 是指属性名支持多种命名风格映射到同一个 Java 属性名：

```properties
user.first-name=Zhang   # kebab-case（烤串式）
user.middle_name=San     # snake_case（下划线式）
user.lastName=Li         # camelCase（驼峰式）
```

`ConfigurationPropertySources.attach(env)` 将 Spring 的 PropertySource 适配为 `ConfigurationPropertySource`，内部对属性名做归一化处理（统一去掉中划线、下划线，转小写比较），使得以上三种写法都能映射到 Java 的 `firstName`、`middleName`、`lastName`。

**对应项目：** `demo04/Bean4.java`, `springframework/boot/Step4.java`

---

### 10. BeanFactory 中的 destroySingletons() 做了什么？销毁后的 singleton 还能再获取吗？

**答：** `destroySingletons()` 会遍历所有 singleton bean，调用其销毁方法（`@PreDestroy` → `DisposableBean.destroy()` → `destroy-method`），然后从 singleton 缓存中移除。

**关键发现**：销毁后，`getBean()` 会**重新创建**该 bean。因为 BeanDefinition 并没有被删除——被删除的只是缓存的 singleton 实例。当再次 `getBean()` 时，发现缓存中没有，就会走完整的创建流程（实例化 → 注入 → 初始化），最终 singleton 缓存中又有了该实例。

**对应项目：** `demo07/Demo2.java`

---

### 11. Spring 的 @Autowired 是如何工作的？底层用了什么处理器？

**答：** `@Autowired` 由 `AutowiredAnnotationBeanPostProcessor` 处理，该处理器在 bean 初始化阶段起作用：

1. 扫描 bean 类中所有带 `@Autowired` 的字段和方法
2. 通过 `beanFactory.resolveDependency()` 解析依赖
3. 对于字段注入，通过反射 `Field.set()` 设置值
4. 对于方法注入，通过反射 `Method.invoke()` 调用

`resolveDependency()` 的查找顺序：
- 按类型匹配（byType）
- 多个匹配时按 `@Primary` 或 `@Qualifier` 筛选
- 若类型是 `ObjectFactory`、`ObjectProvider`，则返回延迟加载代理

**项目中通过 `AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR` 设置了构造器自动注入模式**，防止 `SqlSessionFactoryBean` 等复杂依赖创建失败。

**对应项目：** `demo02/TestBeanFactory.java`, `demo05/AtBeanPostProcessor.java`

---

### 12. @ComponentScan 与 @Bean、XML 三种方式加载的 Bean 是如何统一的？

**答：** 无论是哪种方式，最终都统一为 `BeanDefinition` 存储到 `DefaultListableBeanFactory` 的 `beanDefinitionMap` 中。三种方式的区别在于**元数据读取器不同**：

| 方式 | 读取器 | 原始元数据 |
|------|--------|------------|
| `@ComponentScan` | `ClassPathBeanDefinitionScanner` | 类上的 `@Component` 等注解 |
| `@Bean` | `AnnotatedBeanDefinitionReader` | `@Configuration` 类中的方法 |
| XML | `XmlBeanDefinitionReader` | `<bean>` XML 标签 |

所有方式产出的 `BeanDefinition` 最终集合在同一个 Map 中，`getBean()` 时统一处理，不区分来源。

**对应项目：** `demo39/Demo39_03.java`

---

## 二、AOP 原理（13-19题）

### 13. JDK 动态代理和 CGLIB 代理的区别？Spring 如何选择？

**答：**

| | JDK 动态代理 | CGLIB 代理 |
|---|---|---|
| **机制** | 基于接口，`Proxy.newProxyInstance()` | 基于继承，`Enhancer.create()` |
| **要求** | 目标类必须实现接口 | 目标类不能是 final |
| **性能** | 反射调用，JVM 优化好 | FastClass 机制，方法索引直接调用 |

**Spring 的选择逻辑**（`ProxyFactory` 封装）：
```
proxyTargetClass = false + 有接口 → JDK 代理
proxyTargetClass = false + 无接口 → CGLIB 代理
proxyTargetClass = true → 始终 CGLIB 代理
```

**对应项目：** `demo11/JdkProxyDemo.java`, `demo11/CglibProxyDemo.java`, `demo15/Demo15_01.java`

---

### 14. CGLIB 的 FastClass 机制是什么？为什么比反射快？

**答：** CGLIB 的 `MethodProxy` 使用 FastClass 机制避免反射调用：

1. 每个代理类生成对应的 FastClass（如 `ProxyFastClass`、`TargetFastClass`）
2. 通过 `Signature`（方法名 + 参数类型描述符）获取方法索引号（int）
3. 调用时直接使用 `fastClass.invoke(index, target, args)`，通过 switch-case 分发到具体方法

```java
// demo13 模拟的调用方式
int index = targetFastClass.getIndex(new Signature("save", "(I)V"));
targetFastClass.invoke(index, target, new Object[]{100});
```

这种方式避免了 `Method.invoke()` 的 JNI 调用开销和访问检查，因此比纯反射快。

**对应项目：** `demo13/Demo3.java`, `demo13/TargetFastClass.java`, `demo13/ProxyFastClass.java`

---

### 15. 高级 @Aspect 切面是如何转换为低级 Advisor 链的？

**答：** 转换流程（项目中 `A18.java` 完整演示）：

**第一步：高级注解 → 低级 Advice**

| 注解 | 转换后的 Advice 类 |
|------|-------------------|
| `@Before` | `AspectJMethodBeforeAdvice` |
| `@AfterReturning` | `AspectJAfterReturningAdvice` |
| `@AfterThrowing` | `AspectJAfterThrowingAdvice` |
| `@Around` | `AspectJAroundAdvice` |

**第二步：Advice 统一适配为 MethodInterceptor（适配器模式）**

| 原始 Advice | 适配器 | 目标 MethodInterceptor |
|------------|--------|----------------------|
| `AspectJMethodBeforeAdvice` | `MethodBeforeAdviceAdapter` | `MethodBeforeAdviceInterceptor` |
| `AspectJAfterReturningAdvice` | `AfterReturningAdviceAdapter` | `AfterReturningAdviceInterceptor` |

**核心设计理念**：对外暴露 Before/After/Around 之分方便使用，对内统一为 `MethodInterceptor` 环绕通知方便组装调用链——这是适配器模式的经典应用。

**对应项目：** `springframework/aop/framework/A18.java`

---

### 16. MethodInterceptor 调用链（责任链）是如何递归执行的？

**答：** 调用链的核心是一个 `ReflectiveMethodInvocation` 对象，它使用**计数 + 递归**的方式驱动链条：

```java
// A18_1.java 模拟实现
public Object proceed() throws Throwable {
    if (count > methodInterceptorList.size()) {
        return method.invoke(target, args);  // 调用目标方法
    }
    MethodInterceptor interceptor = methodInterceptorList.get(count++ - 1);
    return interceptor.invoke(this);  // 递归调用
}
```

执行顺序为：
```
ExposeInvocationInterceptor（将 MethodInvocation 放入当前线程）
  → before1 → before2 → target.foo() → after2 → after1
  → afterReturning/afterThrowing
  → around.after
```

这本质上是**责任链模式 + 递归**的组合，过滤器（Filter）、拦截器（Interceptor）都是用类似思路实现的。

**对应项目：** `springframework/aop/framework/A18_1.java`

---

### 17. 静态通知和动态通知的区别？对性能有什么影响？

**答：**

- **静态通知**：`@Before("execution(* foo(..))")` —— 切点表达式在代理创建时就确定，运行时不需要再次匹配切点
- **动态通知**：`@Before("execution(* foo(..)) && args(x)")` —— 需要参数绑定，运行时必须用切点对象对方法参数进行匹配和绑定

区别被封装在 `InterceptorAndDynamicMethodMatcher` 中：

```java
// A19 中的反射查看
Field methodMatcher = clazz.getDeclaredField("methodMatcher"); // 动态切点匹配器
Field methodInterceptor = clazz.getDeclaredField("interceptor"); // 环绕通知
```

**性能影响**：动态通知每次调用都需要执行切点匹配和参数绑定，**性能明显低于静态通知**。

**对应项目：** `springframework/aop/framework/autoproxy/A19.java`

---

### 18. AOP 代理是在什么时机创建的？

**答：** 代理创建时机取决于是否存在**循环依赖**：

- **无循环依赖**：在 bean 初始化之后（`postProcessAfterInitialization` 阶段）、`postProcessBeforeInitialization` → `init()` → `postProcessAfterInitialization`，代理在 after 阶段创建，此时 bean 已完全初始化
- **有循环依赖**：在 bean **实例化之后、依赖注入之前**就创建代理，并放入**三级缓存中的第二级缓存（earlySingletonObjects）**，保证注入给其他 bean 的是代理对象

**核心原则**：依赖注入和初始化阶段**绝不能**是代理对象——否则 `@PostConstruct`、`afterPropertiesSet()` 等方法调用会经过拦截器链。

**对应项目：** `springframework/aop/framework/A17_1.java`

---

### 19. Pointcut 表达式中 @annotation 和 execution 的区别？@annotation 能匹配父类或接口上的注解吗？

**答：**

| 表达式 | 匹配方式 | 使用场景 |
|--------|---------|---------|
| `execution(* foo())` | 匹配方法签名 | 精确方法拦截 |
| `@annotation(org.springframework.transaction.annotation.Transactional)` | 匹配方法上有指定注解 | 基于注解的拦截 |

**关键发现**：`@annotation` **只能匹配方法上直接声明的注解**，不能匹配父类或接口上注解的继承关系。项目中通过对比方法上的 `@Transactional`、类上的 `@Transactional`、接口上的 `@Transactional` 三种情况验证了这一点——只有方法上直接有的才能被 `@annotation` 匹配。

**对应项目：** `demo16/Demo16_01.java`

---

## 三、Spring MVC 核心（20-28题）

### 20. DispatcherServlet 的请求处理流程是怎样的？

**答：** 完整流程：

```
请求 → DispatcherServlet.doDispatch()
  → HandlerMapping.getHandler(request) → 找到 HandlerExecutionChain（包含 HandlerMethod + Interceptors）
  → HandlerAdapter.supports(handler) → 找到匹配的 HandlerAdapter
  → HandlerAdapter.handle(request, response, handler)
    → 参数解析（HandlerMethodArgumentResolver）
    → 调用 Controller 方法
    → 返回值处理（HandlerMethodReturnValueHandler）
  → 视图解析 / 消息转换
```

DispatcherServlet 默认会从 `DispatcherServlet.properties` 文件中加载内置的 HandlerMapping 和 HandlerAdapter 组件。

**对应项目：** `demo20/config/WebConfig01.java`

---

### 21. HandlerMapping 的作用是什么？Spring 中有哪些实现？

**答：** `HandlerMapping` 负责根据请求 URL 找到对应的处理器（Handler）。

| 实现 | 映射方式 | 时代 |
|------|---------|------|
| `RequestMappingHandlerMapping` | `@RequestMapping` 注解解析 URL | 现代 |
| `BeanNameUrlHandlerMapping` | Bean 名称作为 URL（如 `/c1` → `Controller1` bean） | 传统 |
| `SimpleUrlHandlerMapping` | 手动配置 URL → handler 映射 | 传统 |

项目中同时展示了现代和传统两种方式：
- 现代：`demo20` 中 `@RestController` + `@GetMapping`
- 传统：`demo33` 中 `@Component("/c1")` + `Controller` 接口 + `BeanNameUrlHandlerMapping`

**对应项目：** `demo20/Demo20_01.java`, `demo33/WebConfig01.java`

---

### 22. HandlerAdapter 的作用是什么？为什么需要它？

**答：** `HandlerAdapter` 是 **适配器模式**的体现——不同的 Controller 有不同的调用方式（方法签名、返回值类型各不相同），通过适配器统一调用接口：

```java
public interface HandlerAdapter {
    boolean supports(Object handler);  // 能否处理该 handler
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler);
    long getLastModified(HttpServletRequest request, Object handler);
}
```

| 实现 | 支持的 Handler |
|------|---------------|
| `RequestMappingHandlerAdapter` | `HandlerMethod`（`@RequestMapping` 方法） |
| `SimpleControllerHandlerAdapter` | 实现 `org.springframework.web.servlet.mvc.Controller` 接口的类 |

**对应项目：** `demo20/config/WebConfig02.java`, `demo33/WebConfig01.java`

---

### 23. 如何自定义 HandlerMethodArgumentResolver？什么场景需要它？

**答：** 实现 `HandlerMethodArgumentResolver` 接口：

```java
public class TokenArgumentResolver implements HandlerMethodArgumentResolver {
    // 判断是否支持该参数
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Token.class);
    }
    // 解析参数值
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return webRequest.getHeader("token");  // 从请求头获取 token
    }
}
```

注册方式：
```java
handlerAdapter.setCustomArgumentResolvers(List.of(new TokenArgumentResolver()));
```

**典型场景**：自定义 `@Token` 注解，从请求头提取当前用户身份信息注入到 Controller 方法参数中。

**对应项目：** `demo20/handler/TokenArgumentResolver.java`, `demo20/config/WebConfig03.java`

---

### 24. 如何自定义 HandlerMethodReturnValueHandler？

**答：** 实现 `HandlerMethodReturnValueHandler` 接口：

```java
public class YmlReturnValueHandler implements HandlerMethodReturnValueHandler {
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(Yml.class);
    }
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest) {
        // 将返回值序列化为 YAML 格式写入响应
        String yaml = new Yaml().dump(returnValue);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        response.setContentType("text/yaml");
        response.getWriter().print(yaml);
        mavContainer.setRequestHandled(true);
    }
}
```

**典型场景**：让 Controller 方法返回的对象自动转换为 YAML 格式输出。

**对应项目：** `demo20/handler/YmlReturnValueHandler.java`

---

### 25. @InitBinder 的作用是什么？全局和局部的优先级如何？

**答：** `@InitBinder` 用于自定义数据绑定器的转换逻辑，可以在绑定请求参数时添加自定义的 `PropertyEditor` 或 `Formatter`。

```java
@InitBinder
public void initBinder(WebDataBinder dataBinder) {
    dataBinder.addCustomFormatter(new MyDateFormatter("yyyy|MM|dd"));
}
```

**全局 vs 局部**：

| 作用范围 | 定义位置 | 存储位置（内部缓存） |
|---------|---------|-------------------|
| 全局 | `@ControllerAdvice` 中的 `@InitBinder` | `initBinderAdviceCache` |
| 局部 | 某个 Controller 中的 `@InitBinder` | `initBinderCache` |

**优先级**：局部的 `@InitBinder` 优先于全局的。

**项目中通过反射查看 `RequestMappingHandlerAdapter` 的内部缓存验证了这一点。**

**对应项目：** `demo23/a02/TestServletDataBinderFactory.java`, `demo24/Demo24_01.java`

---

### 26. @ModelAttribute 的作用和底层实现原理是什么？

**答：** `@ModelAttribute` 有两个用法：
1. **方法上**：在执行 Controller 方法之前，向 Model 中添加数据
2. **参数上**：从 Model 中取出对应的数据绑定到参数上

底层原理（`ModelFactory.initModel()`）：

```
1. 遍历所有 @ModelAttribute 方法（全局 @ControllerAdvice 的 + 局部 Controller 的）
2. 依次调用每个 @ModelAttribute 方法，将返回值存入 ModelAndViewContainer
3. 处理 @ModelAttribute 参数：从 ModelAndViewContainer 中查找对应的 model 属性进行绑定
```

项目完整演示了 `ModelFactory.initModel()` 的执行流程，包括参数解析器和数据绑定器的协作。

**对应项目：** `demo26/WebConfig.java`, `demo26/Demo26_01.java`

---

### 27. Spring MVC 的数据绑定和类型转换体系是怎样的？

**答：** 数据绑定体系分为三层：

| 层次 | 核心类/接口 | 作用 | 示例 |
|------|-----------|------|------|
| 基础封装 | `BeanWrapperImpl` | 反射操作 bean 属性 | `beanWrapper.setPropertyValue("a", "10")` |
| Web 数据绑定 | `WebDataBinder` / `ServletRequestDataBinder` | 将请求参数绑定到对象 | `binder.bind(new ServletRequestParameterPropertyValues(request))` |
| 高级工厂 | `ServletRequestDataBinderFactory` | 整合 @InitBinder + ConversionService | 创建带转换能力的 DataBinder |

**类型转换方式演进**：

| 方式 | 接口 | 特点 |
|------|------|------|
| PropertyEditor | `PropertyEditor` | Java 原生，只能 String → Object |
| Formatter | `Formatter<T>` | Spring 扩展，支持 String ↔ Object 双向 |
| ConversionService | `ConversionService` | Spring 3.0+，支持任意类型转换 |

**项目中演示了 5 种数据绑定方式**：无转换、仅 @InitBinder、仅 ConversionService、两者合并、ApplicationConversionService（Spring Boot 默认）。

**对应项目：** `demo23/a01/TestBeanWrapper.java`, `demo23/a02/TestServletDataBinderFactory.java`

---

### 28. Spring 如何处理 @DateTimeFormat 和嵌套属性绑定？

**答：**

`@DateTimeFormat(pattern = "yyyy|MM|dd")` 通过 `ApplicationConversionService` 自动注册对应的 Formatter，数据绑定时会自动调用格式化逻辑。

嵌套属性绑定：对于 `address.name` 这样的参数名，`BeanWrapperImpl` 会递归导航：
```
1. 识别 "address" 为嵌套属性
2. 调用 target.getAddress() 获取嵌套对象（如为 null 则自动创建）
3. 对嵌套对象调用 setPropertyValue("name", "西安")
```

**对应项目：** `demo23/a02/TestServletDataBinderFactory.java`

---

## 四、消息转换与响应处理（29-34题）

### 29. HttpMessageConverter 的工作机制是什么？Spring 如何选择使用哪个 Converter？

**答：** `HttpMessageConverter` 负责 HTTP 请求体和响应体的序列化/反序列化：

```java
public interface HttpMessageConverter<T> {
    boolean canRead(Class<?> clazz, MediaType mediaType);   // 能否反序列化
    boolean canWrite(Class<?> clazz, MediaType mediaType);  // 能否序列化
    T read(Class<? extends T> clazz, HttpInputMessage inputMessage);
    void write(T t, MediaType contentType, HttpOutputMessage outputMessage);
}
```

**选择逻辑**（`RequestResponseBodyMethodProcessor`）：
1. 遍历所有已注册的 `HttpMessageConverter`
2. 根据请求的 `Accept` 头和 `Content-Type` 头
3. 选择第一个 `canWrite()` / `canRead()` 返回 true 的 converter

项目中演示了同一方法返回 `@ResponseBody User`，根据请求头 `Accept: application/xml` 选择 XML converter，根据 `Accept: application/json` 选择 JSON converter。

**对应项目：** `demo28/Demo28_01.java`

---

### 30. @ResponseBody 和 @RestController 有什么关系？底层如何处理返回值？

**答：** `@RestController` = `@Controller` + `@ResponseBody`，是组合注解。

`@ResponseBody` 的处理流程：
1. `RequestMappingHandlerAdapter` 在调用 Controller 方法后
2. 发现方法或类上有 `@ResponseBody`
3. 使用 `RequestResponseBodyMethodProcessor` 处理返回值
4. 该 Processor 选择合适的 `HttpMessageConverter` 将返回值序列化为 JSON/XML 写入 response

**对应项目：** `demo28/Demo28_01.java`

---

### 31. ResponseBodyAdvice 是如何实现统一响应包装的？

**答：** `ResponseBodyAdvice` 是 Spring MVC 提供的响应体增强接口，可以在 `HttpMessageConverter` 写数据之前对 body 进行拦截处理：

```java
@ControllerAdvice
static class MyControllerAdvice implements ResponseBodyAdvice<Object> {
    // 判断是否需要处理
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理带 @ResponseBody 的方法
        return returnType.getMethodAnnotation(ResponseBody.class) != null
            || AnnotationUtils.findAnnotation(returnType.getContainingClass(), ResponseBody.class) != null;
    }
    // 在写之前对 body 进行包装
    public Object beforeBodyWrite(Object body, ...) {
        if (body instanceof Result) return body;
        return Result.ok(body); // 统一包装为 {code: 200, data: body}
    }
}
```

**执行时机**：在 `RequestResponseBodyMethodProcessor.handleReturnValue()` 中调用 `writeWithMessageConverters()` 之前执行。

**对应项目：** `demo29/WebConfig.java`

---

### 32. @ExceptionHandler 的工作原理是什么？嵌套异常如何匹配？

**答：** `@ExceptionHandler` 由 `ExceptionHandlerExceptionResolver` 处理：

1. 扫描 Controller 中所有 `@ExceptionHandler` 方法，建立 `异常类型 → 处理方法` 的映射
2. 发生异常时，遍历映射查找匹配的处理方法
3. 通过异常链（`e.getCause()`）递归匹配嵌套异常

```java
// Controller3 中，@ExceptionHandler(IOException.class) 可以匹配
// Exception("e1", RuntimeException("e2", IOException("e3")))
// 即通过异常链递归找到匹配的 IOException
```

`ExceptionHandlerExceptionResolver` **复用了** MVC 的参数解析器和返回值处理器，实现组件重用。

**对应项目：** `demo30/Demo30_01.java`

---

### 33. @ControllerAdvice 全局异常处理和 Controller 局部异常处理的优先级？

**答：** Spring 先查找 Controller 内部的 `@ExceptionHandler` 方法，找不到才去 `@ControllerAdvice` 类中查找：

```
1. 扫描当前 Controller 中的 @ExceptionHandler → 精确匹配
2. 未找到 → 扫描所有 @ControllerAdvice 中的 @ExceptionHandler → 全局兜底
3. 仍未找到 → 交给容器（Tomcat）处理
```

项目中演示了：即使定义了 `@ControllerAdvice` 的全局异常处理，如果目标是 `Controller5`（内部没有 `@ExceptionHandler` 方法），全局的才生效。

**对应项目：** `demo31/Demo31_01.java`

---

### 34. Tomcat 级别的错误处理（ErrorPage）与 Spring MVC 的异常处理有什么区别？

**答：**

| 处理层级 | 机制 | 配置方式 |
|---------|------|---------|
| Spring MVC | `@ExceptionHandler` / `@ControllerAdvice` | 注解驱动 |
| Tomcat 容器 | `ErrorPageRegistrar` 注册错误页面 | 代码配置 |

Tomcat 层面的处理：
```java
// 注册错误页面，将错误转发到 /error 路径
ErrorPageRegistrar registrar = factory -> {
    factory.addErrorPages(new ErrorPage("/error"));
};
```

转发后可以通过 `request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)` 获取原始异常。

Spring Boot 的 `BasicErrorController` 配合 `DefaultErrorAttributes` 提供了默认的错误处理 `/error` 端点。

**对应项目：** `demo32/WebConfig01.java`, `demo32/WebConfig02.java`

---

## 五、Spring Boot 启动与自动配置（35-45题）

### 35. SpringApplication.run() 启动的完整过程是怎样的？

**答：** Spring Boot 启动分为 12 个核心步骤（项目中 Demo39_03 完整模拟）：

```
1. 创建 SpringApplication 对象
2. 封装启动参数（DefaultApplicationArguments）
3. 推断应用类型（SERVLET / REACTIVE / NONE）
4. 加载 ApplicationContextInitializer（spring.factories）
5. 加载 ApplicationListener（spring.factories）
6. 推断主类（通过 StackTrace）
7. 创建 SpringApplicationRunListeners → 发布 starting() 事件
8. 创建 ApplicationContext（根据 WebApplicationType）
    - SERVLET → AnnotationConfigServletWebServerApplicationContext
    - REACTIVE → AnnotationConfigReactiveWebServerApplicationContext
    - NONE → AnnotationConfigApplicationContext
9. 准备容器（执行 Initializer）
10. 加载 BeanDefinition（注解 + XML + 包扫描）
11. refresh 容器（实例化所有 bean）
12. 执行 CommandLineRunner 和 ApplicationRunner
```

**对应项目：** `demo39/Demo39_01.java`, `demo39/Demo39_02.java`, `demo39/Demo39_03.java`

---

### 36. WebApplicationType 是如何推断的？

**答：** Spring Boot 通过 `WebApplicationType.deduceFromClasspath()` 反射推断：

```java
// 判断逻辑
if (存在 DispatcherServlet.class && 非响应式) → SERVLET
if (存在 DispatcherHandler.class && 非 Servlet) → REACTIVE
否则 → NONE（纯 Java 应用）
```

本质是通过 classpath 中是否存在特定类来判断应用类型。

**对应项目：** `demo39/Demo39_01.java`

---

### 37. ApplicationContextInitializer 的作用和注册方式？

**答：** `ApplicationContextInitializer` 在 `ApplicationContext.refresh()` 之前对 context 进行增强：

```java
// 方式1：通过 SpringApplication API 注册
spring.addInitializers(applicationContext -> {
    if (applicationContext instanceof GenericApplicationContext gac) {
        gac.registerBean("bean3", Bean3.class);
    }
});

// 方式2：通过 spring.factories 自动发现
// META-INF/spring.factories:
// org.springframework.context.ApplicationContextInitializer=\
// com.example.MyInitializer
```

**作用阶段**：在 `prepareContext()` 阶段（refresh 之前）执行，用来对 context 做预配置。

**对应项目：** `demo39/Demo39_01.java`, `demo39/Demo39_03.java`

---

### 38. SpringApplicationRunListener 的各个生命周期节点是什么？

**答：** `SpringApplicationRunListener` 定义了 7 个生命周期回调：

```
starting()           → SpringApplication 刚启动
environmentPrepared()→ Environment 就绪
contextPrepared()    → ApplicationContext 创建完毕，未加载 BeanDefinition
contextLoaded()      → BeanDefinition 加载完成，未 refresh
started()            → refresh 完成，ApplicationRunner 执行之前
running()            → ApplicationRunner 执行完毕，应用完全就绪
failed()             → 启动过程中抛出异常
```

这些 Listener 通过 `SpringFactoriesLoader` 从 `META-INF/spring.factories` 中加载。

**对应项目：** `demo39/Demo39_02.java`

---

### 39. Spring Boot 的自动配置原理是什么？@Import 和 ImportSelector 是如何配合的？

**答：** 自动配置的核心流程：

```
@SpringBootApplication
  → @EnableAutoConfiguration
    → @Import(AutoConfigurationImportSelector.class)
      → AutoConfigurationImportSelector.selectImports()
        → SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class)
          → 读取所有 jar 包的 META-INF/spring.factories
            → 返回自动配置类全限定名列表
              → 每个配置类上的 @Conditional 条件评估
                → 满足条件的配置类生效
```

**项目中手动模拟了完整流程**：
1. 自定义 `MyImportSelector` 实现 `ImportSelector`
2. 通过 `SpringFactoriesLoader.loadFactoryNames()` 读取 `spring.factories` 中的配置类
3. 使用 `@Import(MyImportSelector.class)` 导入

**对应项目：** `demo41/Demo41_01.java`

---

### 40. ImportSelector 和 DeferredImportSelector 的区别？为什么要区分？

**答：**

| | ImportSelector | DeferredImportSelector |
|---|---|---|
| **导入顺序** | 先导入 selector 引入的类，再处理 `@Import` 所在配置类 | **先**处理 `@Import` 所在配置类的 `@Bean`，**后**处理 selector 引入的类 |
| **效果** | 第三方配置类优先注册 | 本项目的 `@Bean` 优先注册 |
| **应用场景** | 普通组件导入 | 需要配合 `@ConditionalOnMissingBean` 实现"用户优先覆盖" |

**必要性**：Spring Boot 的自动配置使用 `DeferredImportSelector`（`AutoConfigurationImportSelector` 的实现），确保用户自定义的 Bean 优先注册，自动配置的 Bean 只有在用户没有定义时才生效——配合 `@ConditionalOnMissingBean` 实现"约定优于配置"。

**对应项目：** `demo41/Demo41_01.java`（34-39 行有详细注释说明）

---

### 41. @Conditional 条件装配是如何实现的？

**答：** `@Conditional` 通过实现 `Condition` 接口来判断配置类是否生效：

```java
// 条件：类路径存在 DruidDataSource 时才生效
static class MyCondition1 implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
    }
}

@Configuration
@Conditional(MyCondition1.class)
static class AutoConfiguration1 {
    @Bean
    public Bean1 bean1() { return new Bean1(); }
}
```

**`matches()` 方法的两个参数**：
- `ConditionContext`：可以获取 BeanDefinitionRegistry、Environment 等上下文信息
- `AnnotatedTypeMetadata`：可以获取 `@Conditional` 所在类/方法的注解元数据

**对应项目：** `demo42/Demo42_01.java`

---

### 42. 如何自定义一个类似 @ConditionalOnClass 的条件注解？

**答：** 分三步：

```java
// 第一步：定义注解
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(MyCondition.class)
public @interface ConditionalOnClass {
    boolean exists() default true;    // true=类存在时生效, false=不存在时生效
    String className();               // 要检查的类全限定名
}

// 第二步：实现 Condition，读取自定义注解的属性
static class MyCondition implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnClass.class.getName());
        boolean exists = (boolean) attrs.get("exists");
        String className = (String) attrs.get("className");
        boolean isPresent = ClassUtils.isPresent(className, null);
        return exists ? isPresent : !isPresent;
    }
}

// 第三步：使用
@Configuration
@ConditionalOnClass(className = "com.alibaba.druid.pool.DruidDataSource", exists = true)
static class DruidConfig { ... }
```

**对应项目：** `demo42/Demo42_02.java`

---

### 43. @ConditionalOnMissingBean 的实现原理是什么？为什么需要配合 DeferredImportSelector？

**答：** `@ConditionalOnMissingBean` 检查当前容器中**是否已经存在**同名或同类型的 Bean：

```java
@Bean
@ConditionalOnMissingBean  // 只有当容器中没有 name="bean1" 的 Bean 时才注册
public Bean1 bean1() {
    return new Bean1("第三方");
}
```

**必须配合 `DeferredImportSelector` 的原因**：

- 如果使用普通 `ImportSelector`，第三方配置类的 Bean 会**先于**本项目 `@Configuration` 类的 Bean 注册
- 此时 `@ConditionalOnMissingBean` 检查时，容器为空，条件满足，于是注册
- 然后本项目再注册同名 Bean 时就会因为 `setAllowBeanDefinitionOverriding(false)` 而报 `BeanDefinitionOverrideException`

使用 `DeferredImportSelector` 后，本项目 Bean 先注册，第三方配置类后处理，此时 `@ConditionalOnMissingBean` 发现已有同名 Bean，条件不满足，正确跳过。

**对应项目：** `demo41/Demo41_01.java`（17-40 行注释详细解释了覆盖冲突和解决过程）

---

### 44. Spring Boot 是如何推断主类的？

**答：** Spring Boot 通过分析**调用栈**来推断主类：

```java
// SpringApplication.deduceMainApplicationClass() 的核心逻辑
StackTraceElement[] stackTrace = new Throwable().getStackTrace();
for (StackTraceElement element : stackTrace) {
    if ("main".equals(element.getMethodName())) {
        return Class.forName(element.getClassName());
    }
}
```

它遍历当前线程的调用栈，找到包含 `main` 方法的那个类，即为应用的主类。

**对应项目：** `demo39/Demo39_01.java`

---

### 45. ApplicationRunner 和 CommandLineRunner 的区别？

**答：** 两者都在 Spring Boot 启动完成后执行，区别在于参数封装：

| | CommandLineRunner | ApplicationRunner |
|---|---|---|
| **参数类型** | `String... args`（原始字符串数组） | `ApplicationArguments`（封装后的对象） |
| **参数解析** | 无 | 支持 `getOptionNames()`、`getOptionValues("server.port")`、`getNonOptionArgs()` |
| **使用场景** | 简单参数场景 | 需要解析 `--key=value` 格式的复杂参数 |

**执行顺序**：先执行所有 `CommandLineRunner`，再执行所有 `ApplicationRunner`。

**对应项目：** `demo39/Demo39_03.java`

---

## 六、扩展与设计模式（46-50题）

### 46. Spring 中体现了哪些设计模式？各举一个具体例子。

**答：**

| 设计模式 | Spring 中的体现 | 对应项目 |
|---------|---------------|---------|
| **模板方法** | `BeanPostProcessor` 接口：定义 `postProcessBeforeInitialization` / `postProcessAfterInitialization` 模板，子类实现具体逻辑 | `demo03/TestMethodTemplate.java` |
| **适配器** | 将 @Before/@AfterReturning 等通知适配为统一的 `MethodInterceptor` | `A18.java` |
| **责任链** | `MethodInvocation.proceed()` 递归调用链条中的每个拦截器 | `A18_1.java` |
| **工厂方法** | `@Bean` 注解的方法 | `demo05/AtBeanPostProcessor.java` |
| **观察者** | `ApplicationEvent` + `@EventListener` | `demo01/ComponentA.java` |
| **代理** | AOP 代理（JDK 动态代理 / CGLIB） | `demo11`, `demo15` |
| **策略** | `HandlerMethodArgumentResolver` 不同实现处理不同类型的参数 | `demo20/handler/TokenArgumentResolver.java` |

---

### 47. Spring 中如何使用模板方法模式扩展 Bean 创建流程？

**答：** `BeanPostProcessor` 接口是典型的模板方法模式：

```java
// 框架定义模板接口
public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;  // 默认空实现
    }
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}

// 用户自定义扩展
public class MyBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 初始化前的自定义逻辑
        return bean;
    }
}
```

项目中 `demo03/TestMethodTemplate.java` 手动模拟了整个 BeanFactory + BeanPostProcessor 的模板方法调用流程，通过自定义 `MyBeanFactory` 和 `MyBeanPostProcessor` 接口完整再现了 Spring 的设计。

**对应项目：** `demo03/TestMethodTemplate.java`

---

### 48. Spring MVC 中参数解析器、返回值处理器如何实现策略模式？

**答：** `HandlerMethodArgumentResolver` 是典型的策略模式：

```java
// 策略接口
public interface HandlerMethodArgumentResolver {
    boolean supportsParameter(MethodParameter parameter);  // 判断是否由本策略处理
    Object resolveArgument(...);  // 实际解析
}

// 策略组合（Composite）
HandlerMethodArgumentResolverComposite composite;
for (HandlerMethodArgumentResolver resolver : resolvers) {
    if (resolver.supportsParameter(parameter)) {
        return resolver.resolveArgument(parameter, ...);
    }
}
```

内置的策略实现包括：
- `RequestParamMethodArgumentResolver` → `@RequestParam`
- `PathVariableMethodArgumentResolver` → `@PathVariable`
- `RequestResponseBodyMethodProcessor` → `@RequestBody`
- 自定义 `TokenArgumentResolver` → `@Token`

返回值处理器 `HandlerMethodReturnValueHandler` 同理。

**对应项目：** `demo20/config/WebConfig03.java`, `demo26/Demo26_01.java`

---

### 49. 依赖注入时如何处理泛型类型？GenericTypeResolver 和 ResolvableType 的区别？

**答：** Java 的泛型在运行时会被擦除，但 Spring 需要知道注入的具体泛型类型。例如：

```java
class BaseDao<T> { }
class StudentDao extends BaseDao<Student> { }
```

Spring 提供了两种方式解析泛型：

| 方式 | 基于 | 特点 |
|------|------|------|
| `GenericTypeResolver` | `ParameterizedType` API | 可静态调用 |
| `ResolvableType` | Spring 自研 API | 更强大，支持嵌套泛型、多级继承 |

**实际应用**：当存在多个同类型但不同泛型参数的 Bean 时（如 `BaseDao<Student>` 和 `BaseDao<Teacher>`），Spring 能够根据注入点的泛型声明精确匹配到正确的 Bean。

**对应项目：** `demo23/a03/`

---

### 50. Spring MVC 与 Tomcat 的集成方式是怎样的？

**答：** Spring Boot 与内嵌 Tomcat 的集成分为三个步骤：

```java
// 1. 创建 Tomcat 实例
Tomcat tomcat = new Tomcat();
tomcat.setPort(8080);
Context context = tomcat.addContext("", baseDir);

// 2. 通过 ServletContainerInitializer 注册 DispatcherServlet
// 等价于 web.xml 中的 servlet 注册
// Spring Boot 使用 DispatcherServletRegistrationBean 完成
DispatcherServletRegistrationBean registrationBean =
    new DispatcherServletRegistrationBean(dispatcherServlet, "/");

// 3. 启动 Tomcat
tomcat.start();
tomcat.getServer().await();
```

**内嵌 Tomcat 工厂**：`TomcatServletWebServerFactory` 负责创建和配置 Tomcat 实例，自动设置 Connector（默认使用 `Http11Nio2Protocol`）、端口、Context 路径等。

**对应项目：** `demo40/TestTomcat.java`, `demo20/config/WebConfig01.java`

---

## 项目概览

本宝典基于一个包含 **27 个 Demo 模块**的 Spring 高阶学习项目，覆盖的核心主题：

| 领域 | Demo 编号 | 主题 |
|------|-----------|------|
| 核心容器 | 01-08 | BeanFactory、生命周期、作用域、事件、国际化 |
| AOP | 11-16 | JDK/CGLIB 代理、FastClass、切面转换、静态/动态通知 |
| MVC 基础 | 20, 33 | DispatcherServlet、HandlerMapping、HandlerAdapter |
| MVC 进阶 | 23-32 | 参数解析、数据绑定、类型转换、消息转换、异常处理、响应包装 |
| Boot 启动 | 39-42 | 启动流程、自动配置、条件装配、Environment |

**作者**：douzhenjun | **技术栈**：Spring Boot 2.5.5 + Java 17 + Maven
