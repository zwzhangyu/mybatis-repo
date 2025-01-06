在Java开发中，我们经常需要加载资源文件，如配置文件、图像等。Java提供了两种常用的方法来实现资源的加载：Class.getResource和ClassLoader.getResource。虽然这两者都能达到类似的目的，但在使用和实现上存在一些重要的区别。
## 1. 方法概述
• Class.getResource(String name): 这是一个实例方法，属于Class类。它是通过当前类所处路径来查找资源，并返回一个URL对象。
• ClassLoader.getResource(String name): 这是一个属于ClassLoader类的方法。它直接通过类加载器查找资源。由于它是类加载器的静态方法，所以通常是通过ClassLoader的实例或类加载器的类名来调用。
## 2. 资源查找路径

• ClassLoader并不关心当前类的包名路径，它永远以classpath为基点来定位资源。需要注意的是在用ClassLoader加载资源时，路径不要以"/"开头，所有以"/"开头的路径都返回null；

```bash
   InputStream resource1 = this.getClass().getClassLoader().getResourceAsStream("mybatis-config.xml");
    System.out.println(resource1);
    InputStream resource2 = this.getClass().getClassLoader().getResourceAsStream("/mybatis-config.xml");
    System.out.println(resource2);
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/3d19d5432d37467c946243a42125db9c.png)
Class.getResource如果资源名是绝对路径(以"/"开头)，那么会以classpath为基准路径去加载资源，如果不以"/"开头，那么以这个类的Class文件所在的路径为基准路径去加载资源。

## 3. 资源查找路径
【1】 通过Class对象获取 MyClass.class.getClassLoader();
【2】 可以通过当前线程的上下文类加载器获取ClassLoader，这在某些情况下（例如多线程环境）非常有用：
【3】 可以直接通过ClassLoader类的静态方法获取系统类加载器： ClassLoader.getSystemClassLoader();
【4】 如果你有一个自定义的类加载器，可以直接使用该类加载器的实例：

## 4.使用getResource方法的注意事项
避免使用 Class.getResource("/") 或 ClassLoader.getResource("")。你应该传入一个确切的资源名，然后对输出结果作计算。比如，如果你确实想获取当前类是从哪个类路径起点上执行的，以前面提到的test.App来说，可以调用 App.class.getResource(App.class.getSimpleName() + ".class")。如果所得结果不是 jar 协议的URL，说明 class 文件没有打包，将所得结果去除尾部的 "test/App.class"，即可获得 test.App 的类路径的起点；如果结果是 jar 协议的 URL，去除尾部的 "!/test/App.class"，和前面的 "jar:"，即是 test.App 所在的 jar 文件的url。

```java
    URL resource = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
    System.out.println(resource);
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/8e989152ec49491aaef5e6cad0e7e11d.png)

## 5.获取批量资源
使用classLoader的getResources方法可以获得批量资源


```java
 ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      System.out.println(url);
    }
```

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/88c4fd82b4554a80a09b9bce9d71cfb2.png)
