package com.mw.mini.mybatis.session.defaults;

import com.mw.mini.mybatis.executor.Executor;
import com.mw.mini.mybatis.mapping.Environment;
import com.mw.mini.mybatis.session.Configuration;
import com.mw.mini.mybatis.session.SqlSession;
import com.mw.mini.mybatis.session.SqlSessionFactory;
import com.mw.mini.mybatis.session.TransactionIsolationLevel;
import com.mw.mini.mybatis.transaction.Transaction;
import com.mw.mini.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * @author zhangyu
 * @description 默认的 DefaultSqlSessionFactory
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            // 创建DefaultSqlSession
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            try {
                assert tx != null;
                tx.close();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }
}
