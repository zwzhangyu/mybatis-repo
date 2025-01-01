package com.mw.mini.mybatis.transaction.jdbc;

import com.mw.mini.mybatis.session.TransactionIsolationLevel;
import com.mw.mini.mybatis.transaction.Transaction;
import com.mw.mini.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author zhangyu
 * @description JdbcTransaction 工厂
 * 
 * 
 */
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }

}
