# exceptions包
exceptions包为 MyBatis定义了绝大多数异常类的父类，同时也提供了异常类的生产工厂
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/33af01873cd0444ab73f1cd27e0eee47.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/c97ce7ee663542d98873d096266569e9.png)
MyBatis中异常类类图
# 分包设计
通过 MyBatis异常类的类图还可以看出，众多的异常类并没有放在 exceptions包中，而是散落在其他各个包中。这涉及项目规划时的分包问题。通常，在规划一个项目的包结构时，可以按照以下两种方式进行包的划分。
按照类型方式划分，例如将所有的接口类放入一个包，将所有的 Controller类放入一个包。这种分类方式从类型上看更为清晰，但是会将完成同一功能的多个类分散在不同的包中，不便于模块化开发。
按照功能方式划分，例如将所有与加/解密有关的类放入一个包，将所有与 HTTP请求有关的类放入一个包。这种分类方式下，同一功能的类内聚性高，便于模块化开发，但会导致同一包内类的类型混乱

通常，在进行一个项目的包结构设计时会同时采用以上两种划分方式。exceptions包就是按照类型划分出来的，但也有许多异常类按照功能划分到了其他包中。==MyBatis 中的包也是按照上述两种方式划分的，一类是按照类型划分出来的包，如exceptions包、annotations包；一类是按照功能划分出来的包，如 logging包、plugin包。==

在项目设计和开发中，我们推荐优先将功能耦合度高的类放入按照功能划分的包中，而将功能耦合度低或供多个功能使用的类放入按照类型划分的包中。这种划分思想不仅可以用在包的划分上，类、方法、代码片段的组合与拆分等都可以参照这种思想。

# ExceptionFactory类
## 介绍
该类是负责生产 Exception的工厂。ExceptionFactory类只有两个方法。构造方法由 private修饰，确保该方法无法在类的外部被调用，也就永远无法生成该类的实例。通常，会对一些工具类、工厂类等仅提供静态方法的类进行这样的设置，因为这些类不需要实例化就可以使用。wrapException方法就是 ExceptionFactory类提供的静态方法，它用来生成并返回一个RuntimeException对象。

```java
  @Override
  public void rollback(boolean force) {
    try {
      executor.rollback(isCommitOrRollbackRequired(force));
      dirty = false;
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```

```java

/**
 * @author Clinton Begin
 */
public class ExceptionFactory {

  private ExceptionFactory() {
    // Prevent Instantiation
  }

  public static RuntimeException wrapException(String message, Exception e) {
    return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
  }

}
```
## 为什么使用工厂不是直接new呢？
### 【统一的异常处理机制】
通过 ExceptionFactory.wrapException 来封装异常，MyBatis 可以提供一个统一的异常处理方式。这样做的好处是，在整个系统中无论遇到什么异常，都会通过 wrapException 进行统一包装，并且可以附带有一致的异常信息（比如 ErrorContext 中的消息和堆栈信息）。这种方式增强了异常的一致性，方便了异常的管理和日志记录。

### 【异常的封装与转化】
直接 new 一个异常是可以的，但 MyBatis 通过 wrapException 将异常转化为 PersistenceException，这种做法有助于将底层异常封装成一个业务层异常（如 PersistenceException），而不是让底层的异常类型暴露给上层调用者。这样上层代码就不需要关心底层的具体实现细节（如 SQLException 或其他数据库相关的异常），只需要处理通用的业务异常（PersistenceException）。这也是常见的“异常转译”或“异常抽象”设计模式。

### 【异常上下文（ErrorContext）】
ErrorContext.instance().message(message).cause(e).toString() 通过 ErrorContext 来追踪异常的上下文信息，提供更多的诊断信息。这对于调试和日志分析非常有用，可以提供异常发生时的详细背景、错误信息以及导致异常的根本原因。这使得开发者可以更容易地定位问题，提升系统的可维护性。
### 【扩展性】
使用 ExceptionFactory 封装异常，意味着未来如果需要改变异常的处理方式（例如，加入更多的异常日志、不同的异常类型等），只需要在 wrapException 方法中做修改，而不需要修改系统中每个直接抛出异常的地方。这提高了代码的可维护性和可扩展性。
通过 ExceptionFactory.wrapException 这样的设计，MyBatis 实现了异常的统一封装、转化和上下文管理，增强了系统的可维护性、可调试性和可扩展性。直接 new 一个异常虽然简单，但缺乏统一的异常处理和额外的上下文信息，容易导致异常管理混乱。


