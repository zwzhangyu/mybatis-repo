package com.zy.client.plugin.impl;

import com.zy.client.plugin.IPlugin;

import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description : 日志插件
 * History   :
 *******************************************************/
public class LogPlugin implements IPlugin {

    private String logPrefix;

    @Override
    public void before() {
        System.out.println(logPrefix + " Before method execution...");
    }

    @Override
    public void after() {
        System.out.println(logPrefix + " After method execution...");
    }

    @Override
    public void setProperties(Properties properties) {
        this.logPrefix = properties.getProperty("logPrefix", "LOG:");
    }
}