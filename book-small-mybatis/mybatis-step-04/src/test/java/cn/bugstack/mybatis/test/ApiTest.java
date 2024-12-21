package cn.bugstack.mybatis.test;

import cn.bugstack.mybatis.io.Resources;
import cn.bugstack.mybatis.mapping.MappedStatement;
import cn.bugstack.mybatis.mapping.SqlCommandType;
import cn.bugstack.mybatis.session.SqlSession;
import cn.bugstack.mybatis.session.SqlSessionFactory;
import cn.bugstack.mybatis.session.SqlSessionFactoryBuilder;
import cn.bugstack.mybatis.test.dao.IUserDao;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 小傅哥，微信：fustack
 * @description 单元测试
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        String res = userDao.queryUserInfoById("10001");
        logger.info("测试结果：{}", res);
    }


    @Test
    public void test_sqlParse() throws IOException, DocumentException {
        Reader reader = Resources.getResourceAsReader("mapper/User_Mapper.xml");
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new InputSource(reader));
        Element root = document.getRootElement();
        String namespace = root.attributeValue("namespace");
        List<Element> selectNodes = root.elements("select");
        for (Element node : selectNodes) {
            String id = node.attributeValue("id");
            String sql = node.getText();
            // 保存参数位置和参数名称
            Map<Integer, String> parameter = new HashMap<>();
            // 占位符 #{}
            Pattern pattern = Pattern.compile("(#\\{(.*?)})");
            Matcher matcher = pattern.matcher(sql);
            for (int i = 1; matcher.find(); i++) {
                String g1 = matcher.group(1);
                String g2 = matcher.group(2);
                parameter.put(i, g2);
                sql = sql.replace(g1, "?");
            }
            String msId = namespace + "." + id;
            logger.info("解析后的SQL：{}  参数信息：{}", sql, parameter);
        }
    }
}
