package com.mw.mini.mybatis.session;

import java.util.List;

/**
 * 主要用于执行 SQL 语句、提交事务、获取映射器等操作
 *
 * @author zhangyu
 * @date 2024/12/26
 */
public interface SqlSession {

    /**
     * 执行单条查询语句，并返回唯一结果。
     * 如果查询结果有多个或没有，都会抛出异常。
     *
     * @param statement 映射语句的标识符
     * @param <T>       返回值类型
     * @return 单个查询结果
     */
    <T> T selectOne(String statement);

    /**
     * 执行单条查询语句，传入查询参数，并返回唯一结果。
     * 如果查询结果有多个或没有，都会抛出异常。
     *
     * @param statement 映射语句的标识符
     * @param parameter 查询参数
     * @param <T>       返回值类型
     * @return 单个查询结果
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 执行查询语句，返回一个包含多个结果的列表。
     *
     * @param statement 映射语句的标识符
     * @param parameter 查询参数
     * @param <E>       列表中元素的类型
     * @return 查询结果的列表
     */
    <E> List<E> selectList(String statement, Object parameter);

    /**
     * 执行插入操作，通常返回插入成功的记录数。
     *
     * @param statement 映射语句的标识符
     * @param parameter 插入数据的参数对象
     * @return 影响的记录数
     */
    int insert(String statement, Object parameter);

    /**
     * 执行更新操作，通常返回更新成功的记录数。
     *
     * @param statement 映射语句的标识符
     * @param parameter 更新数据的参数对象
     * @return 影响的记录数
     */
    int update(String statement, Object parameter);

    /**
     * 执行删除操作，通常返回删除成功的记录数。
     *
     * @param statement 映射语句的标识符
     * @param parameter 删除数据的参数对象
     * @return 影响的记录数
     */
    Object delete(String statement, Object parameter);

    /**
     * 提交当前事务，只有在事务模式下才需要显式调用此方法。
     * 提交事务后，所有已执行的数据库操作将生效。
     */
    void commit();

    /**
     * 关闭当前会话，释放相关资源。
     */
    void close();

    /**
     * 清除当前会话中的缓存。调用此方法后，本会话中的缓存将被清空。
     */
    void clearCache();

    /**
     * 获取与当前会话相关联的配置对象。
     *
     * @return 当前会话的配置对象
     */
    Configuration getConfiguration();

    /**
     * 获取指定类型的映射器（Mapper）。映射器是用于执行数据库操作的接口，通常由 MyBatis 自动生成。
     *
     * @param type 映射器接口的类型
     * @param <T>  映射器的类型
     * @return 映射器实例
     */
    <T> T getMapper(Class<T> type);
}
