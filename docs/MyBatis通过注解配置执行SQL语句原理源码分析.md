> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)

### 前置准备
创建一个mybatis-config.xml文件，配置mapper接口

```java
<mappers>
        <!--注解配置-->
        <mapper class="mybatis.test.dao.IUserDao"/>
    </mappers>
```
创建一个mapper接口，使用注解方式编写SQL

```java
public interface IUserDao {

    @Select("SELECT id, userId, userName, userHead\n" +
            "FROM user\n" +
            "where id = #{id}")
    User queryUserInfoById(Long id);

    @Select("SELECT id, userId, userName, userHead\n" +
            "        FROM user\n" +
            "        where id = #{id}")
    User queryUserInfo(User req);

    @Select("SELECT id, userId, userName, userHead\n" +
            "FROM user")
    List<User> queryUserInfoList();

    @Update("UPDATE user\n" +
            "SET userName = #{userName}\n" +
            "WHERE id = #{id}")
    int updateUserInfo(User req);

    @Insert("INSERT INTO user\n" +
            "(userId, userName, userHead, createTime, updateTime)\n" +
            "VALUES (#{userId}, #{userName}, #{userHead}, now(), now())")
    void insertUserInfo(User req);

    @Insert("DELETE FROM user WHERE userId = #{userId}")
    int deleteUserInfoByUserId(String userId);

}
```
### 流程简要分析
【1】 解析 mybatis-config.xml 配置文件
MyBatis 启动时加载 mybatis-config.xml 配置文件，初始化全局配置。
【2】通过配置的mapper路径反射 加载 Mapper 接口
MapperRegistry 注册 Mapper 接口类，检查每个 Mapper 是否已注册。
【3】 创建 MapperProxyFactory
为每个 Mapper 接口创建 MapperProxyFactory，用于生成接口的代理对象。
【4】 解析接口方法注解
MapperAnnotationBuilder 解析接口方法上的 SQL 注解（如 @Select、@Insert 等）。
【5】 注册 SQL 映射
通过 MappedStatement 将 SQL 语句与方法绑定，并注册到 Configuration 中。
【6】 生成代理对象
MapperProxyFactory 使用代理模式为 Mapper 接口生成代理对象。
【7】 执行 SQL 语句
代理对象拦截方法调用，解析注解中的 SQL，交由 MyBatis 执行。

### 配置文件解析

```java
    /**
     * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return Configuration
     */
    public Configuration parse() {
        try {
            // 环境
            environmentsElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }
```
mapperElement(root.element("mappers"));代码解析出配置文件中配置的<mappers>节点，获取节点对象。
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/06e927024018458cbf8f79416667e246.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/eb94613e9dd54784b800cf4e07570e6b.png)
遍历获取的<mapper>节点，在这里会根据mapper属性中的resource和class来判断是加载xml还是接口配置SQL。如果resource不为空说明这里关联的一个外部文件，则执行xml文件的解析和SQL解析。如果是class，则进行接口注解配置方式的解析。然后通过Resources.classForName(mapperClass)加载类对象。此时就获取到了关联的接口类型。

### 加载 Mapper 接口

```java
    public <T> void addMapper(Class<T> type) {
        /* Mapper 必须是接口才会注册 */
        if (type.isInterface()) {
            if (hasMapper(type)) {
                // 如果重复添加了，报错
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));

            // 解析注解类语句配置
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            parser.parse();
        }
    }
```
这个方法用于将 Mapper 接口注册到 MyBatis 中。它的目的是确保 Mapper 接口可以通过注解配置 SQL 语句，并且为该接口创建代理对象。
首先检查传入的 type 是否为接口。只有接口才能作为 Mapper 接口在 MyBatis 中注册。因为 MyBatis 使用 Java 的动态代理来为接口生成实现类，如果是类（而非接口），则不能注册。hasMapper(type) 检查该 Mapper 接口是否已经被注册过。如果已经注册，抛出 RuntimeException 异常，避免重复注册同一个接口。这样做是为了防止配置冲突或逻辑错误。
通过 MapperProxyFactory 为指定的 type 接口类型创建代理工厂，并将其存储到 knownMappers 中，knownMappers是一个 Map<Class<T>, MapperProxyFactory<T>> 类型的集合，记录了所有已注册的 Mapper 类型及其对应的代理工厂。
MapperProxyFactory 是 MyBatis 的一个代理工厂类，它负责生成指定 Mapper 接口的代理对象。通过动态代理，MyBatis 可以拦截接口方法调用，并执行相应的 SQL 操作。
MapperAnnotationBuilder 负责解析 Mapper 接口中的注解配置（如 @Select、@Insert 等），并将注解中的 SQL 语句解析并注册到 MyBatis 的 MappedStatement 中。

