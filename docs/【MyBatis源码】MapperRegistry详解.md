# MapperRegistry 的作用
MapperRegistry 是 MyBatis 核心组件之一，主要负责管理 Mapper 接口的注册和获取。当通过 sqlSession.getMapper(XXXMapper.class) 获取 Mapper 实例时，会通过 MapperRegistry 返回相应的 Mapper 代理对象。
# 核心字段解析

```java
private final Configuration config;
private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
```
• Configuration：MyBatis 的全局配置对象，管理 MyBatis 的各种配置信息。
• knownMappers：用于存储已经注册的 Mapper 接口及其对应的 MapperProxyFactory。

# 整体工作流程
1. Mapper 注册：
   ◦ 通过 addMapper 方法注册 Mapper 接口，将其与 MapperProxyFactory 绑定，并解析注解或 XML 配置文件。
2. Mapper 获取：
   ◦ 通过 getMapper 方法获取 Mapper 实例，实际返回的是一个动态代理对象 (MapperProxy)。
3. 动态代理执行：
   ◦ 当调用 Mapper 方法时，代理对象 (MapperProxy) 会拦截方法调用，并根据方法上的注解或 XML 配置执行对应的 SQL 语句。
# addMapper方法
MapperRegistry#addMapper 是 MyBatis 框架中的一个核心方法，用于将 Mapper 接口注册到 MyBatis 环境中。这一过程确保 MyBatis 能够正确识别和管理 Mapper 接口，以便在实际使用时生成相应的代理对象。

```java

  public <T> void addMapper(Class<T> type) {
    // 判断是否是接口，如果不是接口，直接返回
    if (type.isInterface()) {
      if (hasMapper(type)) {
        // 防止重复注册，如果已经注册过，则抛出异常
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        // 添加到 knownMappers（一个 Map<Class<?>, MapperProxyFactory<?>>）中
        knownMappers.put(type, new MapperProxyFactory<>(type));
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        // 检查该接口是否有与之关联的 XML 映射文件
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
```
# MapperAnnotationBuilder#parse流程详解
MapperRegistry#addMapper方法流程最后是将解析流程交给了MapperAnnotationBuilder#parse，下面针对这个方法进行详细分析。
org.apache.ibatis.builder.annotation.MapperAnnotationBuilder#parse 是 MyBatis 中用来解析 @Mapper 接口中注解的方法，它的作用是扫描并处理 @Select, @Insert, @Update, @Delete 等 SQL 操作注解，生成对应的 MappedStatement 并将其注册到 Configuration 中。

```java
  public void parse() {
    String resource = type.toString();
    // 检查资源是否已加载
    if (!configuration.isResourceLoaded(resource)) {
      // 解析加载mapper关联的XML文件信息，如果是注解形式则为空
      loadXmlResource();
      configuration.addLoadedResource(resource);
      assistant.setCurrentNamespace(type.getName());
      // 解析缓存和缓存引用
      parseCache();
      parseCacheRef();
      // 获取当前 Mapper 接口中的所有方法
      for (Method method : type.getMethods()) {
        // 调用 canHaveStatement(method) 方法检查该方法是否适合生成 MappedStatement
        if (!canHaveStatement(method)) {
          continue;
        }
        // 处理Select和SelectProvider注解方法
        if (getAnnotationWrapper(method, false, Select.class, SelectProvider.class).isPresent()
            && method.getAnnotation(ResultMap.class) == null) {
          parseResultMap(method);
        }
        try {
          // 解析 Mapper 方法上的注解并生成 MappedStatement
          parseStatement(method);
        } catch (IncompleteElementException e) {
          configuration.addIncompleteMethod(new MethodResolver(this, method));
        }
      }
    }
    parsePendingMethods();
  }
```
• 通过 configuration.isResourceLoaded(resource) 检查当前 Mapper 是否已经被加载过，如果加载过则直接返回，避免重复加载。如果没有加载过，则将其标记为已加载。

