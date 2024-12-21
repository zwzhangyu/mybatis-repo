package com.zy.client.plugin.impl;

import com.zy.client.plugin.IPlugin;

import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description :  性能监控插件
 * History   :
 *******************************************************/
public class PerformancePlugin  implements IPlugin {
    private long threshold;

    @Override
    public void before() {
        System.out.println("PerformancePlugin: Monitoring start...");
    }

    @Override
    public void after() {
        System.out.println("PerformancePlugin: Monitoring end...");
    }

    @Override
    public void setProperties(Properties properties) {
        this.threshold = Long.parseLong(properties.getProperty("threshold", "100"));
    }
}
