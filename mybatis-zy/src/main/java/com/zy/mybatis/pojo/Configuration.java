package com.zy.mybatis.pojo;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/10/10
 * Description : 持久层框架mybatis-config.xml映射对象
 * History   :
 *******************************************************/
public class Configuration {

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * key: sqlId value:MappedStatement对象
     */
    private Map<String, MappedStatement> mappedStatementMap = new HashMap<>();

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, MappedStatement> getMappedStatementMap() {
        return mappedStatementMap;
    }

    public void setMappedStatementMap(Map<String, MappedStatement> mappedStatementMap) {
        this.mappedStatementMap = mappedStatementMap;
    }
}
