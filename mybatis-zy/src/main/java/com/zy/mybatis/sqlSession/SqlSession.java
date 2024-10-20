package com.zy.mybatis.sqlSession;

import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public interface SqlSession {

    /**
     * 查询多个结果
     */
    <E> List<E> selectList(String statementId, Object param) throws Exception;

    /**
     * 查询单个结果
     */
    <T> T selectOne(String statementId, Object param) throws Exception;

    /**
     * 获取执行的Mapper
     */
    <T> T getMapper(Class<?> cls);

    /**
     * 清除资源
     */
    void close();

}
