package com.zy.client.test;

import com.zy.client.bean.MyObject;
import com.zy.client.bean.UserBean;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/11/12
 * Description : TypeHandler测试类
 * History   :
 *******************************************************/
public class TypeHandlerTest {


  @Test
  public void test2() throws SQLException, IOException {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    Configuration configuration = sqlSessionFactory.getConfiguration();
    // 手动注册mapper
    configuration.addMapper(UserMapper.class);
    configuration.setProxyFactory(new MyLoggingProxyFactory());
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    UserBean userBean = mapper.selectDataById(1);
    System.out.println(userBean);
  }

  @Test
  public void test1() throws SQLException, IOException {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    Connection connection = sqlSession.getConnection();
    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM t_user WHERE id = ?");
    stmt.setInt(1, 123);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
      int id = rs.getInt("id");
      String name = rs.getString("name");
      Date birthDate = rs.getDate("birth_date");
    }
  }
}
