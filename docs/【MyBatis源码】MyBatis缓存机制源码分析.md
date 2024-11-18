

# 基础
MyBatis的缓存分为一级缓存和二级缓存，==一级缓存默认是开启的，而且不能关闭==。至于一级缓存为什么不能关闭，MyBatis核心开发人员做出了解释：MyBatis的一些关键特性（例如通过<association>和<collection>建立级联映射、避免循环引用（circular references）​、加速重复嵌套查询等）都是基于MyBatis一级缓存实现的，而且MyBatis结果集映射相关代码重度依赖CacheKey，所以目前MyBatis不支持关闭一级缓存。
==MyBatis 默认提供的缓存机制主要包括一级缓存（作用域是 SqlSession）和二级缓存（作用域是 Mapper）==

MyBatis提供了一个配置参数localCacheScope，用于控制一级缓存的级别，该参数的取值为SESSION、STATEMENT，当指定localCacheScope参数值为SESSION时，缓存对整个SqlSession有效，只有执行DML语句（更新语句）时，缓存才会被清除。当localCacheScope值为STATEMENT时，缓存仅对当前执行的语句有效，当语句执行完毕后，缓存就会被清空。MyBatis的一级缓存，用户只能控制缓存的级别，并不能关闭。


MyBatis的一级缓存，用户只能控制缓存的级别，并不能关闭。

MyBatis二级缓存的使用配置：
（1）在MyBatis主配置文件中指定cacheEnabled属性值为true。

```java
<setting name="cacheEnabled" value="true"/>
```
（2）在MyBatis Mapper配置文件中，配置缓存策略、缓存刷新频率、缓存的容量等属性，例如：

```java
<cache eviction="FIFO" flushInterval="60000" size="512" readOnly="true" />
```
（3）在配置Mapper时，通过useCache属性指定Mapper执行时是否使用缓存。另外，还可以通过flushCache属性指定Mapper执行后是否刷新缓存，例如：

```java
  <select id="selectById"
          flushCache="true"
          useCache="true"
          resultType="com.zy.client.bean.UserBean"
          parameterType="com.zy.client.bean.UserBean">
    select *
    from t_user
    where id = #{id}
      and name = #{name}
  </select>
```
通过上面的配置，MyBatis的二级缓存就可以生效了。执行查询操作时，查询结果会缓存到二级缓存中，执行更新操作后，二级缓存会被清空。
# MyBatis缓存实现类

MyBatis的缓存基于JVM堆内存实现，即所有的缓存数据都存放在Java对象中。MyBatis通过Cache接口定义缓存对象的行为，Cache接口代码如下：

```java
public interface Cache {
   // 该方法用于获取缓存的Id，通常情况下缓存的Id为Mapper的命名空间名称
  String getId();
  // 该方法用于将一个Java对象添加到缓存中，该方法有两个参数，第一个参数为缓存的Key，即CacheKey的实例；第二个参数为需要缓存的对象。
  void putObject(Object key, Object value);
  // 该方法用于获取缓存Key对应的缓存对象。
  Object getObject(Object key);
  // 该方法用于将一个对象从缓存中移除。
  Object removeObject(Object key);
  // 该方法用于清空缓存。
  void clear();
  // 获取缓存数量
  int getSize();
  // 该方法返回一个ReadWriteLock对象
  default ReadWriteLock getReadWriteLock() {
    return null;
  }
}

```
Cache接口实现类列表
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/436b41b8fe6b4116ba8784c20adbb8c8.png)

BlockingCache：阻塞版本的缓存装饰器，能够保证同一时间只有一个线程到缓存中查找指定的Key对应的数据。
FifoCache：先入先出缓存装饰器，FifoCache内部有一个维护具有长度限制的Key键值链表（LinkedList实例）和一个被装饰的缓存对象，Key值链表主要是维护Key的FIFO顺序，而缓存存储和获取则交给被装饰的缓存对象来完成
LruCache：最近最少使用的缓存装饰器，当缓存容量满了之后，使用LRU算法淘汰最近最少使用的Key和Value。LruCache中通过重写LinkedHashMap类的removeEldestEntry()方法获取最近最少使用的Key值，将Key值保存在LruCache类的eldestKey属性中，然后在缓存中添加对象时，淘汰eldestKey对应的Value值。具体实现细节读者可参考LruCache类的源码。

