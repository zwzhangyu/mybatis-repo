package com.mw.mini.mybatis.builder;

import com.mw.mini.mybatis.mapping.BoundSql;
import com.mw.mini.mybatis.mapping.ParameterMapping;
import com.mw.mini.mybatis.mapping.SqlSource;
import com.mw.mini.mybatis.session.Configuration;

import java.util.List;

/**
 * @author zhangyu
 * @description 静态SQL源码
 * 
 * 
 */
public class StaticSqlSource implements SqlSource {

    private String sql;

    private List<ParameterMapping> parameterMappings;

    private Configuration configuration;


    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
