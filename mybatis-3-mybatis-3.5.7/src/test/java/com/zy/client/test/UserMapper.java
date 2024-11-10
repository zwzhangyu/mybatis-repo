package com.zy.client.test;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/11/9
 * Description :
 * History   :
 *******************************************************/
@Mapper
public interface UserMapper {

  @SelectProvider(type = UserSqlProvider.class, method = "buildSelectSql")
  List<Map<String, Object>> selectUsers(Map<String, Object> params);


}
