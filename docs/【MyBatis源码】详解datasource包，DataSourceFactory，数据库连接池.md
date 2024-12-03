> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)

# java.sql包和javax.sql包
Java提供的与数据库操作相关的包主要有两个，它们是 java.sql包和 javax.sql包。
java.sql通常被称为 JDBC核心 API包，它为 Java提供了访问数据源中数据的基础功能。基于该包能实现将 SQL语句传递给数据库、从数据库中以表格的形式读写数据等功能。
javax.sql通常被称为 JDBC扩展 API包，它扩展了 JDBC核心 API包的功能，提供了对服务器端的支持，是 Java企业版的重要部分。

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/a63c1509176848f691fade958146f1db.png)
## java.sql 包核心接口
DriverManager：管理数据库驱动，建立与数据库的连接。
Connection：表示与数据库的连接。
Statement：用于执行静态 SQL 语句。
ResultSet：表示 SQL 查询的结果集。
ResultSetMetaData：提供结果集的元数据。
DatabaseMetaData：获取数据库的元信息（如支持的功能、表结构等）。
Blob 和 Clob：分别用于处理二进制大对象和字符大对象。
## javax.sql 包核心接口
DataSource：提供一种获取数据库连接的标准方式，比 DriverManager 更灵活，支持连接池。
ConnectionPoolDataSource：专为连接池提供的接口。
PooledConnection：表示从连接池中获取的连接。
XADataSource：支持分布式事务的高级数据源。
XAConnection：表示参与分布式事务的数据库连接。
RowSet：支持对结果集的更灵活操作，能在脱离连接的情况下处理数据。
RowSetListener：监听 RowSet 操作的变化。

# 数据源工厂接口DataSourceFactory
datasource 包采用了典型的工厂方法模式。DataSourceFactory 作为所有工厂的接口，javax.sql包中的 DataSource作为所有工厂产品的接口。

```java
/**
 * @author Clinton Begin
 */
public interface DataSourceFactory {

  // 通过传入的配置属性初始化数据源
  void setProperties(Properties props);

  // 返回一个具体的 DataSource 对象
  DataSource getDataSource();

}
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/b070484a40604b4a8bf0228efef9df3b.png)
org.apache.ibatis.builder.xml.XMLConfigBuilder#dataSourceElement

```java
  private DataSourceFactory dataSourceElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute("type");
      // 获取<dataSource>子节点的配置
      Properties props = context.getChildrenAsProperties();
      // 根据getStringAttribute("type");获取对应的实现DataSourceFactory
      DataSourceFactory factory = (DataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
      // 映射转换数据源配置 driver；url；username；password
      factory.setProperties(props);
      return factory;
    }
    throw new BuilderException("Environment declaration requires a DataSourceFactory.");
  }
```
## 自定义HikariCPDataSourceFactory
以下是一个自定义数据源实现的示例，使用了流行的 HikariCP 连接池作为数据源，并通过实现 MyBatis 的 DataSourceFactory 接口集成到 MyBatis 中。
HikariCPDataSourceFactory 实现类

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class HikariCPDataSourceFactory implements DataSourceFactory {
    private HikariDataSource dataSource;

    @Override
    public void setProperties(Properties properties) {
        HikariConfig config = new HikariConfig();

        // 从配置中加载数据源参数
        config.setJdbcUrl(properties.getProperty("url"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));

        // 可选的高级配置
        config.setDriverClassName(properties.getProperty("driver"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maxPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("minIdle", "5")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("idleTimeout", "60000")));

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }
}

```
在 mybatis-config.xml 中，配置自定义的数据源工厂：

```java
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="com.zy.datasource.HikariCPDataSourceFactory">
        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/test"/>
        <property name="username" value="root"/>
        <property name="password" value="password"/>
        <property name="maxPoolSize" value="20"/>
        <property name="minIdle" value="5"/>
        <property name="idleTimeout" value="30000"/>
      </dataSource>
    </environment>
  </environments>
</configuration>
```
使用 MyBatis 加载配置并执行 SQL

