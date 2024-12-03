package com.zy.client.datasource;

/*******************************************************
 * Created by ZhangYu on 2024/12/2
 * Description :
 * History   :
 *******************************************************/
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SimpleDataSource implements DataSource {
  private String url;
  private String username;
  private String password;

  public SimpleDataSource(Properties properties) {
    this.url = properties.getProperty("url");
    this.username = properties.getProperty("username");
    this.password = properties.getProperty("password");
    try {
      String driver = properties.getProperty("driver");
      if (driver != null) {
        Class.forName(driver); // 加载驱动类
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to load database driver", e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  // 以下方法为 `DataSource` 接口的默认实现，仅作占位符
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  @Override
  public java.io.PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter(java.io.PrintWriter out) throws SQLException {}

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {}

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public java.util.logging.Logger getParentLogger() {
    return java.util.logging.Logger.getLogger("SimpleDataSource");
  }
}
