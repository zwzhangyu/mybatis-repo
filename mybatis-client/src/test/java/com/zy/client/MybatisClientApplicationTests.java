package com.zy.client;

import com.zy.client.bean.UserBean;
import com.zy.client.mapper.UserMapper;
import com.zy.mybatis.sqlSession.SqlSession;
import com.zy.mybatis.sqlSession.SqlSessionFactory;
import com.zy.mybatis.sqlSession.SqlSessionFactoryBuilder;
import com.zy.mybatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class MybatisClientApplicationTests {


    @Test
    public void test1() throws Exception {
        InputStream resource = Resources.getResource("mybatis-config.xml");

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserBean userBean = new UserBean();
        userBean.setId(1);
        Object objects1 = sqlSession.selectOne("user.selectById", userBean);
        System.out.println(objects1);

    }


    @Test
    public void testMapper() throws Exception {
        InputStream resource = Resources.getResource("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserBean userBean = new UserBean();
        userBean.setId(1);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        UserBean userBeanResult = userMapper.selectById(userBean);
        System.out.println(userBeanResult);

    }


}
