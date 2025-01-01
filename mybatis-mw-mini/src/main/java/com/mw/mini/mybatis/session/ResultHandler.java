package com.mw.mini.mybatis.session;

/**
 * @author zhangyu
 * @description 结果处理器
 * 
 * 
 */
public interface ResultHandler {

    /**
     * 处理结果
     */
    void handleResult(ResultContext context);

}