• loadXmlResource用于加载 XML 资源（即 Mapper.xml 文件），并确保其与 @Mapper 注解的接口绑定在一起。如果 Mapper.xml 存在，则将其加载到 Configuration 中，并将其中的 select, insert, update, delete 等语句转换为 MappedStatement。

• assistant.setCurrentNamespace(type.getName()) 的作用是将当前 Mapper 接口的全限定类名（如 com.example.mapper.UserMapper）设置为命名空间。MyBatis 中的命名空间是用于隔离不同 Mapper 的关键机制，确保每个 MappedStatement 的 id 是唯一的，避免冲突。

• canHaveStatement(Method method) 方法用于判断当前 Mapper 接口中的某个方法是否可以解析为 MyBatis 的 MappedStatement。这意味着只有符合条件的方法，MyBatis 才会尝试为其生成对应的 SQL 映射语句（MappedStatement）。method.isBridge()排除桥接方法。method.isDefault() 用于检查该方法是否为 Java 8 引入的接口默认方法。
# MapperAnnotationBuilder#parseStatement
这段代码是 org.apache.ibatis.builder.annotation.MapperAnnotationBuilder 类的一部分，其作用是从 Mapper接口的方法（通常标注有 @Select、@Insert、@Update 或 @Delete 注解的方法）中提取信息，并构建出 MappedStatement 对象，用于 SQL 的执行。
以下是方法的总体思路：
1. 解析方法参数类型。
2. 获取方法所使用的语言驱动（LanguageDriver）。
3. 从注解中构建 SqlSource 和 SqlCommandType。
4. 解析主键生成策略。
5. 解析缓存、超时、结果集等配置。
6. 生成并注册 MappedStatement。

```java
final Class<?> parameterTypeClass = getParameterType(method);
final LanguageDriver languageDriver = getLanguageDriver(method);
```
• getParameterType(method)：获取方法的参数类型。如果方法有多个参数，则 MyBatis 会将其封装为 Map 类型。

• getLanguageDriver(method)：确定使用哪种语言驱动，默认是 XMLLanguageDriver。语言驱动用于解析 SQL 语句，可以是注解中的 SQL，也可以是 XML 配置中的 SQL。

```java
getAnnotationWrapper(method, true, statementAnnotationTypes).ifPresent(statementAnnotation -> {
    final SqlSource sqlSource = buildSqlSource(
        statementAnnotation.getAnnotation(), parameterTypeClass, languageDriver, method
    );
    final SqlCommandType sqlCommandType = statementAnnotation.getSqlCommandType();
```

```java
  private SqlSource buildSqlSource(Annotation annotation, Class<?> parameterType, LanguageDriver languageDriver,
      Method method) {
    if (annotation instanceof Select) {
      return buildSqlSourceFromStrings(((Select) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof Update) {
      return buildSqlSourceFromStrings(((Update) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof Insert) {
      return buildSqlSourceFromStrings(((Insert) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof Delete) {
      return buildSqlSourceFromStrings(((Delete) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof SelectKey) {
      return buildSqlSourceFromStrings(((SelectKey) annotation).statement(), parameterType, languageDriver);
    }
    return new ProviderSqlSource(assistant.getConfiguration(), annotation, type, method);
  }
```
• getAnnotationWrapper()：获取方法上的 SQL 注解（如 @Select、@Insert 等）。

• buildSqlSource()：根据注解内容生成 SqlSource 对象，SqlSource 用于封装 SQL 语句，并处理占位符参数（如 #{}）。

• getSqlCommandType()：确定 SQL 命令类型，如 SELECT、INSERT、UPDATE、DELETE。

```java
      final Options options = getAnnotationWrapper(method, false, Options.class).map(x -> (Options)x.getAnnotation()).orElse(null);

```
它的作用是从 Mapper 方法中获取 @Options 注解，并将其解析为 Options 对象，以便在后续配置 MappedStatement时使用。MyBatis 提供了 @Options 注解，用于配置 Mapper 方法的一些额外选项。常见的配置选项包括：useCache：是否启用二级缓存。timeout：SQL 执行超时时间。

