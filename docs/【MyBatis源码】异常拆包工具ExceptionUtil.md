# 介绍
ExceptionUtil 是一个异常工具类，它提供一个拆包异常的工具方法unwrapThrowable。该方法将 InvocationTargetException 和UndeclaredThrowableException 这两类异常进行拆包，得到其中包含的真正的异常。

```java
/**
 * 异常拆包工具
 * @author Clinton Begin
 */
public class ExceptionUtil {

  private ExceptionUtil() {
    // Prevent Instantiation
  }

  /**
   * 将 InvocationTargetException 和UndeclaredThrowableException 这两类异常进行拆包，得到其中包含的真正的异常。
   */
  public static Throwable unwrapThrowable(Throwable wrapped) {
    Throwable unwrapped = wrapped;
    while (true) {
      if (unwrapped instanceof InvocationTargetException) {
        unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
      } else if (unwrapped instanceof UndeclaredThrowableException) {
        unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
      } else {
        return unwrapped;
      }
    }
  }
}
```
# 为什么这么写？
为什么需要给 InvocationTargetException和 UndeclaredThrowableException这两个类拆包？这两个类为什么要把其他异常包装起来？
InvocationTargetException为必检异常，UndeclaredThrowableException为免检的运行时异常。它们都不属于 MyBatis，而是来自 java.lang.reflect包。
反射操作中，代理类通过反射调用目标类的方法时，目标类的方法可能抛出异常。反射可以调用各种目标方法，因此目标方法抛出的异常是多种多样无法确定的。这意味着反射操作可能抛出一个任意类型的异常。可以用 Throwable 去接收这个异常，但这无疑太过宽泛。InvocationTargetException就是为解决这个问题而设计的，当反射操作的目标方法中出现异常时，都统一包装成一个必检异常 InvocationTargetException。InvocationTargetException内部的 target 属性则保存了原始的异常。这样一来，便使得反射操作中的异常更易管理。
# 代码示例

```java
public class ExceptionUtilExample {


  // 一个简单的示例类，包含一个抛出异常的方法
  public static class ExampleClass {

    public void throwException() throws Exception {
      throw new IllegalArgumentException("This is an illegal argument exception.");
    }
  }
```

```java
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
```
原始输出：
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/3cdbb139b0ba4a07900f7fb9eff557c8.png)
ExceptionUtil异常拆包抛出：
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/778e27f2147d43e0848c936fc370cf5f.png)
通过这个示例，可以看到如何使用 ExceptionUtil.unwrapThrowable() 来解包异常。无论是反射调用、动态代理还是其他可能导致异常包装的机制，unwrapThrowable 都能帮助你获取原始异常进行处理。