package com.mw.mini.mybatis.session;

import com.alibaba.fastjson.JSON;
import com.mw.mini.mybatis.bean.UserBean;
import com.mw.mini.mybatis.io.Resources;
import org.junit.Test;

import java.io.Reader;

import static org.junit.Assert.*;

/*******************************************************
 * Created by ZhangYu on 2024/12/26
 * Description :
 *******************************************************/
public class SqlSessionTest {

    @Test
    public void selectOne() {
        String resource = "mybatis-config.xml";
        Reader reader;
        try {
            reader = Resources.getResourceAsReader(resource);
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);

            SqlSession session = sqlMapper.openSession();
            try {
                UserBean user = session.selectOne("com.mw.mini.mybatis.mapper.UserMapper.queryUserInfoById", 1L);
                System.out.println(JSON.toJSONString(user));
            } finally {
                session.close();
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}