package com.zy.client.test;

import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/11/20
 * Description :SqlNode测试类
 * History   :
 *******************************************************/
public class SqlNodeTest {

  @Test
  public void test1() throws IOException {
    InputStream resource = Resources.getResourceAsStream(MybatisTest.class.getClassLoader(), "mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode("select * from t_user where 1=1 ");
    IfSqlNode ifSqlNodeId = new IfSqlNode(new StaticTextSqlNode("  and   id = #{id}"), "id != null");
    IfSqlNode ifSqlNodeName = new IfSqlNode(new StaticTextSqlNode("   and name = #{name}"), "name != null");
    MixedSqlNode mixedSqlNode = new MixedSqlNode(Arrays.asList(staticTextSqlNode, ifSqlNodeId, ifSqlNodeName));
    HashMap<String, Object> param = new HashMap<>(2);
    param.put("id", "1");
    param.put("name", null);
    DynamicContext dynamicContext = new DynamicContext(sqlSession.getConfiguration(), param);

    mixedSqlNode.apply(dynamicContext);
    System.out.println(dynamicContext.getSql());

  }


  @Test
  public void testOgnl() throws IOException {
    Map<String, Object> parameterObject = new HashMap<>();
    parameterObject.put("username", "");
    System.out.println(ExpressionEvaluator.evaluateBoolean("username != null and username != ''", parameterObject));
  }

  static class ExpressionEvaluator {

    public static boolean evaluateBoolean(String expression, Object parameterObject) {
      try {
        Object value = Ognl.getValue(parseExpression(expression), parameterObject);
        return value instanceof Boolean && (Boolean) value;
      } catch (OgnlException e) {
        throw new RuntimeException("Error evaluating expression '" + expression + "'", e);
      }
    }

    private static Object parseExpression(String expression) throws OgnlException {
      return Ognl.parseExpression(expression);
    }
  }
}
