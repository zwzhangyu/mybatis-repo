> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)

# 事务概述
MyBatis的transaction包是负责进行事务管理的包，该包内包含两个子包：jdbc子包中包含基于 JDBC进行事务管理的类，managed子包中包含基于容器进行事务管理的类。
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/b745844f499c4b3a99c42a0a5c75e74c.png)
事务功能是由数据库提供的。以 MySQL 数据库为例，MySQL 主要有两种引擎：MyISAM和 InnoDB。

MySQL默认操作模式就是自动提交模式。在这种模式下，除非显式地开始一个事务，否则每个查询都被当作一个单独的事务自动提交执行。可以通过设置 AUTOCOMMIT的值来进行修改，例如设置“SET AUTOCOMMIT=0”将关闭自动提交模式，需要对每个数据库操作都进行显示的提交。不过，通常情况下，我们会使用自动提交模式。

实现 MySQL数据库事务操作的 SQL语句有下面三个。
BEGIN：开始事务；
ROLLBACK：回滚事务；
COMMIT：提交事务。

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/0353e010182849dd849edf9d7c662060.png)
# 事务接口及工厂
整个 transaction包采用了工厂方法模式实现
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/af4146a711f24f0f8bf032c43471956e.png)
## TransactionFactory接口

```java
public interface TransactionFactory {

  /**
   * 配置工厂的属性
   * @param props
   *          the new properties
   */
  default void setProperties(Properties props) {
    // NOP
  }

  /**
   * 从给定的连接中获取一个事务
   * @param conn Existing database connection
   * @return Transaction
   * @since 3.1.0
   */
  Transaction newTransaction(Connection conn);

  /**
   * 从给定的数据源中获取事务，并对事务进行一些配置
   * @param dataSource DataSource to take the connection from
   * @param level Desired isolation level
   * @param autoCommit Desired autocommit
   * @return Transaction
   * @since 3.1.0
   */
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
```
## Transaction接口

```java
public interface Transaction {

  /**
   * 获取该事务对应的数据库连接
   * @return DataBase connection
   * @throws SQLException
   *           the SQL exception
   */
  Connection getConnection() throws SQLException;

  /**
   * 提交事务
   * @throws SQLException
   *           the SQL exception
   */
  void commit() throws SQLException;

  /**
   * 回滚事务
   * @throws SQLException
   *           the SQL exception
   */
  void rollback() throws SQLException;

  /**
   * 关闭对应的数据库连接
   * @throws SQLException
   *           the SQL exception
   */
  void close() throws SQLException;

  /**
   * 读取设置的事务超时时间
   * @return the timeout
   * @throws SQLException
   *           the SQL exception
   */
  Integer getTimeout() throws SQLException;

}
```
TransactionFactory接口与 Transaction接口均有两套实现，分别在 jdbc子包和managed子包中。

# JDBC事务
jdbc子包中存放的是实现 JDBC事务的 JdbcTransaction类及其对应的工厂类。JdbcTransaction类是 JDBC事务的管理类。
## JdbcTransaction
JdbcTransaction 是 MyBatis 提供的默认事务实现，直接使用 JDBC 的原生 API 控制事务，适用于没有外部事务管理器（如 Spring）参与的场景。

```java
protected Connection connection; // JDBC 连接对象
protected DataSource dataSource; // 数据源
protected TransactionIsolationLevel level; // 事务隔离级别
protected boolean autoCommit; // 自动提交标识
```
### getConnection()

```java
@Override
public Connection getConnection() throws SQLException {
    if (connection == null) {
        openConnection();
    }
    return connection;
}
```
延迟获取 Connection，在真正需要时通过 openConnection() 方法初始化。

```java
protected void openConnection() throws SQLException {
    connection = dataSource.getConnection();
    if (level != null) {
        connection.setTransactionIsolation(level.getLevel());
    }
    if (connection.getAutoCommit() != autoCommit) {
        connection.setAutoCommit(autoCommit);
    }
}

```
commit()

```java
@Override
public void commit() throws SQLException {
    if (connection != null && !connection.getAutoCommit()) {
        connection.commit();
    }
}
```
对于非自动提交模式，执行事务提交。

rollback()

```java
@Override
public void rollback() throws SQLException {
    if (connection != null && !connection.getAutoCommit()) {
        connection.rollback();
    }
}
```
对于非自动提交模式，执行事务回滚。

close()

```java
@Override
public void close() throws SQLException {
    if (connection != null) {
        try {
            resetAutoCommit();
            connection.close();
        } finally {
            connection = null;
        }
    }
}
```
• 重置连接的自动提交模式为默认值。
• 关闭连接，并将连接对象置为 null。

JdbcTransaction 简单直接，适合轻量级环境。但对于大型项目，通常会使用外部事务管理器如 Spring 提供的事务管理方案。
## JdbcTransactionFactory
JdbcTransactionFactory 是 TransactionFactory 的实现类，用于创建 JdbcTransaction。

```java
@Override
public Transaction newTransaction(Connection conn) {
    return new JdbcTransaction(conn);
}
```
### 使用场景与配置
默认事务配置
在 MyBatis 的配置文件中，可以通过 transactionManager 配置事务管理器：

```java
<transactionManager type="JDBC">
    <!-- JDBC Transaction -->
</transactionManager>
```
这里的 type="JDBC" 对应的正是 JdbcTransactionFactory。

