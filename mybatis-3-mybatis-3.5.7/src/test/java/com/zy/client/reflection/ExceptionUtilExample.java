package com.zy.client.reflection;

/*******************************************************
 * Created by ZhangYu on 2024/11/30
 * Description :
 * History   :
 *******************************************************/


public class ExceptionUtilExample {


  // 一个简单的示例类，包含一个抛出异常的方法
  public static class ExampleClass {

    public void throwException() throws Exception {
      throw new IllegalArgumentException("This is an illegal argument exception.");
    }
  }
}
