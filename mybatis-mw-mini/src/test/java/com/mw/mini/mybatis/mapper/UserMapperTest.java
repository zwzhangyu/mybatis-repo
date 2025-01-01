package com.mw.mini.mybatis.mapper;

import com.alibaba.fastjson.JSON;
import com.mw.mini.mybatis.bean.UserBean;
import com.mw.mini.mybatis.io.Resources;
import com.mw.mini.mybatis.session.SqlSession;
import com.mw.mini.mybatis.session.SqlSessionFactory;
import com.mw.mini.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.Reader;
import java.util.List;

import static org.junit.Assert.*;

/*******************************************************
 * Created by ZhangYu on 2024/12/26
 * Description : UserMapper测试
 *******************************************************/
public class UserMapperTest {

    @Test
    public void queryUserInfoById() {
        String resource = "mybatis-config.xml";
        Reader reader;
        try {
            reader = Resources.getResourceAsReader(resource);
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
            SqlSession session = sqlMapper.openSession();
            UserMapper userMapper = session.getMapper(UserMapper.class);
            UserBean userBean = new UserBean();
            userBean.setId(1L);
//            UserBean userBeanResult = userMapper.queryUserInfoById(userBean);
//            System.out.println(userBeanResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 多参数查询测试
     */
    @Test
    public void queryUserInfo() {
        String resource = "mybatis-config.xml";
        Reader reader;
        try {
            reader = Resources.getResourceAsReader(resource);
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
            SqlSession session = sqlMapper.openSession();
            UserMapper userMapper = session.getMapper(UserMapper.class);
            List<UserBean> userBeanResult = userMapper.queryUserInfo("zhangsan",1L,"1880031231");
            System.out.println(userBeanResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}