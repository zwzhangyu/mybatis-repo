package com.zy.mybatis.config;

import com.zy.mybatis.utils.ParameterMapping;

import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/10/14
 * Description :
 * History   :
 *******************************************************/
public class BoundSql {

    private String finalSql;


    private List<ParameterMapping> list;

    public BoundSql(String finalSql, List<ParameterMapping> list) {
        this.finalSql = finalSql;
        this.list = list;
    }

    public String getFinalSql() {
        return finalSql;
    }

    public void setFinalSql(String finalSql) {
        this.finalSql = finalSql;
    }

    public List<ParameterMapping> getList() {
        return list;
    }

    public void setList(List<ParameterMapping> list) {
        this.list = list;
    }
}
