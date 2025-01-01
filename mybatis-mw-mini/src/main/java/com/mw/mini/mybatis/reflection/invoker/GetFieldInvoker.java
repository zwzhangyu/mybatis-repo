package com.mw.mini.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * @author zhangyu
 * @description getter 调用者
 * 
 * 
 */
public class GetFieldInvoker implements Invoker {

    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

}
