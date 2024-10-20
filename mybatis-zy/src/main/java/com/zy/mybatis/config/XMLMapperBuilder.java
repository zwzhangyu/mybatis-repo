package com.zy.mybatis.config;

import com.zy.mybatis.pojo.Configuration;
import com.zy.mybatis.pojo.MappedStatement;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description : 解析mapper.xml配置文件
 * History   :
 *******************************************************/
public class XMLMapperBuilder {
    private Configuration configuration;

    XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }


    public void  parse(InputStream mapperInputStream) throws DocumentException, ClassNotFoundException {
        // <select id="selectBlog" resultType="org.mybatis.example.Blog" parameterType="int">
        MappedStatement mappedStatement = new MappedStatement();
        Document document = new SAXReader().read(mapperInputStream);
        Element rootElement = document.getRootElement();
        List<Element> nodeList = rootElement.selectNodes("//select");
        String namespace = rootElement.attributeValue("namespace");
        for (Element element : nodeList) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String parameterType = element.attributeValue("parameterType");
            String sqlText = element.getTextTrim();
            mappedStatement.setStatementId(namespace + "." + id);
            mappedStatement.setSql(sqlText);
            mappedStatement.setSqlCommandType("select");
            mappedStatement.setResultMap(resultType);
            mappedStatement.setParameterType(parameterType);
            configuration.getMappedStatementMap().put(mappedStatement.getStatementId(),mappedStatement);
        }
    }
}
