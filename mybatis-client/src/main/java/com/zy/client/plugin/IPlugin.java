package com.zy.client.plugin;

import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description : 插件接口
 * History   :
 *******************************************************/
public interface IPlugin {

    void before();   // 插件在目标方法执行前执行

    void after();    // 插件在目标方法执行后执行

    void setProperties(Properties properties);  // 配置插件属性
}
