package com.zy.client.result;

import com.zy.client.bean.UserBean;
import com.zy.client.result.po.User;
import com.zy.client.test.MyLoggingProxyFactory;
import com.zy.client.test.UserMapper;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/10/23
 * Description :
 * History   :
 *******************************************************/
public class MybatisTest {
  private static SqlSessionFactory sqlSessionFactory;



  @Test
  public void test7() throws Exception {
    InputStream resource = Resources.getResourceAsStream(com.zy.client.test.MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    Configuration configuration = sqlSessionFactory.getConfiguration();
    // 手动注册mapper
    //configuration.addMapper(UserMapper.class);
    configuration.setProxyFactory(new MyLoggingProxyFactory());
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
//    List<User> users = mapper.listUserOrderInfo(new User(1L, "10001"));
//    System.out.println(users);
  }
}
