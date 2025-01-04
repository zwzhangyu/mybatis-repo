> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)

# 难点分析
【1】 事务的一致性。
在插入数据并获取自增主键时，可能会涉及事务的一致性问题，尤其是在并发插入的情况下。MyBatis需要确保即使在高并发的环境中，获取到的主键是正确的，避免产生重复或错误的主键。
【2】 数据库支持差异。
不同数据库的自增主键实现方式不同，这使得MyBatis必须对不同数据库有不同的处理方式。例如：
• MySQL：可以通过useGeneratedKeys=true来获取自增主键。
• Oracle：需要使用RETURNING INTO语句或通过Sequence获取主键。
• PostgreSQL：使用RETURNING子句来返回主键。
MyBatis需要根据数据库的不同来选择正确的获取主键的方式，因此数据库方言（Dialect）的设计至关重要。
【3】 配置和扩展性
MyBatis允许开发者通过自定义插件来扩展其功能，针对主键自增功能，开发者可能会希望自定义插入操作的行为，例如获取主键的方式或插入后处理。因此，MyBatis的设计需要考虑到灵活的配置选项，以便开发者根据需求调整主键自增的实现方式。
主键自增的配置与生效
MyBatis 中的 KeyGenerator 实现类共有三种：Jdbc3KeyGenerator、SelectKeyGenerator、NoKeyGenerator。在实际使用时，这三种实现类中只能有一种实现类生效。
要启用 Jdbc3KeyGenerator，可以在配置文件中增加配置。

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/d5b4a846420746bdb49d42f08f2b7faa.png)
或者直接在相关语句上启用 useGeneratedKeys

```java
<insert id="insert" parameterType="cn.mybatis.test.po.User" useGeneratedKeys="true" keyProperty="id">
 </insert>
```
如果要启用 SelectKeyGenerator，则需要在 SQL语句前加一段 selectKey标签

```java
<insert id="insert" parameterType="cn.mybatis.test.po.User" >
 INSERT INTO t_user ...
  <selectKey keyProperty="id" order="AFTER" resultType="long">
            SELECT LAST_INSERT_ID()
        </selectKey>
 </insert>
```
如果某一条语句中同时设置了 useGeneratedKeys和 selectKey，则后者生效。
# KeyGenerator接口
## 概述
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/dab96aef5f50432b9813b05b59562f4f.png)

```java
public interface KeyGenerator {

    /**
     * 针对Sequence主键而言，在执行insert sql前必须指定一个主键值给要插入的记录，
     * 如Oracle、DB2，KeyGenerator提供了processBefore()方法。
     */
    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    /**
     * 针对自增主键的表，在插入时不需要主键，而是在插入过程自动获取一个自增的主键，
     * 比如MySQL、PostgreSQL，KeyGenerator提供了processAfter()方法
     */
    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
```
MyBatis 中的 KeyGenerator 实现类共有三种：Jdbc3KeyGenerator、SelectKeyGenerator、NoKeyGenerator。在实际使用时，这三种实现类中只能有一种实现类生效。
NoKeyGenerator：代表不具有任何的主键自增功能。
Jdbc3KeyGenerator：主要用于数据库的自增主键，如MySQL，PostgreSQL。
SelectKeyGenerator：主要用于数据库不支持自增主键的情况，如Oracle，DB2。

## SelectKeyGenerator分析
SelectKeyGenerator类主要体现在processAfter方法对processGeneratedKeys的调用处理。

```java
   private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                if (keyProperties != null) {
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
                    List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                    if (values.size() == 0) {
                        throw new RuntimeException("SelectKey returned no data.");
                    } else if (values.size() > 1) {
                        throw new RuntimeException("SelectKey returned more than one value.");
                    } else {
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));
                        if (keyProperties.length == 1) {
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error selecting key or setting result to parameter object. Cause: " + e);
        }
    }
```
首先检查传入的 parameter 参数对象是否为 null，keyStatement 是否为 null，以及 keyStatement 中的 keyProperties 是否为 null。keyProperties 用来定义主键属性的字段名。Object parameter是执行Insert的参数对象，待回写主键的对象。

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/f4a7c5cb96834dbb97d2bf346d014e51.png)
keyProperties是插入对象主键字段，比如userId。configuration 是 MyBatis 的配置对象，包含了 MyBatis 的配置信息。metaParam 是 parameter 对象的 MetaObject，用于通过反射操作 parameter 对象的字段。
ExecutorkeyExecutor= configuration.newExecutor(executor.getTransaction());

创建一个新的 Executor 对象，这个 Executor 用于执行查询语句（即获取主键的 SelectKey 语句）。它会共享当前事务 executor.getTransaction()。
对于MySQL来说是在执行插入SQL后，返回此条插入语句后的自增索引。当数据库中有俩条需要执行的SQL语句时，重点是必须在同一个数据源连接下，否则会失去事务的特性，如果不是在同一个数据源连接下，那么返回的自增ID的值将是0。

