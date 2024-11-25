package com.zy.client.plugin;

import com.zy.client.bean.UserBean;
import com.zy.client.test.MyLoggingProxyFactory;
import com.zy.client.test.MybatisTest;
import com.zy.client.test.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

/*******************************************************
 * Created by ZhangYu on 2024/11/13
 * Description : Mapper接口测试
 * History   :
 *******************************************************/
public class PluginTest {

  @Test
  public void test1() throws Exception {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    Configuration configuration = sqlSessionFactory.getConfiguration();
    // 手动注册mapper
    configuration.addMapper(UserMapper.class);
    configuration.setProxyFactory(new MyLoggingProxyFactory());
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    UserBean res = mapper.selectDataById(1);
    System.out.println(res);
  }
}
