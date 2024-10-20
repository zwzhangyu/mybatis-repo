package com.zy.mybatis.utils;

import com.zy.mybatis.pojo.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*******************************************************
 * Created by ZhangYu on 2024/10/14
 * Description :
 * History   :
 *******************************************************/
public class ParameterMappingTokenHandler implements TokenHandler {

    private final List<ParameterMapping> parameterMappings = new ArrayList<>();


    @Override
    public String handleToken(String content) {
        parameterMappings.add(buildParameterMapping(content));
        return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
        ParameterMapping parameterMapping = new ParameterMapping(content);
        return parameterMapping;
    }

    public ParameterMappingTokenHandler() {
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }
}
