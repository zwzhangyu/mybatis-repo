package com.mw.mini.mybatis.scripting.xmltags;

/**
 * @author zhangyu
 * @description SQL 节点
 * 
 * 
 */
public interface SqlNode {

    boolean apply(DynamicContext context);

}