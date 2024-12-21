package com.zy.client.plugin;

import com.zy.client.plugin.impl.LogPlugin;
import com.zy.client.plugin.impl.MyService;
import com.zy.client.plugin.impl.PerformancePlugin;

import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description : 测试类
 * History   :
 *******************************************************/
public class Main {

    public static void main(String[] args) {
        // 创建插件管理器
        PluginManager pluginManager = new PluginManager();

        // 创建并配置插件
        IPlugin logPlugin = new LogPlugin();
        Properties logProperties = new Properties();
        logProperties.setProperty("logPrefix", "INFO:");
        pluginManager.configurePlugin(logPlugin, logProperties);

        IPlugin performancePlugin = new PerformancePlugin();
        Properties performanceProperties = new Properties();
        performanceProperties.setProperty("threshold", "150");
        pluginManager.configurePlugin(performancePlugin, performanceProperties);
        // 注册插件
        pluginManager.registerPlugin(logPlugin);
        pluginManager.registerPlugin(performancePlugin);

        // 创建目标对象
        BizService myService = new MyService();

        // 创建代理对象，扩展原有对象功能
        BizService proxyService = (BizService) pluginManager.createProxy(myService);

        // 调用代理对象的业务方法，插件会自动插入增强逻辑
        proxyService.doSomething("Test Param");
    }

}