```java
  @Test
  public void  testHikariCPDataSourceFactory() throws IOException {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserBean userBean = new UserBean();
    userBean.setId(1);
    Object objects1 = sqlSession.selectOne("user.selectById", userBean);
    System.out.println(objects1);
  }

```
## 自定义数据源实现
创建一个简单的数据源类，通过管理 java.sql.Connection 对象提供基本的数据源功能。
自定义 SimpleDataSource

```java
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SimpleDataSource implements DataSource {
  private String url;
  private String username;
  private String password;

  public SimpleDataSource(Properties properties) {
    this.url = properties.getProperty("url");
    this.username = properties.getProperty("username");
    this.password = properties.getProperty("password");
    try {
      String driver = properties.getProperty("driver");
      if (driver != null) {
        Class.forName(driver); // 加载驱动类
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to load database driver", e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  // 以下方法为 `DataSource` 接口的默认实现，仅作占位符
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  @Override
  public java.io.PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter(java.io.PrintWriter out) throws SQLException {}

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {}

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public java.util.logging.Logger getParentLogger() {
    return java.util.logging.Logger.getLogger("SimpleDataSource");
  }
}
```
为了将自定义数据源集成到 MyBatis 中，实现 DataSourceFactory 接口：

```java
public class SimpleDataSourceFactory implements DataSourceFactory {
  private SimpleDataSource dataSource;

  @Override
  public void setProperties(Properties properties) {
    this.dataSource = new SimpleDataSource(properties);
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }
}
```
配置文件 mybatis-config.xml

```java
      <dataSource type="com.zy.client.datasource.SimpleDataSourceFactory">
        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/test"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
      </dataSource>
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/fb0e621b62a44c3096038a4a39368b7f.png)
# JNDI数据源工厂
JNDI（Java Naming and Directory Interface）是 Java命名和目录接口，它能够为Java应用程序提供命名和目录访问的接口，我们可以将其理解为一个命名规范。在使用该规范为资源命名并将资源放入环境（Context）中后，可以通过名称从环境中查找（lookup）对应的资源。
MyBatis 提供的 JndiDataSourceFactory 类封装了这一过程，它通过 JNDI 查找到对应的 DataSource 并将其注入 MyBatis 的环境配置中。
在 Java EE 容器（如 Tomcat、Jetty、WebLogic、WildFly）或其他支持 JNDI 的环境中，通常会由管理员预先配置好数据源，并绑定到一个 JNDI 名称上（例如 java:/comp/env/jdbc/MyDB）。
应用程序通过 JNDI 名称直接获取这个数据源，而无需自行创建和管理。

MyBatis 配置文件使用 JNDI 数据源
在 mybatis-config.xml 中：

```java
<configuration>
  <environments default="production">
    <environment id="production">
      <transactionManager type="JDBC"/>
      <dataSource type="JNDI">
        <property name="jndiName" value="java:/comp/env/jdbc/MyDB"/>
      </dataSource>
    </environment>
  </environments>
</configuration>
```
• type="JNDI"：MyBatis 会加载 JndiDataSourceFactory。
• jndiName：指向在容器中配置的 JNDI 数据源。

容器中配置 JNDI 数据源示例
以 Tomcat 为例，配置 JNDI 数据源：
1. 在 conf/context.xml 或项目的 META-INF/context.xml 中：

```java
<Context>
  <Resource name="jdbc/MyDB"
            auth="Container"
            type="javax.sql.DataSource"
            maxTotal="20"
            maxIdle="10"
            minIdle="5"
            driverClassName="com.mysql.cj.jdbc.Driver"
            url="jdbc:mysql://localhost:3306/test"
            username="root"
            password="password"/>
</Context>
```
在 web.xml 中声明（可选，推荐在 Spring 或其他框架中直接管理）：

```java
<resource-ref>
  <description>My Database</description>
  <res-ref-name>jdbc/MyDB</res-ref-name>
  <res-type>javax.sql.DataSource</res-type>
  <res-auth>Container</res-auth>
