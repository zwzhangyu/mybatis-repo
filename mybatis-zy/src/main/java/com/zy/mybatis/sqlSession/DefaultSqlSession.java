package com.zy.mybatis.sqlSession;

import com.zy.mybatis.executor.Executor;
import com.zy.mybatis.pojo.Configuration;
import com.zy.mybatis.pojo.MappedStatement;

import java.lang.reflect.*;
import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    /**
     * 查询多个结果
     *
     * @param statementId
     * @param param
     */
    @Override
    public <E> List<E> selectList(String statementId, Object param) throws Exception {
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<E> list = executor.query(configuration, mappedStatement, param);
        return list;
    }

    /**
     * 查询单个结果
     *
     * @param statementId
     * @param param
     */
    @Override
    public <T> T selectOne(String statementId, Object param) throws Exception {
        List<T> list = this.selectList(statementId, param);
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                return list.get(0);
            } else {
                throw new RuntimeException("返回结果过多");
            }
        }
        return null;
    }

    /**
     * 清除资源
     */
    @Override
    public void close() {
        executor.close();
    }

    /**
     * 获取执行的Mapper
     *
     * @param cls
     */
    @Override
    public <T> T getMapper(Class<?> cls) {

        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{cls}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();
                String statementId = className + "." + methodName;

                // 获取执行的xml和SQL信息
                MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
                String sqlCommandType = mappedStatement.getSqlCommandType();
                switch (sqlCommandType) {
                    case "select":
                        // 获取方法的返回类型，判断集合还是单个对象
                        Type genericReturnType = method.getGenericReturnType();
                        if (genericReturnType instanceof ParameterizedType) {
                            if (args != null) {
                                return selectList(statementId, args[0]);
                            } else {
                                return selectList(statementId, null);
                            }
                        } else {
                            return selectOne(statementId, args[0]);
                        }
                    case "delete":
                        break;
                    case "update":
                        break;
                    case "insert":
                        break;
                }
                return null;
            }
        });
        return (T) proxyInstance;
    }
}
