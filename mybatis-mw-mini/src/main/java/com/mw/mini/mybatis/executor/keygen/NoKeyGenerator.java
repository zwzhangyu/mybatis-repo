package com.mw.mini.mybatis.executor.keygen;

import com.mw.mini.mybatis.executor.Executor;
import com.mw.mini.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @author zhangyu
 * @description 不用键值生成器
 * 
 * 
 */
public class NoKeyGenerator implements KeyGenerator {

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

}
