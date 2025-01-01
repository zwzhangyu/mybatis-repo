package com.mw.mini.mybatis.binding;


import com.mw.mini.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于创建 MapperProxy 的实例，生成 Mapper 接口的动态代理对象
 *
 * @author zhangyu
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface; // Mapper 接口的 Class 对象

    /**
     * 缓存接口方法与 MapperMethod 的映射关系
     */
    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    /**
     * 创建 MapperProxy 的实例，生成指定 Mapper 接口的动态代理对象
     *
     * @param sqlSession
     * @return
     */
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

}
