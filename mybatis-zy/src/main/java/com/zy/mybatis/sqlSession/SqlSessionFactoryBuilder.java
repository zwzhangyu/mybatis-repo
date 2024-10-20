package com.zy.mybatis.sqlSession;

import com.zy.mybatis.config.XMLConfigBuilder;
import com.zy.mybatis.pojo.Configuration;

import java.io.IOException;
import java.io.InputStream;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description : 读取 MyBatis 的配置文件或者配置信息，创建出一个 SqlSessionFactory 对象
 * History   :
 *******************************************************/
public class SqlSessionFactoryBuilder {


    public SqlSessionFactory build(InputStream inputStream) {
        try {
            // 创建 XML 配置解析器
            XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
            Configuration configuration = xmlConfigBuilder.parse(inputStream);

            // 创建SqlSessionFactory对象
            DefaultSqlSessionFactory defaultSqlSessionFactory = new DefaultSqlSessionFactory(configuration);
            return defaultSqlSessionFactory;
        } catch (Exception e) {
            throw new RuntimeException("Error building SqlSession.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // 忽略关闭流时的异常
            }
        }
    }


}
