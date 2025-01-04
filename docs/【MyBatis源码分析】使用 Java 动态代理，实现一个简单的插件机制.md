> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)



# 设计分析
使用 动态代理 实现类似 MyBatis 插件机制的功能，能更灵活地拦截和增强方法调用。这种方式允许你不直接修改原有类的代码，而是通过代理类在方法调用前后加入自定义逻辑。下面给出一个简单的示例，从0到1实现类似 MyBatis 插件机制的例子，重点在于使用 Java 动态代理。
# 设计思路
【1】插件接口：插件的核心是一个接口，用于定义插件的方法和属性配置。
【2】目标对象：这是你要增强的对象，插件通过代理目标对象来实现拦截。
【3】动态代理：使用 Java 的动态代理机制拦截目标方法的调用，动态地插入额外的逻辑。
【4】插件管理：管理插件的注册和配置，使插件能够在目标方法执行时执行。
# 代码开发
首先定义一个插件接口，插件实现该接口以提供增强逻辑。

```java
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
```
目标对象通常是一些具体的业务类，在MyBatis中代理的对象是ParameterHandler/ResultSetHandler、StatementHandler或者Executor，通过插件来增强其功能

```java
/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description :  业务接口类
 * History   :
 *******************************************************/
public interface BizService {

    void doSomething(String param);
}
```
业务实现类：


```java
/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description :
 * History   :
 *******************************************************/
public class MyService implements BizService {
    @Override
    public void doSomething(String param) {
        System.out.println("Executing doSomething : " + param);
    }
}
```
我们将使用 Java 的 动态代理 来实现插件机制。在执行目标方法时，插件会在目标方法执行前和执行后插入增强逻辑。

```java
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

```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/1eb62af057064c068448d37e7b521fb6.png)
InvocationHandler 接口定义了代理对象的方法调用逻辑，invoke 方法会在目标方法执行前后插入插件逻辑。
在 invoke 方法中，在执行目标方法前调用所有插件的 before 方法，在执行后调用 after 方法。
通过 Proxy.newProxyInstance 创建一个代理对象，将目标对象和插件关联。

插件实现类，实现具体的插件类，定义插件的 before 和 after 行为。

```java

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
```


```java

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

```
# 流程测试

```java
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
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/c82caf7a91544003b2d77a92a8dd3539.png)
组合目标对象和插件，通过 PluginManager 创建代理对象，并执行增强后的业务逻辑。

# 总结
插件类型分类：可以根据需要设计不同类型的插件，例如 前置插件、后置插件、异常插件 等，分别控制在不同阶段执行插件。

更多动态代理功能：可以在 InvocationHandler 中进一步控制插件的执行顺序，或者加入更多复杂的逻辑。

插件优先级：可以为插件添加优先级字段，决定插件执行的顺序，类似 MyBatis 中的插件顺序。

通过使用 Java 动态代理，我们可以实现一个灵活的插件机制，允许在运行时动态地拦截和增强目标对象的方法。这个机制非常适合用于类似 MyBatis 的插件系统，可以方便地在目标方法执行前后加入自定义逻辑，而无需修改目标对象的代码。通过这种方式，可以轻松扩展系统功能，增强代码的可维护性和灵活性。

# 完整代码
[github](https://github.com/zwzhangyu/mybatis-repo/tree/main/mybatis-client/src/main/java/com/zy/client/plugin)