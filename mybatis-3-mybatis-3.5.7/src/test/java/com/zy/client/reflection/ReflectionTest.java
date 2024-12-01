package com.zy.client.reflection;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/*******************************************************
 * Created by ZhangYu on 2024/11/30
 * Description : reflection
 * History   :
 *******************************************************/
public class ReflectionTest {


  /**
   * 测试原始异常抛出
   */
  @Test
  public void testException() {
    try {
      // 创建一个实例并调用方法
      ExceptionUtilExample.ExampleClass example = new ExceptionUtilExample.ExampleClass();
      Method method = ExceptionUtilExample.ExampleClass.class.getMethod("throwException");
      // 使用反射调用方法，这会抛出一个 InvocationTargetException
      method.invoke(example);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * 测试ExceptionUtil异常拆包抛出
   */
  @Test
  public void testExceptionUtil() {
    try {
      // 创建一个实例并调用方法
      ExceptionUtilExample.ExampleClass example = new ExceptionUtilExample.ExampleClass();
      Method method = ExceptionUtilExample.ExampleClass.class.getMethod("throwException");
      // 使用反射调用方法，这会抛出一个 InvocationTargetException
      method.invoke(example);
    } catch (Throwable t) {
      Throwable throwable = ExceptionUtil.unwrapThrowable(t);
      throwable.printStackTrace();
    }
  }


}
