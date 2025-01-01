package com.mw.mini.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.mw.mini.mybatis.builder.annotation.MapperAnnotationBuilder;
import com.mw.mini.mybatis.session.Configuration;
import com.mw.mini.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyu
 * @description 映射器注册机，管理 MyBatis 的所有 Mapper 注册、查找和创建操作
 */
public class MapperRegistry {

    /**
     * MyBatis 配置对象
     */
    private final Configuration config;

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    /**
     * 将已添加的映射器代理加入到 HashMap
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * 根据 Mapper 类型和 SqlSession 获取 Mapper 的代理实例
     *
     * @param type Mapper 接口类型
     * @param sqlSession SqlSession 实例
     * @param <T> Mapper 接口类型
     * @return 返回对应的 Mapper 代理实例
     * @throws RuntimeException 如果没有找到对应的 Mapper 或获取代理实例时出错
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    /**
     * 注册新的 Mapper 接口
     *
     * @param type Mapper 接口类型
     * @param <T> Mapper 接口类型
     * @throws RuntimeException 如果该类型已经注册过，则抛出异常
     */
    public <T> void addMapper(Class<T> type) {
        /* Mapper 必须是接口才会注册 */
        if (type.isInterface()) {
            if (hasMapper(type)) {
                // 如果重复添加了，报错
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));

            // 解析注解类语句配置
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            parser.parse();
        }
    }

    /**
     * 检查指定的 Mapper 接口是否已注册
     *
     * @param type Mapper 接口类型
     * @param <T> Mapper 接口类型
     * @return 如果已经注册返回 true，否则返回 false
     */
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    /**
     * 批量注册指定包名下的所有 Mapper 接口
     *
     * @param packageName 包名
     */
    public void addMappers(String packageName) {
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

}
