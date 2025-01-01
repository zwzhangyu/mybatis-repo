package com.mw.mini.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
 *
 * @author zhangyu
 */
public interface Transaction {

    /**
     * 获取该事务对应的数据库连接
     *
     * @return DataBase connection
     * @throws SQLException the SQL exception
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     *
     * @throws SQLException the SQL exception
     */
    void commit() throws SQLException;

    /**
     * 回滚事务
     *
     * @throws SQLException the SQL exception
     */
    void rollback() throws SQLException;

    /**
     * 关闭对应的数据库连接
     *
     * @throws SQLException the SQL exception
     */
    void close() throws SQLException;

    /**
     * 读取设置的事务超时时间
     *
     * @return the timeout
     * @throws SQLException the SQL exception
     */
    Integer getTimeout() throws SQLException;

}