# ErrorContext 的作用
## 概述
MyBatis 中的 ErrorContext 设计主要用于捕获和存储在执行 SQL 操作过程中发生的错误的相关上下文信息。它通过提供详细的错误上下文信息来帮助开发人员调试和排查问题，尤其是在与数据库交互时发生异常时。ErrorContext 类主要的作用是封装和管理错误相关的诊断信息，使得错误日志更加易于理解和追踪。
### 代码

```java
public class ErrorContext {

  private static final String LINE_SEPARATOR = System.lineSeparator();
  private static final ThreadLocal<ErrorContext> LOCAL = ThreadLocal.withInitial(ErrorContext::new);

  private ErrorContext stored;
  private String resource;
  private String activity;
  private String object;
  private String message;
  private String sql;
  private Throwable cause;

  private ErrorContext() {
  }

  public static ErrorContext instance() {
    return LOCAL.get();
  }

  public ErrorContext store() {
    ErrorContext newContext = new ErrorContext();
    newContext.stored = this;
    LOCAL.set(newContext);
    return LOCAL.get();
  }

  public ErrorContext recall() {
    if (stored != null) {
      LOCAL.set(stored);
      stored = null;
    }
    return LOCAL.get();
  }

  public ErrorContext resource(String resource) {
    this.resource = resource;
    return this;
  }

  public ErrorContext activity(String activity) {
    this.activity = activity;
    return this;
  }

  public ErrorContext object(String object) {
    this.object = object;
    return this;
  }

  public ErrorContext message(String message) {
    this.message = message;
    return this;
  }

  public ErrorContext sql(String sql) {
    this.sql = sql;
    return this;
  }

  public ErrorContext cause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  public ErrorContext reset() {
    resource = null;
    activity = null;
    object = null;
    message = null;
    sql = null;
    cause = null;
    LOCAL.remove();
    return this;
  }

  @Override
  public String toString() {
    StringBuilder description = new StringBuilder();

    // message
    if (this.message != null) {
      description.append(LINE_SEPARATOR);
      description.append("### ");
      description.append(this.message);
    }

    // resource
    if (resource != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may exist in ");
      description.append(resource);
    }

    // object
    if (object != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error may involve ");
      description.append(object);
    }

    // activity
    if (activity != null) {
      description.append(LINE_SEPARATOR);
      description.append("### The error occurred while ");
      description.append(activity);
    }

    // sql
    if (sql != null) {
      description.append(LINE_SEPARATOR);
      description.append("### SQL: ");
      description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
    }

    // cause
    if (cause != null) {
      description.append(LINE_SEPARATOR);
      description.append("### Cause: ");
      description.append(cause.toString());
    }

    return description.toString();
  }

}

```
ErrorContext 的设计可以帮助 MyBatis 更好地追踪和记录执行过程中发生的异常和错误信息。它提供了关于 SQL 执行过程中各个步骤的详细信息，例如：当前正在执行的 SQL 语句、相关的映射文件、参数信息等，这些都可以帮助开发人员快速定位问题。

## store()和recall()方法设计分析
在 MyBatis 中，private ErrorContext stored; 这行代码通常用于 保存当前线程的错误上下文。具体来说，stored 变量通常是一个 ErrorContext 类型的对象，它的作用是暂时存储和保存错误相关的信息，以便在整个操作过程中能够访问和更新。

```java
 private ErrorContext stored;

public ErrorContext store() {
    ErrorContext newContext = new ErrorContext();
    newContext.stored = this;
    LOCAL.set(newContext);
    return LOCAL.get();
  }

  public ErrorContext recall() {
    if (stored != null) {
      LOCAL.set(stored);
      stored = null;
    }
    return LOCAL.get();
  }
```
store() 方法
• 这个方法的作用是 保存当前的错误上下文 到一个新的 ErrorContext 实例中，并将其设置为当前线程的错误上下文。
• newContext.stored = this：这行代码的意思是，将当前的 ErrorContext（即 this）存储到新创建的 newContext 中，这样可以在之后恢复原来的上下文。
• LOCAL.set(newContext)：将新创建的上下文对象设置为当前线程的 ErrorContext。由于使用了 ThreadLocal，每个线程都会有独立的错误上下文。
• return LOCAL.get()：返回当前线程的 ErrorContext（即 newContext）。

