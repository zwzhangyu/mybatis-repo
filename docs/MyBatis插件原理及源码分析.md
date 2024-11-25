> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](zwzhangyu.blog.csdn.net)

MyBatis框架允许用户通过自定义拦截器的方式改变SQL的执行行为，例如在SQL执行时追加SQL分页语法，从而达到简化分页查询的目的。用户自定义的拦截器也被称为MyBatis插件。
### 介绍
在MyBatis主配置文件中，可以通过<plugins>标签注册用户自定义的插件信息，例如：

```java
  <plugins>
    <plugin interceptor="com.zy.client.plugin.ExamplePlugin">
      <property name="myProp" value="1000"/>
    </plugin>
  </plugins>
```
MyBatis的插件实际上就是一个拦截器，Configuration类中维护了一个InterceptorChain的实例:

```java
protected final InterceptorChain interceptorChain = new InterceptorChain();

 public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
      ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    // 如果允许缓存，默认是开启的，对executor进行包装代理
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    // 拦截器插件
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
  }
```
interceptorChain属性是一个拦截器链，用于存放通过<plugins>标签注册的所有拦截器，Configration类中还定义了一个addInterceptor()方法，用于向拦截器链中添加拦截器.

### <plugins>标签解析
MyBatis框架在应用启动时会对<plugins>标签进行解析。MyBatis 的 XMLConfigBuilder 类是用来解析 MyBatis 配置文件的核心组件之一，其中 pluginElement() 方法的主要功能是解析 <plugins> 标签，并将其中定义的插件实例化后添加到 MyBatis 配置中，形成插件链。
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/0f30a4be58ff4ff782613c59d0cc18de.png)
org.apache.ibatis.builder.xml.XMLConfigBuilder#pluginElement

```java
  private void pluginElement(XNode parent) throws Exception {
    if (parent != null) {
      for (XNode child : parent.getChildren()) {
        String interceptor = child.getStringAttribute("interceptor");
        Properties properties = child.getChildrenAsProperties();
        Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
        interceptorInstance.setProperties(properties);
        configuration.addInterceptor(interceptorInstance);
      }
    }
  }
```
方法会首先通过 MyBatis 的 XPathParser 提取 <plugins> 节点的所有子节点。
【1】调用 parent.getChildren() 方法，提取 <plugins> 标签下的所有 <plugin> 子节点。
【2】对每个 <plugin> 节点，通过 child.getStringAttribute("interceptor") 提取插件的全限定类名，如 org.mybatis.example.MyInterceptor。
【3】调用 child.getChildrenAsProperties() 提取 <plugin> 节点下的 <property> 子节点，将其转化为一个 Properties 对象。
【4】通过resolveClass(interceptor).getDeclaredConstructor().newInstance();对配置的插件进行实例化对象。调用 resolveClass(interceptor) 将类名转化为 Class 对象，并通过反射调用其无参构造方法实例化插件对象。
【5】调用 setProperties(Properties) 方法，将解析的属性注入到插件实例中。Interceptor 接口中定义了 setProperties(Properties properties) 方法，插件需要实现该方法以接收参数。
【6】调用 configuration.addInterceptor(interceptorInstance) 将插件实例添加到 Configuration 的插件集合中，等待后续被拦截器链加载。
### 拦截器链的工作原理
MyBatis 的插件采用责任链模式，所有插件都会包装成拦截器链。在实际执行 SQL 操作时，MyBatis 会依次执行这些拦截器的 intercept 方法。
插件接口定义
所有插件都需要实现 Interceptor 接口，该接口定义如下：

```java
public interface Interceptor {
    Object intercept(Invocation invocation) throws Throwable;

    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
        // 可以由子类实现，接收插件的配置参数
    }
}

```
plugin() 方法通过 Plugin.wrap 将目标对象动态代理为被插件管理的对象，从而实现拦截。
### 插件的应用场景

通过拦截器链，开发者可以在以下场景中插入自定义逻辑：
1. SQL 日志记录
   拦截 Executor.query 或 Executor.update 方法，记录 SQL 执行时间和参数。
2. 性能监控
   在方法调用前后记录时间，用于性能分析。
3. 权限控制
   检查 SQL 是否符合权限要求。
4. 动态 SQL 修改
   在拦截器中对 SQL 进行动态修改或优化。

### MyBatis插件应用的四个组件
用户自定义的插件只能对MyBatis中的4种组件的方法进行拦截，这4种组件及方法如下：

