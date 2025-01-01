package com.mw.mini.mybatis.binding;

import com.mw.mini.mybatis.mapping.MappedStatement;
import com.mw.mini.mybatis.mapping.SqlCommandType;
import com.mw.mini.mybatis.session.Configuration;

import java.lang.reflect.Method;

/*******************************************************
 * Description : SqlCommand 类用于封装 SQL 命令的信息，解决了通过映射器接口和方法来获取 SQL 命令的名称和类型。
 *******************************************************/
public class SqlCommand {

    /**
     * SQL 命令的名称，通常是映射器接口和方法的组合名称
     */
    private final String name;

    /**
     * SQL 命令的类型，例如 SELECT、INSERT、UPDATE、DELETE 等
     */
    private final SqlCommandType type;

    /**
     * 构造方法，初始化 SqlCommand 对象
     *
     * @param configuration   MyBatis 配置对象，用于获取 MappedStatement
     * @param mapperInterface Mapper 接口类
     * @param method          映射器方法
     */
    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
        // 通过映射器接口和方法获取 SQL 语句的 ID
        String statementName = mapperInterface.getName() + "." + method.getName();
        // 获取对应的 MappedStatement
        MappedStatement ms = configuration.getMappedStatement(statementName);
        // 初始化命令名称和类型
        name = ms.getId();
        type = ms.getSqlCommandType();
    }

    /**
     * 获取 SQL 命令的名称
     *
     * @return 返回 SQL 命令的名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取 SQL 命令的类型
     *
     * @return 返回 SQL 命令的类型，如 SELECT、INSERT 等
     */
    public SqlCommandType getType() {
        return type;
    }
}
