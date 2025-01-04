> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)


# SqlSession 是线程安全的吗？
SqlSession 本身并不是线程安全的。这意味着，不同线程不应当共享同一个 SqlSession 实例。如果在多线程环境下共享 SqlSession，可能会引发并发问题。
MyBatis 官方文档明确指出，SqlSession 是 非线程安全的，并且推荐每个线程都应该拥有独立的 SqlSession 实例。通常做法是为每个请求创建一个 SqlSession，并在操作完成后关闭它。
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/dc500cf0509d4ce68a2c13db0a912738.png)
# 为什么说是线程不安全的？
### 事务管理问题
SqlSession 中包含了对事务的管理，事务在数据库连接上下文中是绑定的。如果多个线程同时使用同一个 SqlSession，就有可能在同一个事务中执行不同的操作，造成不可预知的结果。例如：

```java
SqlSession sqlSession = sqlSessionFactory.openSession();
Thread thread1 = new Thread(() -> {
    sqlSession.update("update User set name = 'zhangsan' where id = 1");
    sqlSession.commit();  // 提交事务
});
Thread thread2 = new Thread(() -> {
    sqlSession.delete("delete from User where id = 2");
    sqlSession.commit();  // 提交事务
});

thread1.start();
thread2.start();
```
在上述例子中，thread1 和 thread2 会同时操作同一个 SqlSession 实例，执行不同的 SQL 操作。如果 SqlSession 是线程安全的，两个线程的事务提交应该不会互相干扰，但实际上，由于事务是由同一个数据库连接维护的，在并发环境下会出现事务不一致、提交顺序错误等问题。因此，SqlSession 必须是每个线程独立的。
## 数据库连接的共享问题
SqlSession 会持有数据库连接，这些连接是不可共享的。多个线程如果共享同一个 SqlSession，就可能在同一时刻使用同一个数据库连接，这会导致连接池中的连接竞争，进而引发连接池溢出、死锁等问题。
# 一级缓存线程安全问题
MyBatis 支持缓存机制，包括一级缓存和二级缓存。一级缓存是 SqlSession 局部的缓存，它的生命周期与 SqlSession 一致。二级缓存是跨 SqlSession 的缓存，与 SqlSessionFactory 绑定。虽然二级缓存是线程安全的，但一级缓存的设计并没有考虑到并发情况下的安全性。
假设有两个线程同时使用同一个 SqlSession 查询数据，并且 SqlSession 内部的一级缓存被修改：

```java
SqlSession sqlSession = sqlSessionFactory.openSession();
Thread thread1 = new Thread(() -> {
    User user1 = sqlSession.selectOne("select * from Users where id = 1");
    System.out.println(user1);
});
Thread thread2 = new Thread(() -> {
    User user2 = sqlSession.selectOne("select * from Users where id = 2");
    System.out.println(user2);
});

thread1.start();
thread2.start();
```
这里，两个线程可能在同一个 SqlSession 中同时操作数据，SqlSession 内部的一级缓存会被并发修改，导致缓存中的数据不一致。一个线程查询缓存的数据可能是另一个线程未提交的内容，从而引发数据错误。
# 一级缓存占位符EXECUTION_PLACEHOLDER线程安全问题分析
org.apache.ibatis.executor.BaseExecutor#queryFromDatabase

```java
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        try {
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            localCache.removeObject(key);
        }
        // 存入缓存
        localCache.putObject(key, list);
        return list;
    }
```

这段代码定义了一个泛型方法 queryFromDatabase，其主要功能是从数据库查询数据并利用缓存机制优化查询性能。首先，方法通过 localCache.putObject(key, EXECUTION_PLACEHOLDER) 将查询的 key 和一个占位符存入本地缓存，表示该查询正在执行。接着，它调用 doQuery 方法进行实际的数据库查询操作，并将查询结果存入 list。查询完成后，无论成功与否，都会在 finally 代码块中移除缓存中的占位符。然后，查询结果 list 被存入缓存，以便后续相同的查询可以直接从缓存中获取，避免重复查询。若该查询为存储过程（StatementType.CALLABLE），则输出参数被存入 localOutputParameterCache。最后，方法返回查询结果列表 list。

