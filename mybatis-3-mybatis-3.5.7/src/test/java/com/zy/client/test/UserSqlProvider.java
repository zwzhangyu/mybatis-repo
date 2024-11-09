package com.zy.client.test;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/11/9
 * Description :
 * History   :
 *******************************************************/
public class UserSqlProvider {

  public String buildSelectSql(Map<String, Object> params) {
    return new SQL() {{
      SELECT("*");
      FROM("t_user");
      if (params.get("name") != null) {
        WHERE("name = #{name}");
      }
      if (params.get("age") != null) {
        WHERE("age = #{age}");
      }
      ORDER_BY("id DESC");
    }}.toString();
  }
}
