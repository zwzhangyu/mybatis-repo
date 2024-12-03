package com.zy.client.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class HikariCPDataSourceFactory implements DataSourceFactory {
  private HikariDataSource dataSource;

  @Override
  public void setProperties(Properties properties) {
    HikariConfig config = new HikariConfig();

    // 从配置中加载数据源参数
    config.setJdbcUrl(properties.getProperty("url"));
    config.setUsername(properties.getProperty("username"));
    config.setPassword(properties.getProperty("password"));

    // 可选的高级配置
    config.setDriverClassName(properties.getProperty("driver"));
    config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maxPoolSize", "10")));
    config.setMinimumIdle(Integer.parseInt(properties.getProperty("minIdle", "5")));
    config.setIdleTimeout(Long.parseLong(properties.getProperty("idleTimeout", "60000")));

    this.dataSource = new HikariDataSource(config);
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }
}
