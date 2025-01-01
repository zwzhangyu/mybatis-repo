package com.mw.mini.mybatis.builder.annotation;

import com.mw.mini.mybatis.annotations.Delete;
import com.mw.mini.mybatis.annotations.Insert;
import com.mw.mini.mybatis.annotations.Select;
import com.mw.mini.mybatis.annotations.Update;
import com.mw.mini.mybatis.binding.MapperMethod;
import com.mw.mini.mybatis.builder.MapperBuilderAssistant;
import com.mw.mini.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.mw.mini.mybatis.executor.keygen.KeyGenerator;
import com.mw.mini.mybatis.executor.keygen.NoKeyGenerator;
import com.mw.mini.mybatis.mapping.SqlCommandType;
import com.mw.mini.mybatis.mapping.SqlSource;
import com.mw.mini.mybatis.scripting.LanguageDriver;
import com.mw.mini.mybatis.session.Configuration;
import com.mw.mini.mybatis.session.ResultHandler;
import com.mw.mini.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 解析映射器接口中的注解，生成 MyBatis 映射的 SQL 语句，并将它们注册到 MappedStatement 中，
 * 解决了注解驱动 SQL 语句的解析和映射问题
 *
 * @author zhangyu
 */
public class MapperAnnotationBuilder {


    /**
     * 存储支持的 SQL 注解类型，如 @Select, @Insert, @Update, @Delete
     */
    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    /**
     * MyBatis 配置对象
     */
    private Configuration configuration;

    /**
     * 映射器助手，辅助映射器的创建与注册
     */
    private MapperBuilderAssistant assistant;

    /**
     * 映射器接口类型
     */
    private Class<?> type;


    /**
     * 构造方法，初始化 MapperAnnotationBuilder
     *
     * @param configuration MyBatis 配置对象
     * @param type          映射器接口类型
     */
    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace(".", "/") + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
        // 支持的 SQL 注解类型
        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    /**
     * 解析映射器接口中的所有方法，生成对应的 SQL 映射语句
     */
    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            assistant.setCurrentNamespace(type.getName());

            Method[] methods = type.getMethods();
            for (Method method : methods) {
                if (!method.isBridge()) {
                    // 解析每个方法的 SQL 语句
                    parseStatement(method);
                }
            }
        }
    }

    /**
     * 解析方法对应的 SQL 语句，生成 MappedStatement
     *
     * @param method 映射器接口的方法
     */
    private void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getLanguageDriver(method);
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);

        if (sqlSource != null) {
            final String mappedStatementId = type.getName() + "." + method.getName();
            SqlCommandType sqlCommandType = getSqlCommandType(method);

            // 判断是否需要生成主键生成器
            KeyGenerator keyGenerator;
            String keyProperty = "id";
            if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
                keyGenerator = configuration.isUseGeneratedKeys() ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            } else {
                keyGenerator = new NoKeyGenerator();
            }
            // 处理 SELECT 类型的 SQL 命令，生成对应的 ResultMap
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if (isSelect) {
                resultMapId = parseResultMap(method);
            }

            // 调用助手类注册 MappedStatement
            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    sqlCommandType,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    false,
                    false,
                    keyGenerator,
                    keyProperty,
                    languageDriver
            );
        }
    }

    /**
     * 获取方法返回类型（特别是集合类型）
     *
     * @param method 映射器接口的方法
     * @return 方法的返回类型
     */

    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            Type returnTypeParameter = method.getGenericReturnType();
            if (returnTypeParameter instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }
        return returnType;
    }

    /**
     * 解析方法的 ResultMap 配置
     *
     * @param method 映射器接口的方法
     * @return 生成的 ResultMap 的 ID
     */
    private String parseResultMap(Method method) {
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
        return resultMapId;
    }

    /**
     * 获取方法的 SQL 命令类型（如 SELECT、INSERT、UPDATE、DELETE）
     *
     * @param method 映射器接口的方法
     * @return SQL 命令类型
     */
    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if (type == null) {
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    /**
     * 从方法的 SQL 注解中提取 SQL 语句，并构建 SqlSource 对象
     *
     * @param method         映射器接口中的方法
     * @param parameterType  方法的参数类型
     * @param languageDriver 语言驱动，用于处理 SQL 语句
     * @return 构建的 SqlSource 对象，若没有 SQL 注解则返回 null
     * @throws RuntimeException 如果反射过程中出现异常，则抛出运行时异常
     */
    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            if (sqlAnnotationType != null) {
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
        }
    }

    /**
     * 根据 SQL 字符串数组拼接成完整的 SQL 语句，并通过语言驱动创建 SqlSource 对象
     *
     * @param strings            SQL 语句的字符串数组，通常来源于注解的 value 属性
     * @param parameterTypeClass 方法的参数类型，用于处理 SQL 参数
     * @param languageDriver     语言驱动，用于创建 SqlSource 对象
     * @return 创建的 SqlSource 对象，封装了拼接后的 SQL 语句
     */
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
    }

    /**
     * 获取方法上使用的 SQL 注解类型
     *
     * @param method 映射器接口的方法
     * @return SQL 注解类型
     */
    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        for (Class<? extends Annotation> type : sqlAnnotationTypes) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) return type;
        }
        return null;
    }

    /**
     * 获取方法对应的语言驱动
     *
     * @param method 映射器接口的方法
     * @return 语言驱动
     */
    private LanguageDriver getLanguageDriver(Method method) {
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    /**
     * 获取方法参数类型
     *
     * @param method 方法对象
     * @return 方法参数类型
     */
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)) {
                if (parameterType == null) {
                    parameterType = clazz;
                } else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

}
