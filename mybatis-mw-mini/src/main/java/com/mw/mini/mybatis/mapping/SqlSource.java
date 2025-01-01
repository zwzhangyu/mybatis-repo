package com.mw.mini.mybatis.mapping;

/**
 * 用于封装 SQL 语句和相关参数的接口
 * BoundSql 封装了 SQL 语句和参数的映射信息
 *
 * @author zhangyu*
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
