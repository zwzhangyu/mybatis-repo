package com.mw.mini.mybatis.session;

/**
 * @author zhangyu
 * @description 结果上下文
 * 
 * 
 */
public interface ResultContext {

    /**
     * 获取结果
     */
    Object getResultObject();

    /**
     * 获取记录数
     */
    int getResultCount();

}
