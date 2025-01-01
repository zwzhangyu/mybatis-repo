package com.mw.mini.mybatis.scripting;

import com.mw.mini.mybatis.executor.parameter.ParameterHandler;
import com.mw.mini.mybatis.mapping.BoundSql;
import com.mw.mini.mybatis.mapping.MappedStatement;
import com.mw.mini.mybatis.mapping.SqlSource;
import com.mw.mini.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * @author zhangyu
 * @description 脚本语言驱动
 * 
 * 
 */
public interface LanguageDriver {

    /**
     * 创建SQL源码(mapper xml方式)
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建SQL源码(annotation 注解方式)
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

    /**
     * 创建参数处理器
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

}
