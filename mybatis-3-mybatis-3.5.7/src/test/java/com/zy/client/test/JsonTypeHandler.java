package com.zy.client.test;

/*******************************************************
 * Created by ZhangYu on 2024/11/12
 * Description :
 * History   :
 *******************************************************/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.client.bean.MyObject;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class JsonTypeHandler extends BaseTypeHandler<MyObject> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, MyObject parameter, JdbcType jdbcType) throws SQLException {
    try {
      // 将 Java 对象序列化为 JSON 字符串
      String json = objectMapper.writeValueAsString(parameter);
      ps.setString(i, json);
    } catch (JsonProcessingException e) {
      throw new SQLException("Failed to convert  JSON string.", e);
    }
  }

  @Override
  public MyObject getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    return parseJson(json);
  }

  @Override
  public MyObject getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    return parseJson(json);
  }

  @Override
  public MyObject getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    return parseJson(json);
  }

  private MyObject parseJson(String json) throws SQLException {
    try {
      if (json != null && !json.isEmpty()) {
        return objectMapper.readValue(json, MyObject.class);
      }
    } catch (JsonProcessingException e) {
      throw new SQLException("Failed to convert JSON string to MyObject.", e);
    }
    return null;
  }
}
