package com.zy.client.plugin;

import com.zy.client.plugin.IPlugin;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description : 插件管理器
 * History   :
 *******************************************************/
public class PluginManager {

    /**
     * 插件列表
     */
    private List<IPlugin> plugins = new ArrayList<>();

    /**
     * 注册插件
     */
    public void registerPlugin(IPlugin plugin) {
        plugins.add(plugin);
    }

    /**
     * 配置插件
     */
    public void configurePlugin(IPlugin plugin, Properties properties) {
        plugin.setProperties(properties);
    }

    /**
     * 创建代理对象
     */
    public Object createProxy(Object target){
        // 创建代理对象
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 执行所有插件的before操作
                for (IPlugin plugin : plugins) {
                    plugin.before();
                }
                // 调用目标方法
                Object result = method.invoke(target, args);

                // 执行所有插件的after方法
                for (IPlugin plugin : plugins) {
                    plugin.after();
                }
                return result;
            }
        };
        // 创建并返回代理对象
        return Proxy.newProxyInstance(target.getClass().getClassLoader()
                ,target.getClass().getInterfaces(),invocationHandler);
    }


}
