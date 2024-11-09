/**
 *    Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;

/**
 * @author Clinton Begin
 */
public interface StatementHandler {

  /**
   * 该方法用于创建JDBC Statement对象，并完成Statement对象的属性设置
   */
  Statement prepare(Connection connection, Integer transactionTimeout)
      throws SQLException;

  /**
   * 该方法使用MyBatis中的ParameterHandler组件为PreparedStatement和CallableStatement参数占位符设置值。
   */
  void parameterize(Statement statement)
      throws SQLException;

  /**
   * 将SQL命令添加到批处量执行列表中。
   */
  void batch(Statement statement)
      throws SQLException;

  /**
   *调用Statement对象的execute()方法执行更新语句，例如UPDATE、INSERT、DELETE语句。
   */
  int update(Statement statement)
      throws SQLException;

  /**
   *执行查询语句，并使用ResultSetHandler处理查询结果集。
   */
  <E> List<E> query(Statement statement, ResultHandler resultHandler)
      throws SQLException;

  /**
   *带游标的查询，返回Cursor对象，能够通过Iterator动态地从数据库中加载数据，适用于查询数据量较大的情况，避免将所有数据加载到内存中。
   */
  <E> Cursor<E> queryCursor(Statement statement)
      throws SQLException;

  /**
   * 获取Mapper中配置的SQL信息，BoundSql封装了动态SQL解析后的SQL文本和参数映射信息。
   */
  BoundSql getBoundSql();

  /**
   * 获取ParameterHandler实例
   */
  ParameterHandler getParameterHandler();

}
