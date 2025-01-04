> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)


# 核心机制概述
MyBatis 是一款流行的持久层框架，其核心特点之一是可以直接通过定义 Mapper 接口与数据库交互，而无需显式地为接口编写实现类。这种特性极大地提高了开发效率。那么，MyBatis 是如何实现这一特性的？
MyBatis 通过动态代理机制，为 Mapper 接口生成代理对象，拦截接口方法的调用，并根据方法签名找到对应的 SQL 映射进行执行，最后将执行结果返回给调用者。这一过程主要依赖以下几个关键组件：
• SqlSession：提供与数据库交互的核心 API，用于执行 SQL、获取映射器等。
• MapperProxy：动态代理类，用于拦截 Mapper 接口方法调用。
• MapperMethod：封装了 Mapper 方法与 SQL 映射之间的关联关系。

# 源码分析
## 1. 获取 Mapper 实例
当调用 SqlSession.getMapper(Class<T> type) 方法时，MyBatis 会为指定的 Mapper 接口生成代理对象。

```java
<T> T getMapper(Class<T> type);
```

DefaultSqlSession 是 SqlSession 的默认实现类，其 getMapper 方法逻辑如下：

```java
   @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }
```
这里将 type 和当前的 SqlSession 传递给了 Configuration 对象。
## 2. 创建 Mapper 代理对象
Configuration 中的 getMapper 方法会通过 MapperRegistry 创建代理对象：

```java
    /**
     * 根据 Mapper 类型和 SqlSession 获取 Mapper 的代理实例
     *
     * @param type Mapper 接口类型
     * @param sqlSession SqlSession 实例
     * @param <T> Mapper 接口类型
     * @return 返回对应的 Mapper 代理实例
     * @throws RuntimeException 如果没有找到对应的 Mapper 或获取代理实例时出错
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }
```
MapperProxyFactory 是一个工厂类，专门用于生成 MapperProxy 代理对象。

```java
   /**
     * 创建 MapperProxy 的实例，生成指定 Mapper 接口的动态代理对象
     * @param sqlSession
     * @return
     */
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }
```
## 3. 拦截方法调用 MapperProxy
生成的代理对象会通过 MapperProxy 拦截 Mapper 接口的方法调用。
MapperProxy 类
MapperProxy 实现了 InvocationHandler 接口，其 invoke 方法会处理所有代理方法调用。

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
}

