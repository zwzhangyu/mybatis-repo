package com.mw.mini.mybatis.session;

/**
 * 工厂模式接口，构建SqlSession的工厂
 *
 * @author zhangyu
 */
public interface SqlSessionFactory {

    /**
     * 打开一个 session
     *
     * @return SqlSession
     */
    SqlSession openSession();

}
