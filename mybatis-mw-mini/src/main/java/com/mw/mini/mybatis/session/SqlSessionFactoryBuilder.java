package com.mw.mini.mybatis.session;

import com.mw.mini.mybatis.builder.xml.XMLConfigBuilder;
import com.mw.mini.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.dom4j.Document;

import java.io.Reader;

/**
 * 构建SqlSessionFactory的工厂
 *
 * @author zhangyu
 **/
public class SqlSessionFactoryBuilder {

    /**
     * 使用传入的 Reader 对象（通常是配置文件的输入流）构建 SqlSessionFactory。
     * 该方法通过 XMLConfigBuilder 解析 XML 配置文件，生成 Configuration 配置对象，
     * 然后使用该配置对象创建 SqlSessionFactory 实例。
     *
     * @param reader 用于读取 XML 配置文件的 Reader 对象
     * @return 构建好的 SqlSessionFactory 实例
     */
    public SqlSessionFactory build(Reader reader) {
        // 创建一个 XMLConfigBuilder 对象，使用传入的 Reader 读取 XML 配置文件
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        // 解析配置文件，获取 Configuration 配置对象
        Configuration configuration = xmlConfigBuilder.parse();
        return build(configuration);
    }

    /**
     * 使用传入的 Document 对象（通常是已经解析的 XML 配置文件文档）构建 SqlSessionFactory。
     * 该方法通过 XMLConfigBuilder 解析 Document 对象，生成 Configuration 配置对象，
     * 然后使用该配置对象创建 SqlSessionFactory 实例。
     *
     * @param document 解析后的 XML 配置文件 Document 对象
     * @return 构建好的 SqlSessionFactory 实例
     */
    public SqlSessionFactory build(Document document) {
        // 创建一个 XMLConfigBuilder 对象，使用传入的 Document 解析 XML 配置文件
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(document);

        // 解析 Document 对象，获取 Configuration 配置对象
        return build(xmlConfigBuilder.parse());
    }

    /**
     * 使用传入的 Configuration 配置对象构建 SqlSessionFactory。
     * 该方法直接通过传入的 Configuration 创建 SqlSessionFactory。
     *
     * @param config MyBatis 的核心配置对象，包含所有配置信息
     * @return 构建好的 SqlSessionFactory 实例
     */
    public SqlSessionFactory build(Configuration config) {
        // 创建并返回一个 DefaultSqlSessionFactory 实例，该实例基于传入的 Configuration 配置对象
        return new DefaultSqlSessionFactory(config);
    }


}
