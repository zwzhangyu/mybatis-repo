package com.zy.client.test;

/*******************************************************
 * Created by ZhangYu on 2024/11/10
 * Description :
 * History   :
 *******************************************************/
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.plugin.Plugin;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class MyLoggingProxyFactory implements ProxyFactory {

  @Override
  public Object createProxy(Object target,
                            ResultLoaderMap lazyLoader,
                            Configuration configuration,
                            ObjectFactory objectFactory,
                            List<Class<?>> constructorArgTypes,
                            List<Object> constructorArgs) {

    // 创建一个动态代理对象
    return Proxy.newProxyInstance(
      target.getClass().getClassLoader(),
      target.getClass().getClass().getInterfaces(),
      new LoggingInvocationHandler(target, lazyLoader)
    );
  }

  /**
   * 日志记录的 InvocationHandler 内部类
   */
  private static class LoggingInvocationHandler implements InvocationHandler {
    private final Object target;
    private final ResultLoaderMap lazyLoader;

    public LoggingInvocationHandler(Object target, ResultLoaderMap lazyLoader) {
      this.target = target;
      this.lazyLoader = lazyLoader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // 记录方法名称
      System.out.println("调用方法：" + method.getName());

      // 检查是否存在未加载的延迟加载属性
      if (lazyLoader != null && lazyLoader.size() > 0) {
        lazyLoader.loadAll(); // 在方法调用前加载所有懒加载属性
      }

      long startTime = System.currentTimeMillis();
      try {
        // 执行实际的方法调用
        Object result = method.invoke(target, args);
        long executionTime = System.currentTimeMillis() - startTime;
        System.out.println("方法执行时间：" + executionTime + " ms");
        return result;
      } catch (Exception e) {
        System.err.println("方法调用出错：" + e.getMessage());
        throw e;
      }
    }
  }
}
