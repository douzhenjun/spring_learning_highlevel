# spring_learning_highlevel

> Spring 源码级深度学习项目，27 个 Demo 手写代码拆解 Spring 内部机制。
> Spring Boot 2.5.5 · Java 17 · Maven

---

## 项目结构

```
com.tuoheng.demoXX       — 核心学习模块（27 个）
org.springframework.*     — 补充深度案例（AOP、Boot 启动）
src/main/resources/       — 配置文件、i18n、FreeMarker 模板、spring.factories
```

---

## Demo 速查表

### 一、核心容器

| Demo | 主题 | 核心知识点 |
|------|------|-----------|
| **01** | 事件 + 国际化 | `ApplicationEvent` 自定义事件发布/监听、`@EventListener`、`ResourceBundleMessageSource` 多语言（中/英/日） |
| **02** | BeanFactory 内部 | `DefaultListableBeanFactory` 手动注册 `BeanDefinition`、`BeanFactoryPostProcessor` 后处理、`@Configuration` + `@Bean` 协作 |
| **03** | Bean 生命周期 | 构造 → `@Autowired` 注入 → `@PostConstruct` 初始化 → `@PreDestroy` 销毁；`BeanPostProcessor` 模板方法模式手动模拟 |
| **04** | 属性绑定 | `@ConfigurationProperties(prefix="java")` 绑定系统属性 |
| **05** | 模拟 @Bean 处理 | `BeanDefinitionRegistryPostProcessor` 读取 class 文件元数据、为 `@Bean` 方法创建 `BeanDefinition`；模拟 MyBatis `MapperFactoryBean` 自动注册 |
| **06** | @Configuration 生命周期 | 配置类中 `@Autowired`/`@PostConstruct` 的行为限制、`InitializingBean` 和 `ApplicationContextAware` 回调验证 |
| **07** | 初始化/销毁顺序 | 三阶段优先级：`@PostConstruct` > `InitializingBean` > `initMethod`；`destroySingletons()` 后 bean 可重新创建 |
| **08** | Bean 作用域 | request/session/application 作用域；prototype 注入 singleton 的四种解决方案：`@Lazy`、ScopedProxy、`ObjectFactory`、`ApplicationContext.getBean()` |

### 二、AOP 代理

| Demo | 主题 | 核心知识点 |
|------|------|-----------|
| **11** | 代理基础 | JDK 动态代理（`Proxy.newProxyInstance` + `InvocationHandler`）vs CGLIB（`Enhancer` + `MethodInterceptor`） |
| **12** | 模拟 JDK 代理 | 手写 `$Proxy0` 类，展示 JDK 动态代理生成的字节码结构 |
| **13** | CGLIB FastClass | `MethodProxy.invokeSuper()` vs `MethodProxy.invoke(target)`、FastClass 索引调用避免反射、`Signature` 方法签名 |
| **15** | AOP 自动代理 | `AspectJExpressionPointcut` + `DefaultPointcutAdvisor` + `ProxyFactory`；JDK/CGLIB 选择三规则；`AnnotationAwareAspectJAutoProxyCreator` 发现切面 |
| **16** | Pointcut 匹配 | `execution()` vs `@annotation()` 区别；`@Transactional` 在方法/类/接口上的匹配行为验证 |

### 三、Spring MVC 核心

