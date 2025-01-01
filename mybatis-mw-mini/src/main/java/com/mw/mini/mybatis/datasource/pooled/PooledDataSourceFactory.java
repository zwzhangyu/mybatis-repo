package com.mw.mini.mybatis.datasource.pooled;

import com.mw.mini.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author zhangyu
 * @description 有连接池的数据源工厂
 * 
 * 
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }

}