## 总结
JdbcTransaction：直接使用 JDBC 控制事务，简单高效，但缺乏分布式事务支持。
JdbcTransactionFactory：工厂类，负责生成 JdbcTransaction 实例。
适用场景：
适用于小型项目或不需要事务框架（如 Spring）支持的场景。
在测试场景下直接管理数据库连接。
如果你的项目中有外部事务管理需求（如 Spring 事务），建议使用 ManagedTransaction 和相应的 ManagedTransactionFactory，以实现更复杂的事务管理功能。
# 容器事务
managed子包中存放的是实现容器事务的 ManagedTransaction类及其对应的工厂类。在 ManagedTransaction类中，可以看到 commit、rollback等方法内都没有任何逻辑操作，这些都是提供给容器（Spring）去实现。

```java
  @Override
  public void commit() throws SQLException {
    // Does nothing
  }

  @Override
  public void rollback() throws SQLException {
    // Does nothing
  }
```
以 Spring容器为例。当 MyBatis和 Spring集成时，MyBatis中拿到的数据库连接对象是 Spring给出的。Spring可以通过 XML配置、注解等多种方式来管理事务（即决定事务何时开启、回滚、提交）​。当然，这种情况下，事务的最终实现也是通过Connection对象的相关方法进行的。整个过程中，MyBatis 不需要处理任何事务操作，全都委托给 Spring即可。

## Spring 中集成事务管理
在 Spring 中，通常通过 DataSourceTransactionManager 管理事务，MyBatis 配置 ManagedTransaction 可与之无缝配合。

```java
<transactionManager type="MANAGED">
    <!-- 使用 ManagedTransaction -->
</transactionManager>
```
## Spring 事务管理器工作流程
事务操作流程
【开启事务】Spring 根据 @Transactional 注解（或其他配置）拦截目标方法，调用事务管理器的 getTransaction() 方法。
DataSourceTransactionManager 从数据源中获取连接，将连接设置为非自动提交模式。
【执行业务逻辑】在目标方法执行期间，业务代码操作的数据库连接是 Spring 事务管理器控制的。
【事务提交】如果方法执行成功，Spring 调用 commit()，事务管理器提交连接上的事务。
【事务回滚】如果方法抛出异常，Spring 调用 rollback()，事务管理器回滚连接上的事务。
【关闭资源】Spring 在事务结束后关闭连接，或将连接归还到连接池。

## MyBatis 配合 DataSourceTransactionManager
MyBatis 的 ManagedTransaction 配合方式

使用外部事务管理器
MyBatis 的 ManagedTransaction 将事务的提交和回滚交给外部管理器（即 Spring 的 DataSourceTransactionManager）。
ManagedTransaction 本身不会主动调用 commit() 和 rollback()，也不会关闭连接。

Spring 管理的连接
Spring 通过 DataSource 提供数据库连接，ManagedTransaction 从 Spring 事务管理器获取连接。

在 Spring 中配置 MyBatis 数据源和事务管理器：

```java
@Configuration
public class MyBatisConfig {

    @Bean
    public DataSource dataSource() {
        // 配置数据源（例如 HikariCP 或 Druid）
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

```
在 MyBatis 配置中使用 ManagedTransactionFactory：

```java
<transactionManager type="MANAGED" />
```
Spring 事务管理生效后，DataSourceTransactionManager 会接管事务，MyBatis 的 ManagedTransaction 不干预事务行为。
## Spring 如何通过 AOP 管理事务
Spring 使用 AOP 拦截目标方法并动态代理事务逻辑。
【解析 @Transactional 注解】Spring 在扫描到 @Transactional 时，会为该类或方法创建代理对象。使用 TransactionInterceptor 处理事务逻辑。
【拦截方法调用】方法调用被代理对象拦截，代理对象在方法执行前后插入事务管理逻辑。
【事务控制】在方法执行前，开启事务（getTransaction()）。方法执行成功后，提交事务（commit()）。方法执行失败后，回滚事务（rollback()）。

TransactionInterceptor 的核心逻辑

```java
@Override
public Object invoke(MethodInvocation invocation) throws Throwable {
    TransactionInfo txInfo = createTransactionIfNecessary(txManager, txAttr, joinpointIdentification);
    Object retVal;
    try {
        retVal = invocation.proceed(); // 调用目标方法
    } catch (Throwable ex) {
        completeTransactionAfterThrowing(txInfo, ex);
        throw ex;
    }
    commitTransactionAfterReturning(txInfo);
    return retVal;
}

```
## 事务传播机制
pring 提供丰富的事务传播机制（通过 Propagation 配置），包括：
• REQUIRED：加入当前事务或创建新事务。
• REQUIRES_NEW：挂起当前事务，创建新事务。
• SUPPORTS：支持事务，如果当前没有事务则以非事务方式执行。
MyBatis 在使用 ManagedTransaction 时，事务传播由 Spring 的事务管理器完全控制。

## 总结
1. 集中事务管理：Spring 控制事务边界，简化 MyBatis 的事务逻辑。
2. 支持事务传播：通过 Spring 的事务传播机制，MyBatis 可以轻松实现嵌套事务等复杂场景。
3. 灵活扩展：通过 AOP，可以在事务管理中插入额外的逻辑，如日志、监控。
   实现细节总结
   • MyBatis 的 ManagedTransaction 本质上是一个适配器，将事务管理的控制权交给 Spring。
   • Spring 的 DataSourceTransactionManager 通过代理和 AOP 实现了事务的统一管理。
   这种设计使得 MyBatis 能够专注于 ORM 操作，而事务的复杂性交由更专业的框架（Spring）处理。

