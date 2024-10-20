package com.zy.mybatis.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zy.mybatis.io.Resources;
import com.zy.mybatis.pojo.Configuration;
import com.zy.mybatis.pojo.MappedStatement;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public class XMLConfigBuilder {


    /**
     * 解析mybatis-config配置文件
     */
    public Configuration parse(InputStream inputStream) throws DocumentException, ClassNotFoundException {
        Configuration configuration = new Configuration();
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();
        List<Element> nodeList = rootElement.selectNodes("//property");
        Properties properties = new Properties();
        for (Element node : nodeList) {
            String name = node.attributeValue("name");
            String value = node.attributeValue("value");
            properties.setProperty(name, value);
        }

        // 数据库连接池
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(properties.getProperty("driver"));
        druidDataSource.setUrl(properties.getProperty("url"));
        druidDataSource.setUsername(properties.getProperty("username"));
        druidDataSource.setPassword(properties.getProperty("password"));

        configuration.setDataSource(druidDataSource);
        // 解析映射配置文件
        Map<String, MappedStatement> mappedStatementMap = new HashMap<>();
        configuration.setMappedStatementMap(mappedStatementMap);
        List<Element> resourceEleList = rootElement.selectNodes("//mapper");
        for (Element element : resourceEleList) {
            String resource = element.attributeValue("resource");
            // 读取mapper.xml文件
            InputStream mapperInputStream = Resources.getResource(resource);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration);
            xmlMapperBuilder.parse(mapperInputStream);
        }
        return configuration;
    }
}
