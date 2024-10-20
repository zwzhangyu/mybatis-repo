package com.zy.mybatis.sqlSession;

import com.zy.mybatis.executor.SimpleExecutor;
import com.zy.mybatis.pojo.Configuration;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public class DefaultSqlSessionFactory implements SqlSessionFactory {


    private Configuration configuration;


    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        // 创建执行器对象
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        // 生成SqlSession
        DefaultSqlSession defaultSqlSession = new DefaultSqlSession(configuration, simpleExecutor);
        return defaultSqlSession;
    }
}
