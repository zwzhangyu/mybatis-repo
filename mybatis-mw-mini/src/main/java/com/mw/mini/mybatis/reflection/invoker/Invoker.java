package com.mw.mini.mybatis.reflection.invoker;

/**
 * @author zhangyu
 * @description 调用者
 *
 *
 */
public interface Invoker {

    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();

}
