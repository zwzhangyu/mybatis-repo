package com.zy.client.datasource;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class SimpleDataSourceFactory implements DataSourceFactory {
  private SimpleDataSource dataSource;

  @Override
  public void setProperties(Properties properties) {
    this.dataSource = new SimpleDataSource(properties);
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }
}
