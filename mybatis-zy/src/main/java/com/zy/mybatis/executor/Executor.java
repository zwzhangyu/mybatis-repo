package com.zy.mybatis.executor;

import com.zy.mybatis.pojo.Configuration;
import com.zy.mybatis.pojo.MappedStatement;

import java.sql.SQLException;
import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/10/13
 * Description :
 * History   :
 *******************************************************/
public interface Executor {

    <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object param) throws Exception;

    void close();

}