### MapperAnnotationBuilder解析接口方法注解

mybatis.builder.annotation.MapperAnnotationBuilder#parse

```java
    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            assistant.setCurrentNamespace(type.getName());

            Method[] methods = type.getMethods();
            for (Method method : methods) {
                if (!method.isBridge()) {
                    // 解析语句
                    parseStatement(method);
                }
            }
        }
    }
```
parse() 方法的功能是遍历 Mapper 接口中的每个方法，解析其中的 SQL 注解，并将对应的 SQL 语句注册到 MyBatis 的配置中，使得接口方法能够直接执行对应的 SQL 语句。
通过 type.getMethods() 获取当前 Mapper 接口的所有方法（包括继承自 Object 类的方法）。
遍历每个方法，使用 method.isBridge() 排除桥接方法（泛型方法的代理方法）。
对每个非桥接方法，调用 parseStatement(method) 来解析该方法上的 SQL 注解，提取出 SQL 语句，并进行注册。桥接方法是 Java 编译器为支持泛型而生成的特殊方法，通常与实际的业务逻辑无关，所以在解析时被跳过。
### parseStatement 方法详解

```java
  /**
     * 解析语句
     */
    private void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getLanguageDriver(method);
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);

        if (sqlSource != null) {
            final String mappedStatementId = type.getName() + "." + method.getName();
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if (isSelect) {
                resultMapId = parseResultMap(method);
            }
        }
    }
```
parseStatement 方法是 MyBatis 中 MapperAnnotationBuilder 类的一个重要方法，用于解析 Mapper 接口中的 SQL 注解（如 @Select、@Insert 等），并根据注解创建 SqlSource 和相关的 SQL 配置（MappedStatement）。
通过 getParameterType(method) 获取当前方法的参数类型。这是为了确定 SQL 执行时需要传入的参数类型，以便正确地构造 SQL 语句。
调用 getLanguageDriver(method) 获取与该方法对应的 LanguageDriver，它用于解析 SQL 语句，并确定如何处理注解中的 SQL。
通过 getSqlSourceFromAnnotations() 方法，根据方法上的 SQL 注解（如 @Select、@Insert）获取 SqlSource。SqlSource 是 MyBatis 中封装 SQL 语句和参数的对象。
通过 type.getName()（Mapper 接口的类名）和 method.getName()（方法名）拼接得到 MappedStatement 的唯一标识符（ID）。这个 ID 用于在 MyBatis 中唯一标识一个 SQL 语句。
通过 getSqlCommandType(method) 获取当前方法对应的 SQL 类型（如 SELECT、INSERT 等）。如果是 SELECT 类型的 SQL，isSelect 设置为 true。
如果 SQL 类型是 SELECT，则通过 parseResultMap(method) 获取查询结果的 ResultMap ID。ResultMap 用于将查询结果的字段映射到 Java 对象。

### MapperBuilderAssistant

```java
/**
     * 添加映射器语句
     */
    public MappedStatement addMappedStatement(
            String id,
            SqlSource sqlSource,
            SqlCommandType sqlCommandType,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            LanguageDriver lang
    ) {
        // 给id加上namespace前缀：xxx.test.dao.IUserDao.queryUserInfoById
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);

        // 结果映射，给 MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement statement = statementBuilder.build();
        // 映射语句信息，建造完存放到配置项中
        configuration.addMappedStatement(statement);

        return statement;
    }
```
addMappedStatement 方法是 MyBatis 中用于创建并注册 SQL 映射语句的核心方法之一。它接受多个参数，用于配置一个完整的 SQL 语句映射并将其添加到 MyBatis 的 Configuration 中.
使用 MappedStatement.Builder 创建一个 MappedStatement 对象。MappedStatement 是 MyBatis 用于封装 SQL 语句信息、参数类型、返回结果等数据的核心对象。
调用 setStatementResultMap 方法，为 MappedStatement 设置结果映射。resultMap 定义了 SQL 查询结果与 Java 对象之间的映射关系。
通过 statementBuilder.build() 方法构建最终的 MappedStatement 对象。此时，MappedStatement 包含了 SQL 语句源、ID、结果类型、命令类型等信息。
将构建完成的 MappedStatement 添加到 MyBatis 的全局配置（configuration）中，确保该 SQL 映射信息被 MyBatis 管理和执行。
这个方法主要用于将从注解解析的 SQL 语句及相关配置注册到 MyBatis 中，使得 MyBatis 能够执行对应的 SQL 语句并正确地映射结果。