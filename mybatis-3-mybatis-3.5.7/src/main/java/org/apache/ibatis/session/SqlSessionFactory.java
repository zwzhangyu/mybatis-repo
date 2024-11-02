/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 *
 * @author Clinton Begin
 */
public interface SqlSessionFactory {

  /**
   * 打开一个默认的SqlSession会话，一般使用默认配置创建SqlSession，通常是非自动提交模式。
   */
  SqlSession openSession();

  /**
   * 打开一个新的SqlSession，并指定是否自动提交事务。
   * autoCommit为true时会自动提交；为false时需要手动提交或回滚事务。
   */
  SqlSession openSession(boolean autoCommit);

  /**
   * 使用已有的数据库连接Connection创建一个SqlSession。
   * 这种方式适用于已有连接池或自定义数据库连接的情况。
   */
  SqlSession openSession(Connection connection);

  /**
   *根据指定的事务隔离级别TransactionIsolationLevel创建SqlSession，可用于控制事务的隔离级别
   */
  SqlSession openSession(TransactionIsolationLevel level);

  /**
   *根据指定的执行器类型ExecutorType创建SqlSession。ExecutorType可以是以下几种：
   *
   * SIMPLE：简单的执行器，每次执行语句都会创建一个新的预处理语句。
   * REUSE：重用执行器，重用预处理语句。
   * BATCH：批量执行的执行器，可以提高批量插入、更新的效率。
   */
  SqlSession openSession(ExecutorType execType);

  SqlSession openSession(ExecutorType execType, boolean autoCommit);

  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

  SqlSession openSession(ExecutorType execType, Connection connection);

  Configuration getConfiguration();

}
