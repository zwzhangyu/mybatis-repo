package com.zy.client.plugin;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;

import java.util.Properties;

/*******************************************************
 * Created by ZhangYu on 2024/11/23
 * Description :
 * History   :
 *******************************************************/

@Intercepts({})
public class ExamplePlugin implements Interceptor {

  private Properties properties;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 执行自定义逻辑
    System.out.println("Before executing: " + properties);
    return invocation.proceed();
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
