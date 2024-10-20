package com.zy.mybatis.executor;

import com.zy.mybatis.config.BoundSql;
import com.zy.mybatis.pojo.Configuration;
import com.zy.mybatis.pojo.MappedStatement;
import com.zy.mybatis.utils.GenericTokenParser;
import com.zy.mybatis.utils.ParameterMapping;
import com.zy.mybatis.utils.ParameterMappingTokenHandler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public class SimpleExecutor implements Executor {

    private Connection connection;

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object param) throws Exception {
        // 获取数据库连接
        connection = configuration.getDataSource().getConnection();
        // 获取执行sql
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);
        // 预编译SQL
        preparedStatement = connection.prepareStatement(boundSql.getFinalSql());
        // 获取参数对象类型和反射
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterClass = Class.forName(parameterType);
        List<ParameterMapping> parameterMappingList = boundSql.getList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            // content存储的是参数的key名称
            String content = parameterMapping.getContent();
            Field declaredField = parameterClass.getDeclaredField(content);
            declaredField.setAccessible(true);
            Object contentValue = declaredField.get(param);
            // 赋值占位符参数 preparedStatement.setString();
            preparedStatement.setObject(i + 1, contentValue);
        }
        // 执行SQL
        resultSet = preparedStatement.executeQuery();
        // 处理结果集
        ArrayList<E> resultList = new ArrayList<>();
        while (resultSet.next()) {
            // 返回值类型
            String resultMap = mappedStatement.getResultMap();
            Class<?> resultClass = Class.forName(resultMap);
            Object newInstance = resultClass.newInstance();
            // 元数据信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i < metaData.getColumnCount(); i++) {
                // 数据库表字段名
                String columnName = metaData.getColumnName(i);
                // 字段的值
                Object object = resultSet.getObject(columnName);
                // 属性描述器，获取某个属性的读写方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                // 实例对象；要赋值的值参数
                writeMethod.invoke(newInstance, object);
            }
            resultList.add((E) newInstance);
        }
        return resultList;
    }

    /**
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        // 创建标记处理器，完成标记占位符的解析
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler();
        // 创建标记解析器
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        // #{}占位符替换为jdbc的？
        String finalSql = parser.parse(sql);
        return new BoundSql(finalSql, handler.getParameterMappings());
    }

    @Override
    public void close() {
        // 释放资源
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