### 处理主键生成策略（KeyGenerator）

```java
// 处理主键生成策略（KeyGenerator）
      final KeyGenerator keyGenerator;
      String keyProperty = null;
      String keyColumn = null;
      if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
        // 如果方法上有 @SelectKey 注解，使用其生成主键
        SelectKey selectKey = getAnnotationWrapper(method, false, SelectKey.class).map(x -> (SelectKey)x.getAnnotation()).orElse(null);
        if (selectKey != null) {
          keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
          keyProperty = selectKey.keyProperty();
        } else if (options == null) {
          // 默认使用 JDBC3 的主键生成方式
          keyGenerator = configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
        } else {
          keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
          keyProperty = options.keyProperty();
          keyColumn = options.keyColumn();
        }
      } else {
        keyGenerator = NoKeyGenerator.INSTANCE;
      }
```
• 如果 SqlCommandType 是 INSERT 或 UPDATE，则可能需要生成主键。

• 优先使用 @SelectKey 注解来生成主键；如果没有，则根据配置选择是否自动生成主键。

• keyProperty 和 keyColumn 用于指定主键映射。

### 处理缓存、超时、ResultMap 等配置

```java
  // 处理缓存、超时、ResultMap 等配置
      Integer fetchSize = null;
      Integer timeout = null;
      // statementType：语句类型，默认为 PREPARED，还可以是 STATEMENT 或 CALLABLE。
      StatementType statementType = StatementType.PREPARED;
      ResultSetType resultSetType = configuration.getDefaultResultSetType();
      boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
      boolean flushCache = !isSelect;
      boolean useCache = isSelect;
      if (options != null) {
        // 从方法上的 @Options 注解中提取配置，如 flushCache、useCache、fetchSize 和 timeout。
        if (FlushCachePolicy.TRUE.equals(options.flushCache())) {
          flushCache = true;
        } else if (FlushCachePolicy.FALSE.equals(options.flushCache())) {
          flushCache = false;
        }
        useCache = options.useCache();
        fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
        timeout = options.timeout() > -1 ? options.timeout() : null;
        statementType = options.statementType();
        if (options.resultSetType() != ResultSetType.DEFAULT) {
          resultSetType = options.resultSetType();
        }
      }
```
### 处理 ResultMap

```java
 // 处理 ResultMap
      String resultMapId = null;
      if (isSelect) {
        // 如果方法是 SELECT 查询，则可能需要 ResultMap 来映射结果集到 Java 对象。
        ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
        if (resultMapAnnotation != null) {
          // 优先使用 @ResultMap 注解；如果没有，则自动生成 ResultMap。
          resultMapId = String.join(",", resultMapAnnotation.value());
        } else {
          resultMapId = generateResultMapName(method);
        }
      }
```
### 注册 MappedStatement

```java
 // 注册 MappedStatement
      assistant.addMappedStatement(
          mappedStatementId,
          sqlSource,
          statementType,
          sqlCommandType,
          fetchSize,
          timeout,
          // ParameterMapID
          null,
          parameterTypeClass,
          resultMapId,
          getReturnType(method),
          resultSetType,
          flushCache,
          useCache,
          // TODO gcode issue #577
          false,
          keyGenerator,
          keyProperty,
          keyColumn,
          statementAnnotation.getDatabaseId(),
          languageDriver,
          // ResultSets
          options != null ? nullOrEmpty(options.resultSets()) : null);
```
• 调用 assistant.addMappedStatement() 将解析好的信息封装成 MappedStatement 并注册到 MyBatis 的 Configuration 中。

• MappedStatement 包含了执行 SQL 所需的所有信息，包括 SQL 语句、参数类型、结果集映射、缓存策略等。