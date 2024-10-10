package com.zy.mybatis.pojo;

/*******************************************************
 * Created by ZhangYu on 2024/10/10
 * Description : MappedStatement 用于存储映射的 SQL 语句和返回结果类型
 *  对应的是mapper.xml中的一条执行语句
 * History   :
 *******************************************************/
public class MappedStatement {

    /**
     * SQL标识，全路径标识
     */
    private String sqlId;

    private String sql;

    /**
     * 返回类型
     */
    private Class<?> resultMap;


    /**
     * 参数类型
     */
    private Class<?> parameterType;
}
