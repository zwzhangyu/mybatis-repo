/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class PoolState {

  /**
   * 连接池的核心数据源，负责管理数据库连接的获取、使用和释放
   */
  protected PooledDataSource dataSource;

  /**
   * 空闲连接对象池
   */
  protected final List<PooledConnection> idleConnections = new ArrayList<>();
  /**
   * 活跃连接对象池
   */
  protected final List<PooledConnection> activeConnections = new ArrayList<>();
  /**
   * 记录连接池的连接请求总数。每当应用程序请求连接时，这个值就会增加。它是一个重要的统计数据，帮助监控系统的整体负载。
   */
  protected long requestCount = 0;
  /**
   * 记录所有连接请求的累计时间。通过该字段可以统计每次连接请求从发起到获得连接的总时间，帮助评估连接池的响应效率和数据库性能。
   */
  protected long accumulatedRequestTime = 0;
  /**
   * 记录所有活跃连接的累计使用时间（即被借出的时间）。该值越大，说明连接池中的连接被长时间占用，可能需要优化连接池配置或者数据库查询性能。
   */
  protected long accumulatedCheckoutTime = 0;
  /**
   * 记录那些被借用并超过最大允许借用时间（poolMaximumCheckoutTime）的连接数。
   * 当连接超时未归还时，这个值会增加。这个字段帮助监控数据库连接是否被合理使用，并防止连接泄漏。
   */
  protected long claimedOverdueConnectionCount = 0;
  /**
   * 记录所有超时连接的累计占用时间。这可以帮助了解超时连接对系统性能的影响。
   * 如果该值过大，意味着连接池中存在不合理使用的连接，可能需要优化连接超时设置。
   */
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
  /**
   * 记录等待连接的累计时间。它表示连接池中等待获取连接的线程总共等待了多少时间。
   * 这个字段对于评估连接池的响应能力和识别潜在的性能瓶颈非常重要。
   */
  protected long accumulatedWaitTime = 0;
  /**
   * 记录等待连接的次数。如果连接池已满，且没有空闲连接可用，新的连接请求会被挂起，直到有连接可用。
   * 该字段用于统计发生等待的连接请求次数。这个值过高表明连接池的容量可能不足，需要增加连接数或优化查询性能。
   */
  protected long hadToWaitCount = 0;
  /**
   * 记录发生错误的连接数（例如无法建立连接或连接池中的连接出现故障）。
   * 这个字段有助于监控连接池的健康状态，并可以帮助快速定位和修复连接相关的问题。
   */
  protected long badConnectionCount = 0;

  public PoolState(PooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public synchronized long getRequestCount() {
    return requestCount;
  }

  public synchronized long getAverageRequestTime() {
    return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
  }

  public synchronized long getAverageWaitTime() {
    return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;

  }

  public synchronized long getHadToWaitCount() {
    return hadToWaitCount;
  }

  public synchronized long getBadConnectionCount() {
    return badConnectionCount;
  }

  public synchronized long getClaimedOverdueConnectionCount() {
    return claimedOverdueConnectionCount;
  }

  public synchronized long getAverageOverdueCheckoutTime() {
    return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
  }

  public synchronized long getAverageCheckoutTime() {
    return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
  }

  public synchronized int getIdleConnectionCount() {
    return idleConnections.size();
  }

  public synchronized int getActiveConnectionCount() {
    return activeConnections.size();
  }

  @Override
  public synchronized String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("\n===CONFINGURATION==============================================");
    builder.append("\n jdbcDriver                     ").append(dataSource.getDriver());
    builder.append("\n jdbcUrl                        ").append(dataSource.getUrl());
    builder.append("\n jdbcUsername                   ").append(dataSource.getUsername());
    builder.append("\n jdbcPassword                   ").append(dataSource.getPassword() == null ? "NULL" : "************");
    builder.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
    builder.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
    builder.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
    builder.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
    builder.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
    builder.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
    builder.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
    builder.append("\n ---STATUS-----------------------------------------------------");
    builder.append("\n activeConnections              ").append(getActiveConnectionCount());
    builder.append("\n idleConnections                ").append(getIdleConnectionCount());
    builder.append("\n requestCount                   ").append(getRequestCount());
    builder.append("\n averageRequestTime             ").append(getAverageRequestTime());
    builder.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
    builder.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
    builder.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
    builder.append("\n hadToWait                      ").append(getHadToWaitCount());
    builder.append("\n averageWaitTime                ").append(getAverageWaitTime());
    builder.append("\n badConnectionCount             ").append(getBadConnectionCount());
    builder.append("\n===============================================================");
    return builder.toString();
  }

}
