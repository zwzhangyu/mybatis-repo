package com.zy.client.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

/*******************************************************
 * Created by ZhangYu on 2024/11/9
 * Description : SqlRunner测试类
 * History   :
 *******************************************************/
public class SqlRunnerTest {

  @Test
  public void test1() {
    try {
      // 数据库连接
      Connection connection =
        DriverManager.getConnection("jdbc:mysql://127.0.0.1/test?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", "root", "root");
      // 创建ScriptRunner对象
      ScriptRunner scriptRunner = new ScriptRunner(connection);
      // 读取classpath路径下的文件，返回Reader对象
      Reader reader = Resources.getResourceAsReader("sql/test_ddl.sql");
      // 执行SQL脚本
      scriptRunner.runScript(reader);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
