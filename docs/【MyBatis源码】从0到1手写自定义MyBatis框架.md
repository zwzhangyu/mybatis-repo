> 🎮	作者主页：[点击](https://github.com/zwzhangyu)
> 🎁	完整专栏和代码：[点击](https://github.com/zwzhangyu/mybatis-repo)
> 🏡	博客主页：[点击](https://zwzhangyu.blog.csdn.net/)


# 引言
MyBatis作为一款轻量级的持久层框架，广泛用于Java开发中。它简化了数据库操作，但在很多情况下，我们可能需要对它的功能进行定制或理解其底层实现机制。本文将基于之前的源码分析和开源文章构建一个简化版的MyBatis框架，帮助大家更好地理解MyBatis的工作原理，并掌握自定义ORM框架的基本步骤。

# Mybatis的工作流程以及体系结构图
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/e98d714a750945c8814a156cbddcc4d4.png)
mybatis-config.xml 是 MyBatis 的全局配置文件，用于定义 MyBatis 的各种配置选项，如数据库连接信息、事务管理、缓存、插件、Mapper 的扫描等。这个文件是 MyBatis 框架初始化时读取的重要配置文件。

在 MyBatis 中，首先需要创建一个 SqlSessionFactory 对象。它是 MyBatis 的核心，它负责管理和创建 SqlSession 实例，连接数据库、执行 SQL 等操作。SqlSessionFactory 的创建通常通过 SqlSessionFactoryBuilder 来完成。SqlSession 是 MyBatis 与数据库交互的核心对象，所有的数据库操作都通过 SqlSession 完成。在得到 SqlSessionFactory 后，我们可以通过它创建一个 SqlSession。

Mapper 是 MyBatis 中的一个关键概念。它是 Java 接口与 SQL 映射文件之间的桥梁。通过映射文件或注解，Mapper 定义了 Java 方法和 SQL 语句的对应关系。

Executor 是执行 SQL 语句的核心组件，负责将映射的 SQL 语句发送到数据库，并处理查询结果。Executor 负责管理查询、更新等操作，并且支持多种类型的执行策略。

StatementHandler 负责处理 SQL 语句的预编译、参数设置以及执行 SQL 操作。它是 MyBatis 中执行 SQL 的核心组件之一，在执行数据库操作时起到了关键作用。StatementHandler 主要用于准备和执行 SQL 语句，并在查询过程中管理与数据库的交互。
# 项目结构
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/3e83979c1f934ed4a884c61218477f81.png)
【annotations包】简化 SQL 映射操作的相关注解（Annotations）。@Select，@Insert，@Update，@Delete等等。

【binding包】主要用来处理 Java方法与 SQL语句之间绑定关系的包。维护映射接口中抽象方法与数据库操作节点之间的关联关系；为映射接口中的抽象方法接入对应的数据库操作。

【builder包】是一个按照类型划分出来的包，包中存在许多的建造者类，主要是MapperAnnotationBuilder和XMLConfigBuilder。

【cache包】 MyBatis缓存能力的提供者。

【datasource包】主要用于数据源的管理和获取，MyBatis 通过它来管理数据库连接池等与数据源相关的操作

【executor包】主要负责执行 SQL 语句。它提供了多种执行策略，包括直接执行、重用执行、批量执行等。执行器是 MyBatis 核心的一部分，处理实际的数据库操作。

【io包】负责文件和资源的加载，包括 MyBatis 配置文件、Mapper 文件等。

【mapping包】包含了与 SQL 映射相关的类，定义了如何将 SQL 语句与 Java 方法以及 Java 对象进行映射。

【parsing包】负责 SQL 语句的解析工作，特别是动态 SQL 的解析和处理。

【plugin包】提供了 MyBatis 的插件机制。通过插件，开发者可以在 MyBatis 执行 SQL 操作的各个阶段插入自定义的逻辑。

【reflection包】主要处理与反射相关的操作，包括访问 Java 对象的字段、方法、构造函数等。

【scripting包】负责支持 MyBatis 中的动态 SQL 语句生成。它通过脚本语言（如 OGNL 或 Java）来构造动态 SQL。

【session包】负责 SQL 会话的管理。SqlSession 是 MyBatis 中的一个关键类，用于与数据库进行交互。

【transaction包】管理 MyBatis 的事务操作。它支持不同的事务管理方式，主要是 JDBC 事务。

【type包】主要用于处理 Java 类型与 JDBC 类型之间的转换。MyBatis 使用 TypeHandler 来实现这些转换。

# 代码运行
下载代码：
https://github.com/zwzhangyu/mybatis-repo.git
[github](https://github.com/zwzhangyu/mybatis-repo.git)

自定义框架：mybatis-mw-mini
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/1d5dd60bbacb48edb8df43066a8ff002.png)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/867c201323ee483a9c9b7de09d0d68e6.png)

测试脚本和测试配置文件,相关测试用例

# 历史源码分析文章
[专栏连接](https://blog.csdn.net/octopus21/category_12818138.html)
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/ddc8390f626c4396851574f9723a49db.png)

# 参考资料
【1】mybatis-3-mybatis-3.5.7源码。  [mybatis](https://github.com/mybatis/mybatis-3)

【2】《通用源码阅读指导书：MyBatis源码详解》 微信读书

【3】《MyBatis 3源码深度解析》  微信读书

【4】《Mybatis渐进式源码实践》