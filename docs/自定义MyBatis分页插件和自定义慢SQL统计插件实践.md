> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](zwzhangyu.blog.csdn.net)


# 自定义MyBatis分页插件实践
### 说明
在 MyBatis 项目中，自定义分页插件是一种灵活且高效的方式，用于满足项目的分页需求。
实现思路
• 拦截 SQL 执行
利用 MyBatis 的插件机制，通过拦截器对 StatementHandler 进行拦截，修改原始 SQL，加入分页逻辑。
• 动态拼接分页语句
根据数据库类型（如 MySQL、Oracle 等），拼接对应的分页 SQL。
• 设置分页参数
使用 ThreadLocal 或其他方式传递分页参数。

### 代码
创建分页参数对象

```java
/*******************************************************
 * Created by ZhangYu on 2024/11/24
 * Description : 分页接口
 * History   :
 *******************************************************/
public interface Paginable<T> {

  /** 总记录数 */
  int getTotalCount();

  /** 总页数 */
  int getTotalPage();

  /** 每页记录数 */
  int getPageSize();

  /** 当前页号 */
  int getPageNo();

  /** 是否第一页 */
  boolean isFirstPage();

  /** 是否最后一页 */
  boolean isLastPage();

  /** 返回下页的页号 */
  int getNextPage();

  /** 返回上页的页号 */
  int getPrePage();

  /** 取得当前页显示的项的起始序号 */
  int getBeginIndex();

  /** 取得当前页显示的末项序号 */
  int getEndIndex();
  /** 获取开始页*/
  int getBeginPage();
  /** 获取结束页*/
  int getEndPage();
}
```

```java
/*******************************************************
 * Created by ZhangYu on 2024/11/24
 * Description : 分页实现类
 * History   :
 *******************************************************/
public class Page<T>  implements Paginable<T> {

  public static final int DEFAULT_PAGE_SIZE = 10; // 默认每页记录数

  public static final int PAGE_COUNT = 10;

  private int pageNo = 1; // 页码

  private int pageSize = DEFAULT_PAGE_SIZE; // 每页记录数

  private int totalCount = 0; // 总记录数

  private int totalPage = 0; // 总页数

  private long timestamp = 0; // 查询时间戳

  private boolean full = false; // 是否全量更新 //false 不更新totalcount

  public int getPageNo() {
    return pageNo;
  }

  public void setPageNo(int pageNo) {
    this.pageNo = pageNo;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
    int totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
    this.setTotalPage(totalPage);
  }

  public int getTotalPage() {
    return totalPage;
  }

  public void setTotalPage(int totalPage) {
    this.totalPage = totalPage;
  }

  public boolean isFirstPage() {
    return pageNo <= 1;
  }

  public boolean isLastPage() {
    return pageNo >= totalPage;
  }

  public int getNextPage() {
    return isLastPage() ? pageNo : (pageNo + 1);
  }

  public int getPrePage() {
    return isFirstPage() ? pageNo : (pageNo - 1);
  }

  public int getBeginIndex() {
    if (pageNo > 0) {
      return (pageSize * (pageNo - 1));
    } else {
      return 0;
    }
  }

  public int getEndIndex() {
    if (pageNo > 0) {
      return Math.min(pageSize * pageNo, totalCount);
    } else {
      return 0;
    }
  }

  public int getBeginPage() {
    if (pageNo > 0) {
      return (PAGE_COUNT * ((pageNo - 1) / PAGE_COUNT)) + 1;
    } else {
      return 0;
    }
  }

  public int getEndPage() {
    if (pageNo > 0) {
      return Math.min(PAGE_COUNT * ((pageNo - 1) / PAGE_COUNT + 1), getTotalPage());
    } else {
      return 0;
    }
  }

  public boolean isFull() {
    return full;
  }

  public void setFull(boolean full) {
    this.full = full;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
```

通过拦截器实现分页逻辑。