# MyBatis一级缓存实现原理
MyBatis的一级缓存是SqlSession级别的缓存。一级缓存使用PerpetualCache实例实现，在BaseExecutor类中维护了两个PerpetualCache属性.
![在这里插入图片描述](img/1.png)

PerpetualCache类，该类的实现比较简单，通过一个HashMap实例存放缓存对象。需要注意的是，PerpetualCache类重写了Object类的equals()方法，当两个缓存对象的Id相同时，即认为缓存对象相同。另外，PerpetualCache类还重写了Object类的hashCode()方法，仅以缓存对象的Id作为因子生成hashCode。

其中，localCache属性用于缓存MyBatis查询结果，localOutputParameterCache属性用于缓存存储过程调用结果。MyBatis通过CacheKey对象来描述缓存的Key值。在进行查询操作时，首先创建CacheKey对象（CacheKey对象决定了缓存的Key与哪些因素有关系）​。如果两次查询操作CacheKey对象相同，就认为这两次查询执行的是相同的SQL语句。

```java
 public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
      // 从缓存获取结果
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
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/c8158d78186549a18fcf7857dff0d9ce.png)
在BaseExecutor类的query()方法中，首先根据缓存Key从localCache属性中查找是否有缓存对象，如果查找不到，则调用queryFromDatabase()方法从数据库中获取数据，然后将数据写入localCache对象中。如果localCache中缓存了本次查询的结果，则直接从缓存中获取。
需要注意的是，如果localCacheScope属性设置为STATEMENT，则每次查询操作完成后，都会调用clearLocalCache()方法清空缓存。
除此之外，MyBatis会在执行完任意更新语句后清空缓存，我们可以看一下BaseExecutor类的update()方法。

```java
  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    clearLocalCache();
    return doUpdate(ms, parameter);
  }
```

```java
  @Override
  public void clearLocalCache() {
    if (!closed) {
      localCache.clear();
      localOutputParameterCache.clear();
    }
  }
```
注意在分布式环境下，务必将MyBatis的localCacheScope属性设置为STATEMENT，避免其他应用节点执行SQL更新语句后，本节点缓存得不到刷新而导致的数据一致性问题。
# MyBatis二级缓存实现原理

MyBatis二级缓存在默认情况下是关闭的，因此需要通过设置cacheEnabled参数值为true来开启二级缓存。SqlSession将执行Mapper的逻辑委托给Executor组件完成，而Executor接口有几种不同的实现，分别为SimpleExecutor、BatchExecutor、ReuseExecutor。另外，还有一个比较特殊的CachingExecutor，CachingExecutor用到了装饰器模式，在其他几种Executor的基础上增加了二级缓存功能。

```java
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    // 获取mapper对象维护的二级缓存对象
    Cache cache = ms.getCache();
    if (cache != null) {
      // 判断是否需要刷新二级缓存
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, boundSql);
        // 从MapperStatement对象对应的二级缓存中获取对象
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          // 如果缓存对象不存在，则从数据库获取
          list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          // 将数据库查询结果写入到二级缓存中
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }
```
在CachingExecutor的query()方法中，首先调用createCacheKey()方法创建缓存Key对象，然后调用MappedStatement对象的getCache()方法获取MappedStatement对象中维护的二级缓存对象。然后尝试从二级缓存对象中获取结果，如果获取不到，则调用目标Executor对象的query()方法从数据库获取数据，再将数据添加到二级缓存中。当执行更新语句后，同一命名空间下的二级缓存将会被清空。

```java
 @Override
  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    // 如果需要刷新，则清除缓存
    flushCacheIfRequired(ms);
    return delegate.update(ms, parameterObject);
  }
```
