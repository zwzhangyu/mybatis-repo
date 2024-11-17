package com.zy.client.test;

import com.zy.client.bean.UserBean;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/11/13
 * Description : Mapper接口测试
 * History   :
 *******************************************************/
public class MapperTest {

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