```java
/*******************************************************
 * Created by ZhangYu on 2024/11/24
 * Description : 分页插件
 * History   :
 *******************************************************/
@Intercepts({
  @Signature(method = "prepare", type = StatementHandler.class, args = {Connection.class, Integer.class})
})
public class PageInterceptor implements Interceptor {

  private String databaseType;

  public Object intercept(Invocation invocation) throws Throwable {
    // 获取拦截的目标对象
    RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();
    StatementHandler delegate = (StatementHandler) ReflectionUtils.getFieldValue(handler, "delegate");
    BoundSql boundSql = delegate.getBoundSql();
    // 获取参数对象，当参数对象为Page的子类时执行分页操作
    Object parameterObject = boundSql.getParameterObject();
    if (parameterObject instanceof Page<?>) {
      Page<?> page = (Page<?>) parameterObject;
      MappedStatement mappedStatement = (MappedStatement) ReflectionUtils.getFieldValue(delegate, "mappedStatement");
      Connection connection = (Connection) invocation.getArgs()[0];
      String sql = boundSql.getSql();
      if (page.isFull()) {
        // 获取记录总数
        this.setTotalCount(page, mappedStatement, connection);
      }
      page.setTimestamp(System.currentTimeMillis());
      // 获取分页SQL
      String pageSql = this.getPageSql(page, sql);
      // 将原始SQL语句替换成分页语句
      ReflectionUtils.setFieldValue(boundSql, "sql", pageSql);
    }
    return invocation.proceed();
  }

  /**
   * 拦截器对应的封装原始对象的方法
   */
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  /**
   * 设置注册拦截器时设定的属性
   */
  public void setProperties(Properties properties) {
    this.databaseType = properties.getProperty("databaseType");
  }

  /**
   * 根据page对象获取对应的分页查询Sql语句，
   * 这里只做了三种数据库类型，Mysql、Oracle、HSQLDB
   * 其它的数据库都没有进行分页
   *
   * @param page 分页对象
   * @param sql  原始sql语句
   * @return
   */
  private String getPageSql(Page<?> page, String sql) {
    StringBuffer sqlBuffer = new StringBuffer(sql);
    if ("mysql".equalsIgnoreCase(databaseType)) {
      return getMysqlPageSql(page, sqlBuffer);
    } else if ("oracle".equalsIgnoreCase(databaseType)) {
      return getOraclePageSql(page, sqlBuffer);
    } else if ("hsqldb".equalsIgnoreCase(databaseType)) {
      return getHSQLDBPageSql(page, sqlBuffer);
    }
    return sqlBuffer.toString();
  }

  /**
   * 获取Mysql数据库的分页查询语句
   *
   * @param page      分页对象
   * @param sqlBuffer 包含原sql语句的StringBuffer对象
   * @return Mysql数据库分页语句
   */
  private String getMysqlPageSql(Page<?> page, StringBuffer sqlBuffer) {
    int offset = (page.getPageNo() - 1) * page.getPageSize();
    sqlBuffer.append(" limit ").append(offset).append(",").append(page.getPageSize());
    return sqlBuffer.toString();
  }

  /**
   * 获取Oracle数据库的分页查询语句
   *
   * @param page      分页对象
   * @param sqlBuffer 包含原sql语句的StringBuffer对象
   * @return Oracle数据库的分页查询语句
   */
  private String getOraclePageSql(Page<?> page, StringBuffer sqlBuffer) {
    int offset = (page.getPageNo() - 1) * page.getPageSize() + 1;
    sqlBuffer.insert(0, "select u.*, rownum r from (").append(") u where rownum < ")
      .append(offset + page.getPageSize());
    sqlBuffer.insert(0, "select * from (").append(") where r >= ").append(offset);
    return sqlBuffer.toString();
  }


  /**
   * 获取HSQLDB数据库的分页查询语句
   *
   * @param page      分页对象
   * @param sqlBuffer 包含原sql语句的StringBuffer对象
   * @return Oracle数据库的分页查询语句
   */
  private String getHSQLDBPageSql(Page<?> page, StringBuffer sqlBuffer) {
    int offset = (page.getPageNo() - 1) * page.getPageSize() + 1;
    String sql = "select limit " + offset + " " + page.getPageSize() + " * from (" + sqlBuffer.toString() + " )";
    return sql;
  }

  /**
   * 给当前的参数对象page设置总记录数
   *
   * @param page            Mapper映射语句对应的参数对象
   * @param mappedStatement Mapper映射语句
   * @param connection      当前的数据库连接
   */
  private void setTotalCount(Page<?> page, MappedStatement mappedStatement, Connection connection) {
    BoundSql boundSql = mappedStatement.getBoundSql(page);
    String sql = boundSql.getSql();
    // 获取总记录数
    String countSql = this.getCountSql(sql);
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql, parameterMappings, page);
    ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, page, countBoundSql);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = connection.prepareStatement(countSql);
      parameterHandler.setParameters(pstmt);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        int totalCount = rs.getInt(1);
        page.setTotalCount(totalCount);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(rs);
      IOUtils.closeQuietly(pstmt);
    }
  }

  /**
   * 根据原Sql语句获取对应的查询总记录数的Sql语句
   *
   * @param sql
   * @return
   */
  private String getCountSql(String sql) {
    return "select count(1) " + sql.substring(sql.toLowerCase().indexOf("from"));
  }

}
```
自定义的插件类还需要通过Intercepts注解指定对哪些组件的哪些方法进行拦截。这里我们指定对StatementHandler实例的prepare()方法进行拦截，因此在调用StatementHandler对象的prepare()方法之前都会调用PageInterceptor 对象的intercept()方法，我们只需要在该方法中把执行的SQL语句替换成分页查询SQL即可。
在PageInterceptor类的intercept()方法中，我们通过反射机制获取到BoundSql对象，BoundSql封装了执行的SQL语句和参数对象，我们需要获取SQL语句和参数对象，如果参数对象是Page的子类，则调用getPageSql()方法把SQL语句包装成分页语句
# 自定义慢SQL统计插件
### 说明
在 MyBatis 项目中，自定义慢 SQL 统计插件可以帮助我们监控执行时间较长的 SQL，优化数据库性能。
实现思路
1. 拦截 SQL 执行
   使用 MyBatis 的插件机制拦截 StatementHandler 或 Executor 执行方法。
