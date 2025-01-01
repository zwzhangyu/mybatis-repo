package com.mw.mini.mybatis.binding;


import com.mw.mini.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zhangyu
 * @description 用于动态代理 Mapper 接口的类
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -1424544343959729838L;

    private SqlSession sqlSession; // MyBatis 的会话对象，执行 SQL 操作

    private final Class<T> mapperInterface; // 对应的 Mapper 接口类型

    private final Map<Method, MapperMethod> methodCache; // 方法缓存

    /**
     * 通过传入 sqlSession、mapperInterface（Mapper 接口类型）、methodCache（方法缓存）来初始化 MapperProxy 对象
     * @param sqlSession
     * @param mapperInterface
     * @param methodCache
     */
    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final MapperMethod mapperMethod = cachedMapperMethod(method);
            return mapperMethod.execute(sqlSession, args);
        }
    }

    /**
     * 去缓存中找MapperMethod
     */
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            //找不到才去new
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }

}
