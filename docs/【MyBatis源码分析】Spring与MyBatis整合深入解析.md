> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)

# 概述
MyBatis-Spring 的原理通过整合 Spring 的事务管理和 MyBatis 的数据操作功能，简化了数据库操作的流程。核心在于 SqlSessionFactoryBean 和 SqlSessionTemplate，前者负责初始化 SqlSessionFactory，后者提供线程安全的 SqlSession 代理，自动处理数据库操作；同时，Spring 的 @Transactional 注解与 DataSourceTransactionManager 实现了事务的自动管理，使得 MyBatis 的数据库操作和 Spring 事务管理无缝连接，避免了手动管理事务和 SQL 会话。通过 MapperScannerConfigurer 自动扫描 Mapper 接口，实现了与 Spring 容器的深度集成。

# SqlSessionFactoryBean详解
SqlSessionFactoryBean 是 MyBatis-Spring 整合中的核心组件之一，主要作用是创建和配置 SqlSessionFactory。它通过配置 DataSource、mybatis-config.xml、Mapper 文件等信息，将 MyBatis 环境初始化为一个 Spring 管理的 Bean，方便与 Spring 容器的其他组件（如事务管理、Mapper 扫描等）进行集成。通过使用 SqlSessionFactoryBean，Spring 能够自动管理 MyBatis 配置，简化开发流程。

### 配置
spring-config.xml

```java
 <bean id="sqlSessionFactory" class="cn.xxxx.mybatis.spring.SqlSessionFactoryBean">
        <property name="resource" value="mybatis-config-datasource.xml"/>
    </bean>
```
### 原理

```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {

    private String resource;
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SqlSessionFactory getObject() throws Exception {
        return sqlSessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
```
FactoryBean 是 Spring 提供的一个接口，它的作用是用来生产某个类型的 Bean。SqlSessionFactoryBean 实现了这个接口，从而允许 Spring 容器通过 FactoryBean 机制创建 SqlSessionFactory 实例。
getObject()：该方法返回 SqlSessionFactory 实例，Spring 容器会将该实例注入到依赖该 SqlSessionFactory的 Bean 中。
getObjectType()：返回 SqlSessionFactory 的类型，Spring 用来确认返回的对象类型。
isSingleton()：表明该 Bean 是否是单例的。由于 SqlSessionFactory 应当是整个应用共享的资源，isSingleton() 返回 true，表示每次请求返回同一个实例。

InitializingBean 是 Spring 提供的一个接口，其 afterPropertiesSet() 方法会在 Spring 完成依赖注入后调用。SqlSessionFactoryBean 实现了这个接口，确保在 Spring 容器初始化完成后，它会创建 SqlSessionFactory。它通过 Resources.getResourceAsReader(resource) 方法加载 MyBatis 的配置文件（如 mybatis-config.xml），并使用 SqlSessionFactoryBuilder 来构建 SqlSessionFactory 实例。

这里，Resources.getResourceAsReader(resource) 会加载指定路径的 MyBatis 配置文件，SqlSessionFactoryBuilder 用来构建 SqlSessionFactory。

在 Spring 容器中，SqlSessionFactoryBean 只会初始化一次 SqlSessionFactory，并且该实例会被 Spring 容器作为单例（Singleton）Bean 管理。SqlSessionFactory 是线程安全的，可以在多个线程之间共享，因此可以在整个应用程序中复用。所有的 SqlSession 都是通过 SqlSessionFactory 来生成的，但 SqlSession 不是单例的，它是按需创建的。每次需要执行数据库操作时，MyBatis 会通过 SqlSessionFactory 创建一个新的 SqlSession 实例。

# MapperScannerConfigurer 源码分析
### 介绍
MapperScannerConfigurer 是 MyBatis-Spring 框架中的一个非常重要的组件，它帮助开发者自动扫描并注册 MyBatis 的 Mapper 接口。通过 MapperScannerConfigurer，我们不需要手动配置每一个 Mapper 接口的 Bean，而是通过配置扫描路径，自动为每个接口创建代理实例，并将其注册为 Spring 的 Bean。
【1】自动扫描指定包路径下的 MyBatis Mapper 接口。
【2】将扫描到的接口通过动态代理生成 Mapper 实例。
【3】将生成的 Mapper 实例注入到 Spring 容器中。

MapperScannerConfigurer 是一个实现了 BeanDefinitionRegistryPostProcessor、InitializingBean、ApplicationContextAware 和 BeanNameAware 接口的类。通过这些接口，MapperScannerConfigurer 可以在 Spring 容器启动时进行自定义的 Bean 注册和初始化操作。

### postProcessBeanDefinitionRegistry 方法
这是 BeanDefinitionRegistryPostProcessor 接口中的核心方法，它会在 Spring 容器启动过程中被调用，用于注册新的 Bean 定义。
下面代码非mybatis源码，参考原理代码

```java
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            // classpath*:com/**/dao/**/*.class
            String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = new SimpleMetadataReader(resource, ClassUtils.getDefaultClassLoader());

                ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));

                beanDefinition.setResource(resource);
                beanDefinition.setSource(resource);
                beanDefinition.setScope("singleton");
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(sqlSessionFactory);
                beanDefinition.setBeanClass(MapperFactoryBean.class);

                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
【1】扫描指定包（basePackage）下的所有 .class 文件。
【2】读取每个 .class 文件的元数据（MetadataReader）。MetadataReader 用于读取每个 Resource（即 .class 文件）的元数据，SimpleMetadataReader 是它的实现。它会从 Resource 中加载类的基本信息，如类名、接口、注解等。
【3】根据读取到的元数据创建一个 ScannedGenericBeanDefinition。ScannedGenericBeanDefinition 是一个特殊的 BeanDefinition 实现，它通过 MetadataReader 提供的元数据来构建一个新的 Bean 定义。此时，beanDefinition 包含了类的相关信息，如类名、构造方法、作用域等。
【4】配置该 BeanDefinition 的属性，包括 Bean 名称、构造器参数等。
【5】将 BeanDefinition 注册到 Spring 的 BeanDefinitionRegistry 中。通过 BeanDefinitionRegistry 注册 beanDefinition。BeanDefinitionHolder 用来包装 BeanDefinition 和 Bean 名称，registry.registerBeanDefinition(beanName, ...) 则是将其实际注册到 Spring 容器中，确保 Spring 能识别并管理这个 Bean。