| Demo | 主题 | 核心知识点 |
|------|------|-----------|
| **20** | MVC 定制 | 嵌入 Tomcat + `DispatcherServlet` 手动装配；自定义 `HandlerMethodArgumentResolver`（`@Token`）和 `HandlerMethodReturnValueHandler`（`@Yml`） |
| **21** | 占位 | WebConfig 预留 |
| **23** | 数据绑定 | `BeanWrapperImpl` 反射赋值、`ServletRequestDataBinder` 请求参数绑定、`@DateTimeFormat` + `ConversionService` 五种方式对比；`GenericTypeResolver` / `ResolvableType` 泛型解析 |
| **24** | @InitBinder 追踪 | 全局（`@ControllerAdvice`）vs 局部（Controller 内）`@InitBinder`；反射查看 `initBinderAdviceCache` 和 `initBinderCache` |
| **26** | @ModelAttribute | 方法上向 Model 添加数据、参数上从 Model 获取数据；`ModelFactory.initModel()` 完整生命周期 |
| **27** | 视图解析 | FreeMarker 模板引擎集成、`ModelAndView` / String / `HttpEntity` / `@ResponseBody` 多种返回值处理；`LocaleResolver` |
| **28** | 消息转换 | `MappingJackson2HttpMessageConverter`（JSON）、`MappingJackson2XmlHttpMessageConverter`（XML）；`RequestResponseBodyMethodProcessor` 内容协商 |
| **29** | 统一响应包装 | `ResponseBodyAdvice<Object>` + `@ControllerAdvice` 将所有返回值统一包装为 `Result` 结构 |
| **30** | 异常处理 | `@ExceptionHandler` 四种场景：JSON 响应、ModelAndView、嵌套异常链匹配（`IOException e3` 通过三层 cause 找到）、`HttpServletRequest` 参数注入 |
| **31** | 全局异常 | `@ControllerAdvice` + `@ExceptionHandler` 兜底；局部优先、全局次之 |
| **32** | 容器级错误 | `ErrorPageRegistrar` 注册 Tomcat 错误页、`BasicErrorController` + `DefaultErrorAttributes` |
| **33** | 传统 MVC | `BeanNameUrlHandlerMapping`（bean 名即 URL）+ `SimpleControllerHandlerAdapter` + `Controller` 接口（Servlet 时代遗风） |

### 四、Spring Boot 深度

| Demo | 主题 | 核心知识点 |
|------|------|-----------|
| **39** | 启动全流程 | 12 步启动过程：`SpringApplication` 创建 → 类型推断 → Initializer → Listener → 主类推断 → Context 创建 → BeanDefinition 加载（三种 Reader）→ refresh → Runner 执行 |
| **40** | 内嵌 Tomcat | Apache Catalina `Tomcat` 类手动配置：`Context`、`Connector`（`Http11Nio2Protocol`）、`ServletContainerInitializer` |
| **41** | 自动配置 | `@Import` + `ImportSelector` / `DeferredImportSelector` + `SpringFactoriesLoader` + `spring.factories`；`@ConditionalOnMissingBean` 实现"用户优先覆盖"；Bean 覆盖异常演示 |
| **42** | 条件装配 | `Condition` 接口 + `@Conditional`；`ClassUtils.isPresent()`；自定义 `@ConditionalOnClass` 元注解 |

---

## org.springframework 补充案例

| 文件 | 主题 | 核心知识点 |
|------|------|-----------|
| `A18.java` | 切面转换全链路 | `@Aspect` → `Advisor` → 适配器统一为 `MethodInterceptor` → `ReflectiveMethodInvocation` 调用链执行 |
| `A18_1.java` | 调用链模拟 | 手写 `MethodInvocation`：计数 + 递归实现责任链逐个调用拦截器 → 目标方法 |
| `A17_1.java` | 代理创建时机 | 无循环依赖：初始化后创建；有循环依赖：实例化后即创建，存二级缓存 |
| `A19.java` | 静态/动态通知 | `InterceptorAndDynamicMethodMatcher` 封装；参数绑定通知需运行时切点匹配，性能更差 |
| `Step4.java` | 宽松绑定 | `ConfigurationPropertySources.attach()` + kebab/snake/camel 多风格属性名自动映射 |

---

## 关键配置文件

| 文件 | 用途 |
|------|------|
| `META-INF/spring.factories` | 自动配置类注册（demo41/42 使用） |
| `messages*.properties` | i18n 多语言资源（demo01） |
| `step4.properties` | 宽松绑定验证（kebab/snake/camel） |
| `*.ftl` | FreeMarker 模板（demo27） |
| `demo02.xml`, `b01.xml`, `b03.xml` | 传统 XML Bean 定义 |
| `logback.xml` | 彩色日志配置（SiftingAppender 按线程着色） |

---

## 运行环境

- JDK 17+（需添加 `--add-opens java.base/java.lang=ALL-UNNAMED` JVM 参数）
- Maven 3.6+
- 每个 Demo 含独立 `main()` 方法，可直接运行

## 作者

douzhenjun
