# Mapper接口的注册过程
Mapper接口用于定义执行SQL语句相关的方法，方法名一般和Mapper XML配置文件中<select|update|delete|insert>标签的id属性相同，接口的完全限定名一般对应Mapper XML配置文件的命名空间。

```java
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    UserBean res = mapper.selectDataById(1);
```

```java
  @Select("select * from t_user where id=#{id}")
  @Result(column = "jsonInfo", property = "jsonInfo", typeHandler = JsonTypeHandler.class)
  UserBean selectDataById(int id);
```
在上面的代码中我们通过sqlSession.getMapper(UserMapper.class);就可以执行对应的SQL语句，但是这只是一个接口，我们并没有提供对应的实现类，那么这个是如何实现的呢？
实际上getMapper()方法返回的是一个动态代理对象。MyBatis中通过MapperProxy类实现动态代理。

MyBatis框架在应用启动时会解析所有的Mapper接口，然后调用MapperRegistry对象的addMapper()方法将Mapper接口信息和对应的MapperProxyFactory对象注册到MapperRegistry对象中。

# knownMappers使用 MapperProxyFactory 而不是直接存储代理类原因分析

```java
private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
```
knownMappers 是一个 Map<Class<?>, MapperProxyFactory<?>> 类型的成员变量，表示的是 Mapper 类型与其对应的 MapperProxyFactory 对象的映射。
在 MyBatis 的启动过程中，当 MyBatis 扫描到一个新的 Mapper 接口时，它会通过 MapperRegistry 的 addMapper() 方法将该接口及其对应的 MapperProxyFactory 对象注册到 knownMappers 中

为什么knownMappers使用 MapperProxyFactory 而不是直接存储代理类呢？
在 MyBatis 中使用 MapperProxyFactory 而不是直接存储代理类，背后的设计原因和原理主要与 灵活性 和 延迟加载（懒加载）有关。以下是为什么选择 MapperProxyFactory 而不是直接存储代理类的几个关键原因：

【1】 代理类的创建开销：如果在 addMapper 时就直接将代理类实例化并存入 knownMappers 中，这意味着每次添加 Mapper 时都会实例化一个代理对象，这在某些情况下是不必要的，尤其是在大规模的应用程序中，可能会增加性能负担。

【2】 懒加载机制：MapperProxyFactory 作为一个工厂类，它只会在调用 getMapper() 时，针对某个特定的 Mapper接口动态创建代理实例。这样，代理对象只会在需要时创建，避免了在启动时就为每个 Mapper 创建一个代理对象的开销。

【3】 设计原理：这种懒加载的设计方式使得 MyBatis 更加高效。只有在需要对某个 Mapper 进行操作时，才会创建相应的代理对象，避免了不必要的对象创建和内存占用。

【4】 代理类与接口分离：MapperProxyFactory 是一个工厂类，负责根据 Mapper 接口创建代理对象。通过工厂模式，MyBatis 可以灵活地控制何时创建代理类，并且保持代码的解耦性。如果直接存储代理实例，则每次获取时只能使用已创建的代理，无法实现这种灵活的控制。

【5】 工厂模式的好处：使用工厂模式可以让我们在运行时动态地创建不同类型的代理。例如，在不同的环境下，代理对象可能具有不同的行为，或者在某些情况下需要特殊的处理，MapperProxyFactory 可以灵活地根据这些需求创建代理对象。

【6】 控制代理对象的创建：如果直接将代理对象存储在 knownMappers 中，那么在获取 Mapper 时就无法控制代理的创建和配置。而通过工厂模式，MyBatis 可以在创建代理对象时加入一些额外的逻辑，比如事务处理、拦截器等。

【7】 避免多次创建相同的代理：MapperProxyFactory 是通过 newInstance 方法来创建代理对象的，每次调用 getMapper() 方法时，MapperProxyFactory 都会根据当前的 SqlSession 创建一个新的代理实例。而如果直接将代理对象存储在 knownMappers 中，那么每次访问都会返回相同的代理对象，这可能导致一些问题（如会话状态不一致）。通过每次调用 getMapper 创建新的代理对象，MyBatis 可以确保每个 SqlSession 使用的是独立的代理实例，避免不同的会话之间共享相同的代理对象，从而保持隔离性。

【8】 可扩展性。在 MyBatis 中，代理对象可能会涉及到一些额外的逻辑，比如事务管理、缓存管理、日志记录等。这些逻辑通常通过插件或者拦截器的方式进行处理，而 MapperProxyFactory 可以灵活地插入这些处理逻辑。如果直接将代理对象存入 knownMappers，则无法在代理对象创建时灵活地添加这些额外的功能。如果直接存储代理对象，意味着所有的代理类都会被硬编码在 MapperRegistry 中，这不利于框架的灵活扩展。而通过 MapperProxyFactory，MyBatis 可以根据不同的需求创建不同的代理，保持系统的灵活性和可扩展性。

【9】 工厂模式统一管理代理类的创建：MapperProxyFactory 作为一个工厂类，它为每个 Mapper 类型提供了统一的创建代理对象的接口。通过这种方式，MyBatis 可以更清晰地管理不同 Mapper 接口的代理对象，而不需要为每个接口单独处理创建逻辑。