</resource-ref>
```
# PooledDataSourceFactory池化数据源
PooledDataSourceFactory 是 MyBatis 提供的内置数据源工厂之一，旨在支持数据库连接池功能。通过连接池，应用程序可以复用已有的数据库连接，从而显著提高性能并降低资源消耗。

在 MyBatis 的配置文件中使用字符串“POOLED”来代表池化数据源，在池化数据源的配置中，除了非池化数据源中的相关属性外，还增加了一些与连接池相关的属性。
## PoolState连接池属性对象
在 PooledDataSource 中，定义了一些用于管理和统计数据库连接池状态的字段。这些字段帮助 MyBatis 连接池监控其连接池的使用情况，包括空闲连接、活跃连接的数量、连接请求的统计数据、超时连接、等待的线程数等。

```java
  /**
   * 连接池的核心数据源，负责管理数据库连接的获取、使用和释放
   */
  protected PooledDataSource dataSource;

  /**
   * 空闲连接对象池
   */
  protected final List<PooledConnection> idleConnections = new ArrayList<>();
  /**
   * 活跃连接对象池
   */
  protected final List<PooledConnection> activeConnections = new ArrayList<>();
  /**
   * 记录连接池的连接请求总数。每当应用程序请求连接时，这个值就会增加。它是一个重要的统计数据，帮助监控系统的整体负载。
   */
  protected long requestCount = 0;
  /**
   * 记录所有连接请求的累计时间。通过该字段可以统计每次连接请求从发起到获得连接的总时间，帮助评估连接池的响应效率和数据库性能。
   */
  protected long accumulatedRequestTime = 0;
  /**
   * 记录所有活跃连接的累计使用时间（即被借出的时间）。该值越大，说明连接池中的连接被长时间占用，可能需要优化连接池配置或者数据库查询性能。
   */
  protected long accumulatedCheckoutTime = 0;
  /**
   * 记录那些被借用并超过最大允许借用时间（poolMaximumCheckoutTime）的连接数。
   * 当连接超时未归还时，这个值会增加。这个字段帮助监控数据库连接是否被合理使用，并防止连接泄漏。
   */
  protected long claimedOverdueConnectionCount = 0;
  /**
   * 记录所有超时连接的累计占用时间。这可以帮助了解超时连接对系统性能的影响。
   * 如果该值过大，意味着连接池中存在不合理使用的连接，可能需要优化连接超时设置。
   */
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
  /**
   * 记录等待连接的累计时间。它表示连接池中等待获取连接的线程总共等待了多少时间。
   * 这个字段对于评估连接池的响应能力和识别潜在的性能瓶颈非常重要。
   */
  protected long accumulatedWaitTime = 0;
  /**
   * 记录等待连接的次数。如果连接池已满，且没有空闲连接可用，新的连接请求会被挂起，直到有连接可用。
   * 该字段用于统计发生等待的连接请求次数。这个值过高表明连接池的容量可能不足，需要增加连接数或优化查询性能。
   */
  protected long hadToWaitCount = 0;
  /**
   * 记录发生错误的连接数（例如无法建立连接或连接池中的连接出现故障）。
   * 这个字段有助于监控连接池的健康状态，并可以帮助快速定位和修复连接相关的问题。
   */
  protected long badConnectionCount = 0;
```
## activeConnections活跃池
在数据库连接池中，activeConnections（活跃池）是用来存储当前正在被应用程序使用的数据库连接的容器。在 MyBatis 的 PooledDataSource 中，activeConnections 是一个关键的成员变量，它通过管理活跃连接来优化数据库连接的使用效率，并且帮助处理连接的获取、释放及生命周期管理。

管理活跃连接：当连接池中的连接被借用时，它会从空闲池（idleConnections）转移到活跃池（activeConnections）。这些连接是被应用程序使用并未归还的。
跟踪连接使用情况：通过 activeConnections 可以知道当前有多少连接正在被使用，以及每个连接的状态。
连接超时管理：通过 activeConnections，可以监控连接是否超时，并强制回收已超时的连接。
### (1) 连接从空闲池到活跃池
当应用程序请求一个连接时，首先会从空闲池（idleConnections）中取出一个连接，并将其放入活跃池中。这个连接就被认为是“活跃的”，直到它被归还。

```java
synchronized (state) {
    if (!state.idleConnections.isEmpty()) {
        // 从空闲池取出连接
        PooledConnection conn = state.idleConnections.remove(0);
        // 将连接加入活跃池
        state.activeConnections.add(conn);
        return conn;
    }
}
```
在上面的代码中，state.idleConnections 是空闲连接池，state.activeConnections 是活跃连接池。连接从空闲池取出后被放入活跃池。
### (2) 连接归还到空闲池
当应用程序完成数据库操作后，连接需要被归还到连接池中。在这个过程中，连接会从活跃池移除，并被放回空闲池。

```java
synchronized (state) {
    // 从活跃池移除连接
    state.activeConnections.remove(conn);
    // 将连接放回空闲池
    if (state.idleConnections.size() < poolMaximumIdleConnections) {
        state.idleConnections.add(conn);
    } else {
        conn.invalidate(); // 如果空闲池已满，销毁连接
    }
}

