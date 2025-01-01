package com.mw.mini.mybatis.mapping;

import ch.qos.logback.core.sift.Discriminator;
import com.mw.mini.mybatis.session.Configuration;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/*******************************************************
 *   ZhangYu
 * Description : ResultMap.Builder建造者模式实例
 *******************************************************/
public class ResultMapTest {

    @Test
    public void test1(){
        // 创建 Configuration 对象，MyBatis 的配置类
        Configuration configuration = new Configuration();
        // 使用 Builder 模式构建 ResultMap
        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, "resultMapId", Object.class, new ArrayList<>());

    }

}