```java
Executor（update, query, flushStatements, commit, rollback, getTransaction, close, isClosed）
ParameterHandler（getParameterObject, setParameters）
ResultSetHandler（handleResultSets, handleOutputParameters）
StatementHandler（prepare, parameterize, batch, update, query）
```
为什么MyBatis插件能够对Executor、ParameterHandler、ResultSetHandler、StatementHandler四种组件的实例进行拦截呢？
我们可以从MyBatis源码中获取答案。前面在介绍Configuration组件的作用时，我们了解到Configuration组件有3个作用，分别如下：
（1）用于描述MyBatis配置信息，项目启动时，MyBatis的所有配置信息都被转换为Configuration对象。
（2）作为中介者简化MyBatis各个组件之间的交互，解决了各个组件错综复杂的调用关系，属于中介者模式的应用。
（3）作为Executor、ParameterHandler、ResultSetHandler、StatementHandler组件的工厂创建这些组件的实例。
MyBatis使用工厂方法创建Executor、ParameterHandler、ResultSetHandler、StatementHandler组件的实例，其中一个原因是可以根据用户配置的参数创建不同实现类的实例；另一个比较重要的原因是可以在工厂方法中执行拦截逻辑。我们不妨看一下Configuration类中这些工厂方法的实现.
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/5bdf0e80ae4e40e696527a9c8e1d0470.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/1521d8036f0648ac89abfbdc4857c87d.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/a0481b2c673849e7b5febea2e11c6648.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/eece267e0020428092209dfdf3d5c02f.png)
在Configuration类的newParameterHandler()、newResultSetHandler()、newStatementHandler()、newExecutor()这些工厂方法中，都调用了InterceptorChain对象的pluginAll()方法，pluginAll()方法返回ParameterHandler、ResultSetHandler、StatementHandler或者Executor对象的代理对象，拦截逻辑都是在代理对象中完成的。这就解释了为什么MyBatis自定义插件只能对Executor、ParameterHandler、ResultSetHandler、StatementHandler这4种组件的方法进行拦截。

### InterceptorChain和Interceptor
在InterceptorChain类中通过一个List对象维护所有的拦截器实例，在InterceptorChain的pluginAll()方法中，会调用所有拦截器实例的plugin()方法，该方法返回一个目标对象的代理对象。
Interceptor接口中定义了3个方法，intercept()方法用于定义拦截逻辑，该方法会在目标方法调用时执行。plugin()方法用于创建Executor、ParameterHandler、ResultSetHandler或StatementHandler的代理对象，该方法的参数即为Executor、ParameterHandler、ResultSetHandler或StatementHandler组件的实例。setProperties()方法用于设置插件的属性值。需要注意的是，intercept()接收一个Invocation对象作为参数，Invocation对象中封装了目标对象的方法及参数信息。Invocation类的实现代码如下：

```java
public class Invocation {

  private final Object target;
  private final Method method;
  private final Object[] args;

  public Invocation(Object target, Method method, Object[] args) {
    this.target = target;
    this.method = method;
    this.args = args;
  }

  public Object getTarget() {
    return target;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArgs() {
    return args;
  }

  public Object proceed() throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, args);
  }

}
```
Invocation类中封装了目标对象、目标方法及参数信息，我们可以通过Invocation对象获取目标对象（Executor、ParameterHandler、ResultSetHandler或StatementHandler）的所有信息。另外，Invocation类中提供了一个proceed()方法，该方法用于执行目标方法的逻辑。所以在自定义插件类中，拦截逻辑执行完毕后一般都需要调用proceed()方法执行目标方法的原有逻辑。

为了便于用户创建Executor、ParameterHandler、ResultSetHandler或StatementHandler实例的代理对象，MyBatis中提供了一个Plugin工具类
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/12217f5ef70e4503a87d6ac1442c52d0.png)
Plugin类实现了InvocationHandler接口，即采用JDK内置的动态代理方式创建代理对象。Plugin类中维护了Executor、ParameterHandler、ResultSetHandler或者StatementHandler类的实例，以及用户自定义的拦截器实例和拦截器中通过Intercepts注解指定的拦截方法。Plugin类的invoke()方法会在调用目标对象的方法时执行，在invoke()方法中首先判断该方法是否被Intercepts注解指定为被拦截的方法，如果是，则调用用户自定义拦截器的intercept()方法，并把目标方法信息封装成Invocation对象作为intercept()方法的参数。Plugin类中还提供了一个静态的wrap()方法，该方法用于简化动态代理对象的创建。

```java
  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }
```
wrap()方法的第一个参数为目标对象，即Executor、ParameterHandler、ResultSetHandler、StatementHandler类的实例；第二个参数为拦截器实例。在wrap()方法中首先调用getSignatureMap()方法获取Intercepts注解指定的要拦截的组件及方法，然后调用getAllInterfaces()方法获取当前Intercepts注解指定要拦截的组件的接口信息，接着调用Proxy类的静态方法newProxyInstance()创建一个动态代理对象。

Intercepts注解用于修饰拦截器类，告诉拦截器要对哪些组件的方法进行拦截。