private MapperMethod cachedMapperMethod(Method method) {
    return methodCache.computeIfAbsent(method, k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
}
```
• 如果调用的是 Object 的方法（如 toString、equals），直接执行。
• 否则，将方法与参数封装为 MapperMethod，并执行对应的 SQL。

## 4. 关联 SQL 并执行
MapperMethod 负责将接口方法与 SQL 映射绑定，并执行 SQL。
MapperMethod 的执行逻辑

```java
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
        case INSERT: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = sqlSession.insert(command.getName(), param);
            break;
        }
        case UPDATE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = sqlSession.update(command.getName(), param);
            break;
        }
        case DELETE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = sqlSession.delete(command.getName(), param);
            break;
        }
        case SELECT: {
            if (method.returnsVoid()) {
                sqlSession.select(command.getName(), method.convertArgsToSqlCommandParam(args), null);
                result = null;
            } else if (method.returnsMany()) {
                result = sqlSession.selectList(command.getName(), method.convertArgsToSqlCommandParam(args));
            } else {
                result = sqlSession.selectOne(command.getName(), method.convertArgsToSqlCommandParam(args));
            }
            break;
        }
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    return result;
}
```
这里通过 SqlSession 的 insert、update、delete 和 select 方法与数据库交互。
# 为什么 MyBatis 采用了代理机制，而不是简单地面向流程化的方式?
在 MyBatis 中，使用代理的方式相比于直接在方法内部获取 namespace 对应的 XML 并解析 SQL，具有几个显著的优点。虽然通过手动解析 SQL 也能实现功能，但代理机制的使用带来了更多的灵活性、简洁性和可维护性。
### 1. 解耦和灵活性
使用代理的核心优点之一是 解耦。代理机制将 SQL 执行和业务逻辑分离，避免了在每个方法中手动查找和解析 SQL。这样可以确保：
• 接口和实现的分离：你定义的接口方法与数据库操作解耦，接口定义只是一个“约定”，不需要担心 SQL 语句如何查找、解析、执行等细节。
• 灵活性：动态代理允许在运行时确定需要执行的 SQL，而不需要在方法中硬编码 SQL 路径和执行细节。SQL 的解析和执行由 MyBatis 的代理类自动完成，开发者专注于业务逻辑，不必关心数据库操作的实现。
如果不使用代理，而是面向流程化，方法内部需要实现：
• 手动加载 SQL 映射文件（如 XML）。
• 从 XML 文件中根据方法名解析对应的 SQL 语句。
• 绑定 SQL 的参数并执行。
• 处理结果集并返回。
这样做会导致每个方法的逻辑变得复杂、重复，降低了代码的可读性和可维护性。
### 2. 方法拦截和事务管理
使用代理时，MyBatis 可以利用代理对象来拦截方法调用，这样就能：
• 统一处理日志、事务、缓存等跨切面功能。例如，事务管理和 SQL 执行的处理都可以统一通过代理类来完成，不需要在每个方法内部显式地调用事务控制逻辑。
• 统一的错误处理：代理机制可以在方法调用时自动捕获异常并进行统一的处理，如 SQL 异常的转换、事务的回滚等。
这种集中式的管理方式，比在方法内部手动管理事务和错误处理更加简洁、可靠和一致。

### 3. 动态代理支持方法级别的 SQL 定义
MyBatis 的代理机制通过注解或 XML 配置能够动态地将方法名和 SQL 语句绑定起来。代理对象能在方法调用时，动态查找映射文件中对应的 SQL 语句，并通过传入的参数执行 SQL。
• 例如，接口 UserDao 中的 findUserById(int id) 方法，会在代理类中被拦截，动态解析出对应的 SQL 语句并执行。这种方式的好处是，你不需要在每个方法内部去查找 SQL 映射，MyBatis 会自动根据方法名（或注解）定位对应的 SQL，从而使得代码更加简洁和规范。
如果不使用代理，方法内部就需要自己编写代码来：
• 查找与方法名对应的 SQL。
• 解析 SQL 文件。
• 绑定参数并执行查询。
这种手动的方式不仅增加了冗余代码，还会让每个方法的实现变得更复杂。

### 4. 增强的扩展性
代理机制还为 MyBatis 提供了很强的扩展性。例如：
• 插件机制：你可以通过自定义 MyBatis 插件来扩展或修改 SQL 执行过程，如日志打印、性能监控、缓存等功能。这些都可以通过代理类来实现，而不需要修改每个方法的实现。
• 跨切面功能：代理对象使得 MyBatis 能够轻松地为方法调用提供横向的功能（例如，缓存、事务、权限检查等），而这些功能在流程化的实现中会变得难以统一处理。

### 5. 提高代码的可维护性
由于代理机制将所有的 SQL 执行和参数绑定逻辑封装在 MapperProxy 中，开发者只需要关注业务逻辑的实现。修改 SQL、改变数据库连接等都可以在配置层完成，不需要修改业务层的代码。
如果采用面向流程的方式，每次修改 SQL 时都需要修改每个方法的实现，导致代码的维护变得复杂，且容易出错。


# 为什么MyBatis Mapper接口中的方法不支持重载？
在 MyBatis 中，每个方法与 SQL 语句之间的映射关系是通过 MethodSignature 来建立的。每个方法的签名（方法名和参数类型）都应该是唯一的，以便 MyBatis 能够根据方法签名来匹配 SQL 语句。

如果 Mapper 接口中的方法重载，MethodSignature 将不再唯一，导致 SQL 映射无法确定是哪一个具体的方法。例如，两个方法虽然参数不同，但在解析时会共享相同的方法名，从而无法区分。