```java
public enum ExecutionPlaceholder {
  EXECUTION_PLACEHOLDER
}
```
【重点分析】为什么在查询数据库前将key插入缓存中，并且值是一个占位符呢？
ExecutionPlaceholder.EXECUTION_PLACEHOLDER 是一个查询标记，这个占位符可以避免在查询缓存时出现“脏读”，当多个线程同时查询同一个 key 的缓存，线程 A 还在数据库查询过程中，线程 B 也开始查询相同的 key，但此时线程 A 还没完成查询，缓存中的数据尚未更新，假设此时是同一个 SqlSession，因为cacheKey 是一模一样的，线程B会去一级缓存中取值，取出的数据就是旧的值。

MyBatis是如何解决这个问题的呢，它在执行数据库查询前，将改变缓存的值为一个“错误的标记值”，这个值是一个枚举类型，假设此时线程B过来，会经过下面的代码

```java
 List<E> list;
    try {
      queryStack++;
      // 从一级缓存获取结果
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        // 若缓存获取不到，从数据库获取
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
```
在执行 (List<E>) localCache.getObject(key) ，此时获取到是标记的值EXECUTION_PLACEHOLDER，那么就是出现类型转换异常，Mybatis是直接通过设置一个异常标记值，直接抛出异常的方式避免这种多线程同一个SqlSession问题。SqlSession不是线程安全，所以尽量不要多个线程混用一个SqlSession，应该是一个线程一个SqlSession，每个线程独立的connection。

# 如何避免线程安全问题？

每个线程使用独立的 SqlSession：在多线程环境下，每个线程应该创建一个独立的 SqlSession 实例，避免共享实例。

请求范围内管理 SqlSession：对于 Web 应用程序，通常在每个请求中创建并使用 SqlSession，请求结束后关闭 SqlSession。

使用 ThreadLocal：如果需要在多个方法或类中共享 SqlSession，可以使用 ThreadLocal 来确保每个线程都有自己的 SqlSession 实例。

```java
ThreadLocal<SqlSession> threadLocalSession = new ThreadLocal<SqlSession>() {
    @Override
    protected SqlSession initialValue() {
        return sqlSessionFactory.openSession();
    }
};

```
# Spring是如何解决这个问题的？
在 Spring 中，SqlSession 是通过 Spring 提供的事务管理和依赖注入机制来管理的。Spring 通过一系列的技术（如 @Transactional 注解、@Autowired 注解、TransactionManager 等）来避免 SqlSession 的线程安全问题，确保每个线程（通常是每个请求）都能使用一个独立的 SqlSession 实例。
Spring 会为每个请求创建独立的 SqlSession，并在请求结束时自动关闭，从而避免了线程共享 SqlSession 实例的问题。
Spring 内部使用 ThreadLocal 来为每个线程提供独立的 SqlSession。ThreadLocal 是一种线程局部存储机制，它可以确保每个线程都有自己的 SqlSession 实例。
具体来说，TransactionSynchronizationManager 类通过 ThreadLocal 维护了与当前事务相关的资源（如 SqlSession）。当一个请求（或一个线程）执行时，Spring 会将该请求的 SqlSession 实例绑定到当前线程的 ThreadLocal 中，这样其他线程就无法访问同一个 SqlSession 实例，从而避免了线程安全问题。

在 Spring 中，事务的生命周期通常由 PlatformTransactionManager 管理。当事务开始时，Spring 会在当前线程上通过 TransactionSynchronizationManager 来保存当前事务的信息。这个信息包括了当前事务管理器以及任何与事务相关的资源（如 SqlSession）。

```java
    private SqlSession getSqlSession() {
        // 根据当前线程的事务上下文来获取 SqlSession 实例
        SqlSession session = (SqlSession) TransactionSynchronizationManager.getResource(sqlSessionFactory);
        if (session == null) {
            session = sqlSessionFactory.openSession();
            // 将 SqlSession 绑定到当前线程
            TransactionSynchronizationManager.bindResource(sqlSessionFactory, session);
        }
        return session;
    }
```
当 Spring 开始一个新的事务时，SqlSession 会被绑定到当前线程的 ThreadLocal 上。这个绑定操作使得每个线程都有自己的独立 SqlSession。
当线程请求 SqlSession 时，Spring 会首先从当前线程的 ThreadLocal 中获取已经绑定的 SqlSession，如果没有绑定的 SqlSession，则会通过 SqlSessionFactory 创建新的 SqlSession。