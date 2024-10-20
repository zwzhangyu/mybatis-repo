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
    private String statementId;

    private String sql;

    /**
     * 返回类型
     */
    private String resultMap;

    /**
     * 执行SQL类型（update/select/insert/delete）
     */
    private String sqlCommandType;


    /**
     * 参数类型
     */
    private String parameterType;

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getStatementId() {
        return statementId;
    }

    public String getResultMap() {
        return resultMap;
    }

    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(String sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
}
