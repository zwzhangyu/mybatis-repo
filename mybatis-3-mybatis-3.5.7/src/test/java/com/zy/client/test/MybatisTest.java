package com.zy.client.test;

import com.zy.client.bean.UserBean;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.h2.util.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;

/*******************************************************
 * Created by ZhangYu on 2024/10/23
 * Description :
 * History   :
 *******************************************************/
public class MybatisTest {


  @Test
  public void test4() throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      System.out.println(url);
    }
  }


  @Test
  public void test2() throws Exception {
    InputStream resource1 = this.getClass().getClassLoader().getResourceAsStream("mybatis-config.xml");
    System.out.println(resource1);
    InputStream resource2 = this.getClass().getClassLoader().getResourceAsStream("/mybatis-config.xml");
    System.out.println(resource2);

    URL resource = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
    System.out.println(resource);
  }


  @Test
  public void test1() throws Exception {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserBean userBean = new UserBean();
    userBean.setId(1);
    Object objects1 = sqlSession.selectOne("user.selectById", userBean);
    System.out.println(objects1);
  }

  /**
   * 测试XMLConfigBuilder类完成Configuration对象的构建
   */
  @Test
  public void test5() throws Exception {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(resource);
    Configuration parse = xmlConfigBuilder.parse();
    System.out.println(parse);
  }
}
