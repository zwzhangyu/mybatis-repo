package com.mw.mini.mybatis.builder;

import com.mw.mini.mybatis.session.Configuration;
import com.mw.mini.mybatis.type.TypeAliasRegistry;
import com.mw.mini.mybatis.type.TypeHandler;
import com.mw.mini.mybatis.type.TypeHandlerRegistry;

/**
 * BaseBuilder 是 MyBatis 中的一个抽象基类，它提供了几个常见的工具方法，用于解析配置中的类型、类型别名、类型处理器等。
 * 它的主要职责是为 MyBatis 的配置构建过程提供公共功能。
 * 其他具体的构建器（如 MapperBuilderAssistant、XmlConfigBuilder 等）可以继承这个类，
 * 并在其基础上完成特定的配置解析和注册。
 *
 * @author zhangyu
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    /**
     * 根据别名解析 Class 类型别名注册/事务管理器别名
     *
     * @param alias 别名
     * @return 类型
     */
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    /**
     * 根据给定的 Java 类型和类型处理器类型，解析并返回相应的类型处理器（TypeHandler）。
     * <p>
     * 如果提供的 `typeHandlerType` 为 `null`，则返回 `null`。否则，通过 `typeHandlerRegistry` 获取并返回
     * 与给定 `typeHandlerType` 类型对应的 `TypeHandler` 实例。
     * <p>
     * 类型处理器（TypeHandler）用于在 MyBatis 中进行 Java 类型与 JDBC 类型之间的转换。通过注册并解析
     * `typeHandlerType`，MyBatis 可以在执行 SQL 时自动处理类型转换。
     *
     * @param javaType        Java 类型，用于映射到数据库字段的类型。这个参数在本方法中未直接使用，
     *                        但可能会影响到 `typeHandlerType` 的解析。
     * @param typeHandlerType 类型处理器的具体实现类。用于将 Java 类型与数据库类型之间的转换。
     *                        如果为 `null`，则返回 `null`。
     * @return 返回与给定类型处理器类型（`typeHandlerType`）相对应的 `TypeHandler` 实例，
     * 如果 `typeHandlerType` 为 `null`，则返回 `null`。
     */
    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        return typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    }


    protected Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

}
