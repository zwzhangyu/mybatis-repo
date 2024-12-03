package com.zy.client.datasource;

import com.zy.client.bean.UserBean;
import com.zy.client.test.MybatisTest;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/*******************************************************
 * Created by ZhangYu on 2024/12/2
 * Description :
 * History   :
 *******************************************************/
public class DataSourceTest {


  @Test
  public void  testHikariCPDataSourceFactory() throws IOException {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserBean userBean = new UserBean();
    userBean.setId(1);
    Object objects1 = sqlSession.selectOne("user.selectById", userBean);
    System.out.println(objects1);
  }

}
