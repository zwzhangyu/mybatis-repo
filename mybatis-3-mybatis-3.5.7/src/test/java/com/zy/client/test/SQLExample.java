package com.zy.client.test;

import org.apache.ibatis.jdbc.SQL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*******************************************************
 * Created by ZhangYu on 2024/11/9
 * Description : org.apache.ibatis.jdbc.SQL工具使用
 * History   :
 *******************************************************/
public class SQLExample {

  @Test
  public void testSelectSQL() {
    String orgSql = "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
      "FROM PERSON P, ACCOUNT A\n" +
      "INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID\n" +
      "INNER JOIN COMPANY C on D.COMPANY_ID = C.ID\n" +
      "WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) \n" +
      "OR (P.LAST_NAME like ?)\n" +
      "GROUP BY P.ID\n" +
      "HAVING (P.LAST_NAME like ?) \n" +
      "OR (P.FIRST_NAME like ?)\n" +
      "ORDER BY P.ID, P.FULL_NAME";

    String newSql = new SQL() {{
      SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
      SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
      FROM("PERSON P");
      FROM("ACCOUNT A");
      INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
      INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
      WHERE("P.ID = A.ID");
      WHERE("P.FIRST_NAME like ?");
      OR();
      WHERE("P.LAST_NAME like ?");
      GROUP_BY("P.ID");
      HAVING("P.LAST_NAME like ?");
      OR();
      HAVING("P.FIRST_NAME like ?");
      ORDER_BY("P.ID");
      ORDER_BY("P.FULL_NAME");
    }}.toString();
    assertEquals(orgSql, newSql);
  }


  @Test
  public void testSelectSQL2() {
    String newSql = new SQL() {{
      SELECT("name,mobile_no,age");
      FROM("t_user A");
      INNER_JOIN("student B on B.name = A.name");
      WHERE("B.name like ?");
      OR();
      WHERE("A.ID = ?");
      ORDER_BY("A.ID DESC");
    }}.toString();
    System.out.println(newSql);
  }

}