```
在这段代码中，conn 会被从 activeConnections 中移除，并且重新加入 idleConnections 中，或者在池已满的情况下销毁。
### (3) 连接超时处理
为了避免连接长时间被占用导致资源浪费，MyBatis 连接池会在活跃连接中检查是否有超时的连接。如果某个连接超过了最大借出时间（poolMaximumCheckoutTime），它会被回收。
下面的代码是在获取连接，并且判断连接池已满的情况下关闭超时的

```java
// 获取活跃池第一个连接
PooledConnection oldestActiveConnection = state.activeConnections.get(0);
// 借出时间，即连接使用的时间戳和当前时间时间对比
long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
if (longestCheckoutTime > poolMaximumCheckoutTime) {
    state.activeConnections.remove(oldestActiveConnection); // 移除超时连接
    state.idleConnections.add(oldestActiveConnection); // 放回空闲池
}
```
### (4) 监控活跃连接数量
可以通过监控 activeConnections 的大小来判断当前有多少连接正在被应用程序使用。

```java
System.out.println("当前活跃连接数: " + state.activeConnections.size());

```
### (5) 防止连接泄漏
连接池要防止连接泄漏，即连接被借出后没有及时归还。为此，activeConnections 可以配合其他机制，如超时检测和连接关闭回收，确保每个借出的连接最终都被归还。
在 MyBatis 中，PooledDataSource 会通过最大借用时间（poolMaximumCheckoutTime）来防止连接被长时间占用。当连接超时未归还时，系统会回收连接并标记其为失效。
### (6) 最大活跃连接数
MyBatis 连接池的配置项之一是 poolMaximumActiveConnections，它限制了同时存在的最大活跃连接数。如果池中已达到最大活跃连接数，则新的请求会被阻塞，直到有连接归还或超时。

## activeConnections空闲池
idleConnections负责存储空闲连接的列表。与 activeConnections 配合，它实现了对数据库连接的状态管理。用于存储一组 PooledConnection 对象，表示当前未被借出的数据库连接（即空闲连接）。
存储空闲连接：当数据库连接未被应用程序使用时，这些连接会存储在 idleConnections 列表中，以便快速复用。
连接复用：如果应用程序需要连接，PooledDataSource 优先从 idleConnections 中取出一个可用连接，而不是重新创建新的连接。这大大减少了连接创建和销毁的开销。
池管理：在连接池达到最大活跃连接数时，空闲连接可被借出以支持更多的数据库操作。若空闲连接超过配置的 poolMaximumIdleConnections，多余的连接会被销毁以释放资源。
### (1) 添加到空闲池
当应用程序归还数据库连接时，该连接会从 activeConnections 移至 idleConnections。

```java
// org.apache.ibatis.datasource.pooled.PooledDataSource#pushConnection
synchronized (state) {
    state.activeConnections.remove(conn); // 从活跃池移除
    if (state.idleConnections.size() < poolMaximumIdleConnections) {
        // 若空闲池未满，添加连接
        state.idleConnections.add(conn);
    } else {
        // 若空闲池已满，销毁连接
        conn.invalidate();
    }
}
```
### (2) 从空闲池中获取连接
当应用程序需要连接时，优先从 idleConnections 中取出连接。

```java
synchronized (state) {
    if (!state.idleConnections.isEmpty()) {
        // 从空闲池中移除一个连接
        PooledConnection conn = state.idleConnections.remove(0);
        state.activeConnections.add(conn); // 添加到活跃池
        return conn;
    }
}
```