store() 方法一般在 操作开始时调用，用于 保存当前线程的错误上下文，并为后续的错误信息提供独立的上下文。比如，当处理一个新的数据库操作时，可能需要清空当前的错误上下文并开始一个新的上下文。在执行某个操作时，如果错误发生，则可以在新的上下文中记录错误信息。

recall() 方法通常用于 操作完成后，恢复原来的错误上下文，特别是在跨越多个操作的错误追踪中，恢复原来的上下文信息。比如，在处理完某个操作后，系统可能需要恢复到先前的错误上下文，以便继续处理其他操作或者记录原有的错误信息。

```java
  protected void generateKeys(Object parameter) {
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    ErrorContext.instance().store();
    keyGenerator.processBefore(executor, mappedStatement, null, parameter);
    ErrorContext.instance().recall();
  }
```
上面这段代码就是实际mybatis使用store和recall地方。
这段代码出现在 MyBatis 中，通常是在执行某些数据库操作时生成主键的过程，尤其是在插入操作中，涉及到主键生成的逻辑。generateKeys() 方法负责生成数据库操作后的主键，并确保在生成主键的过程中能够正确管理和追踪错误上下文。让我们逐行分析这段代码，理解其具体含义和作用：

```java
protected void generateKeys(Object parameter) {
    // 获取与当前映射语句相关的 KeyGenerator
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    
    // 存储当前线程的错误上下文，防止在主键生成过程中出现错误时丢失上下文
    ErrorContext.instance().store();
    
    // 在执行主键生成前，调用 KeyGenerator 的处理方法
    keyGenerator.processBefore(executor, mappedStatement, null, parameter);
    
    // 恢复之前存储的错误上下文，确保其他操作的错误上下文不受影响
    ErrorContext.instance().recall();
}

```
## 为什么需要 store 和 recall？
在 MyBatis 中，ErrorContext 负责捕获和存储当前错误的上下文信息。在多步骤的操作中（如插入操作生成主键时），有时会因为主键生成的策略（如自增长主键）涉及到复杂的数据库操作，因此需要确保在生成主键之前，当前的错误上下文能够被保存，避免在主键生成时如果发生错误丢失上下文信息。
通过调用 store() 方法，保存当前错误上下文，并在执行主键生成逻辑后通过 recall() 恢复之前的上下文，能够确保错误信息在不同的操作之间得到正确的隔离和处理。这种方式使得 MyBatis 可以精确地记录并跟踪错误，尤其是在复杂的多步骤操作中，避免错误上下文的混乱。

上面方法的最外层是DefaultSqlSession#update(java.lang.String, java.lang.Object)

```java
  @Override
  public int update(String statement, Object parameter) {
    try {
      dirty = true;
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.update(ms, wrapCollection(parameter));
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```
上面会捕获所有的异常并使用ExceptionFactory，而里面输出的信息就是ErrorContext存储的。
假设执行keyGenerator.processBefore(executor, mappedStatement, null, parameter);出现数据库异常了，那么此时可能抛出SQLException异常，而且在processBefore中会执行很多的自定义的操作，这些可能导致ErrorContext中存储的信息被修改了，那么之前在processBefore前保存的上下文信息就丢失了，这就会导致最终异常信息输出的时候只有processBefore的信息了，而丢失了原来的上下文信息，在使用了store方法后，先将这个前面的上下文信息保存，这样的话，假设在processBefore中ErrorContext信息被修改了，在最后的异常信息输出的时候，也可以通过store字段获取到。store() 和 recall() 确保了每个操作的错误上下文是独立的。在 generateKeys() 执行前，保存当前的错误上下文，在生成主键后恢复原有的上下文。这样，即使在执行过程中某个操作失败，错误信息也能清楚地区分开来，避免了上下文混乱。如果 generateKeys() 阶段失败，错误信息会指明是主键生成阶段的问题，而不是后续的更新操作。这样能够更容易地诊断和解决问题。会清楚知道错误发生在更新操作而不是主键生成阶段，从而能够更快定位问题。


通过使用 ErrorContext 进行错误上下文的存储和恢复，可以确保每个操作的错误信息都能得到独立处理，便于调试和问题的准确定位。