2. 记录执行时间
   在 SQL 执行前后记录时间差，计算 SQL 的执行耗时。
3. 识别慢 SQL
   根据预设的阈值（如 500ms）判断是否属于慢 SQL。
4. 日志记录或告警
   将慢 SQL 记录到日志中，或者通过告警机制通知相关人员。
### 代码

```java
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.defaults.DefaultSqlSession;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Intercepts({
  @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
  @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
  @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

  private Integer limitSecond;

  @Override
  public Object intercept(Invocation invocation) throws InvocationTargetException, IllegalAccessException {
    long beginTimeMillis = System.currentTimeMillis();
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    try {
      return invocation.proceed();
    } finally {
      long endTimeMillis = System.currentTimeMillis();
      long costTimeMillis = endTimeMillis - beginTimeMillis;
      if (costTimeMillis > limitSecond * 1000) {
        BoundSql boundSql = statementHandler.getBoundSql();
        // 调用getForMatedSql（）方法对参数占位符进行替换
        String sql = 调用getForMatedSql(boundSql);
        System.out.println("SQL语句【" + sql + "】，执行耗时：" + costTimeMillis + "ms");
      }
    }
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {
    String limitSecond = (String) properties.get("limitSecond");
    this.limitSecond = Integer.parseInt(limitSecond);
  }

  private String 调用getForMatedSql(BoundSql boundSql) {
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    sql = beautifySql(sql);
    if (parameterObject == null || parameterMappings == null || parameterMappings.isEmpty()) {
      return sql;
    }
    String sqlWithoutReplacePlaceholder = sql;
    try {
      if (parameterMappings != null) {
        Class<?> parameterObjectClass = parameterObject.getClass();
        if (isStrictMap(parameterObjectClass)) {
          DefaultSqlSession.StrictMap<Collection<?>> strictMap = (DefaultSqlSession.StrictMap<Collection<?>>) parameterObject;
          if (isList(strictMap.get("list").getClass())) {
            sql = handleListParameter(sql, strictMap.get("list"));
          }
        } else if (isMap(parameterObjectClass)) {
          Map<?, ?> paramMap = (Map<?, ?>) parameterObject;
          sql = handleMapParameter(sql, paramMap, parameterMappings);
        } else {
          sql = handleCommonParameter(sql, parameterMappings, parameterObjectClass, parameterObject);
        }
      }
    } catch (Exception e) {
      return sqlWithoutReplacePlaceholder;
    }
    return sql;

  }

  private String beautifySql(String sql) {
    sql = sql.replace("\n", "")
      .replace("\t", "")
      .replace("  ", " ")
      .replace("( ", "(")
      .replace(" )", ")")
      .replace(" ,", ",");
    return sql;

  }

  private String handleListParameter(String sql, Collection<?> col) {
    if (col != null && col.size() != 0) {
      for (Object obj : col) {
        String value = null;
        Class<?> objClass = obj.getClass();
        if (isPrimitiveOrPrimitiveWrapper(objClass)) {
          value = obj.toString();
        } else if (objClass.isAssignableFrom(String.class)) {
          value = "\"" + obj.toString() + "\"";
        }
        sql = sql.replaceFirst("\\?", value);
      }
    }
    return sql;

  }

  private String handleMapParameter(String sql, Map<?, ?> paramMap, List<ParameterMapping> parameterMappingList) {
    for (ParameterMapping parameterMapping : parameterMappingList) {
      Object propertyName = parameterMapping.getProperty();
      Object propertyValue = paramMap.get(propertyName);
      if (propertyValue != null) {
        if (propertyValue.getClass().isAssignableFrom(String.class)) {
          propertyValue = "\"" + propertyValue + "\"";
        }
        sql = sql.replaceFirst("\\?", propertyValue.toString());
      }
    }
    return sql;

  }


  private String handleCommonParameter(String sql, List<ParameterMapping> parameterMappingList,
                                       Class<?> parameterObjectClass,
                                       Object parameterObject) throws Exception {
    for (ParameterMapping parameterMapping : parameterMappingList) {
      String propertyValue = null;
      if (isPrimitiveOrPrimitiveWrapper(parameterObjectClass)) {
        propertyValue = parameterObject.toString();
      } else {
        String propertyName = parameterMapping.getProperty();
        Field field = parameterObjectClass.getDeclaredField(propertyName);
        field.setAccessible(true);
        propertyValue = String.valueOf(field.get(parameterObject));
        if (parameterMapping.getJavaType().isAssignableFrom(String.class)) {
          propertyValue = "\"" + propertyValue + "\"";
        }
      }
      sql = sql.replaceFirst("\\?", propertyValue);
    }
    return sql;

  }

  private boolean isPrimitiveOrPrimitiveWrapper(Class<?> parameterObjectClass) {
    return parameterObjectClass.isPrimitive() ||
      (parameterObjectClass.isAssignableFrom(Byte.class)
        || parameterObjectClass.isAssignableFrom(Short.class)
        || parameterObjectClass.isAssignableFrom(Integer.class)
        || parameterObjectClass.isAssignableFrom(Long.class)
        || parameterObjectClass.isAssignableFrom(Double.class)
        || parameterObjectClass.isAssignableFrom(Float.class)
        || parameterObjectClass.isAssignableFrom(Character.class)
        || parameterObjectClass.isAssignableFrom(Boolean.class));
  }

  private boolean isStrictMap(Class<?> parameterObjectClass) {
    return parameterObjectClass.isAssignableFrom(DefaultSqlSession.StrictMap.class);

  }


  private boolean isList(Class<?> clazz) {
    Class<?>[] interfaceClasses = clazz.getInterfaces();
    for (Class<?> interfaceClass : interfaceClasses) {
      if (interfaceClass.isAssignableFrom(List.class)) {
        return true;
      }
    }
    return false;

  }


  private boolean isMap(Class<?> parameterObjectClass) {
    Class<?>[] interfaceClasses = parameterObjectClass.getInterfaces();
    for (Class<?> interfaceClass : interfaceClasses) {
      if (interfaceClass.isAssignableFrom(Map.class)) {
        return true;
      }
    }
    return false;

  }


}

```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/9d7e607907f1447f9c9e285a50fc90d8.png)