使用 keyExecutor 执行 keyStatement（一个 MappedStatement 对象，表示查询主键的 SQL 语句）并返回结果。parameter 作为查询参数传入，RowBounds.DEFAULT 表示没有分页，Executor.NO_RESULT_HANDLER 表示不使用结果处理器。

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/21b7cb246a264fe6b1e6a2998524d689.png)
keyExecutor.query实际就是复用了Insert执行语句的事务连接，并在执行Insert后执行selectKey标签的SELECT LAST_INSERT_ID()获取刚才执行的自增主键。LAST_INSERT_ID() 是一个 MySQL 函数，用来返回最后插入的自增列的 ID。它会返回当前连接的最后一次插入操作生成的自增值。
如果查询返回了单个结果，将其封装成 MetaObject，并通过 keyProperties 设置到 parameter 对象中。如果 keyProperties 只有一个属性，通过 setValue 方法将主键值设置到 parameter 对象。如果 keyProperties 有多个属性，调用 handleMultipleProperties 方法来处理。
然后就将获取的主键值通过反射赋值到Object parameter对象的keyProperties字段属性上，最终完成主键值的回写。

# 解析selectKey标签
<selectKey>标签会在解析mapper xml的INSERT语句中进行处理。
cn.bugstack.mybatis.builder.xml.XMLStatementBuilder#parseStatementNode

```java
    // Parse selectKey after includes and remove them.
    processSelectKeyNodes(id, parameterTypeClass, langDriver);
```

```java
  private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
    List<XNode> selectKeyNodes = context.evalNodes("selectKey");
    if (configuration.getDatabaseId() != null) {
      parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, configuration.getDatabaseId());
    }
    parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, null);
    removeSelectKeyNodes(selectKeyNodes);
  }
```

```java
private void parseSelectKeyNode(String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        String resultType = nodeToHandle.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));
        String keyProperty = nodeToHandle.attributeValue("keyProperty");

        // default
        String resultMap = null;
        KeyGenerator keyGenerator = new NoKeyGenerator();

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        // 调用助手类
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                keyGenerator,
                keyProperty,
                langDriver);

        // 给id加上namespace前缀
        id = builderAssistant.applyCurrentNamespace(id, false);

        // 存放键值生成器配置
        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
    }
```
解析 MyBatis 配置中的 <selectKey> 元素，构建一个用于获取生成主键的 SQL 语句，并将其作为 MappedStatement 注册到 MyBatis 配置中。它支持在插入操作之前或之后执行主键查询，并且能够将查询结果映射到指定的属性。

# 执行插入后执行获取主键查询
对于StatementHandler接口定义的方法，用于SQL语句执行时只有update和query，所以扩展的insert方法也是对update方法的扩展。
mybatis.executor.statement.PreparedStatementHandler#update

```java
    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
        return rows;
    }
```
将传入的 Statement 对象转换为 PreparedStatement，因为更新操作通常使用预编译的 SQL 语句。使用 execute() 方法执行 SQL 语句。这里使用 execute() 而不是 executeUpdate()，可能是为了兼容多种类型的 SQL 语句。execute() 可以执行所有类型的 SQL 语句，包括更新、插入、删除和查询。

getUpdateCount() 返回的是通过执行 SQL 语句所影响的行数，即被更新、插入或删除的记录数。
mappedStatement 是与当前 SQL 语句相关联的 MappedStatement 对象，它包含了执行该 SQL 所需的所有配置信息。通过 getKeyGenerator() 获取与当前 SQL 语句关联的主键生成器（KeyGenerator），这个生成器负责处理主键的生成和设置。
keyGenerator.processAfter 是 MyBatis 中 KeyGenerator 接口的一个方法，表示在执行 SQL 更新语句后对主键进行处理。
executor 是 MyBatis 中负责执行 SQL 语句的执行器，mappedStatement 是当前的映射语句，ps 是执行的 PreparedStatement 对象，parameterObject 是执行 SQL 语句时传入的参数对象。这个方法用于将生成的主键值从数据库查询中获取，并将其设置到参数对象的相应属性中。

在执行 SQL 更新（插入）操作，并在执行后处理生成的主键，它通过执行 PreparedStatement 更新语句获取影响的行数，并通过主键生成器的 processAfter 方法获取数据库生成的主键，将其设置到参数对象的相应属性中，从而确保在执行插入或更新操作时，主键能够正确地回填到参数对象中，最终返回更新操作影响的行数，实现了 MyBatis 中主键生成与对象映射的自动化处理，
这里的关键在于它在执行 SQL 更新操作时，利用同一个事务连接（executor.getTransaction()）来确保对数据库的操作保持一致性，并且通过主键生成器（KeyGenerator）在同一事务中处理主键回填问题。通过共用事务连接，MyBatis 保证了在执行插入或更新操作时，数据库生成的主键能够被正确获取并回填到参数对象中，同时不会发生跨事务的操作错误，从而实现了主键生成、事务一致性以及数据回填的自动化处理。

