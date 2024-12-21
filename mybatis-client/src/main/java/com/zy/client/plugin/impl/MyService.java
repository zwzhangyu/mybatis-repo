package com.zy.client.plugin.impl;

import com.zy.client.plugin.BizService;

/*******************************************************
 * Created by ZhangYu on 2024/12/21
 * Description :
 * History   :
 *******************************************************/
public class MyService implements BizService {
    @Override
    public void doSomething(String param) {
        System.out.println("Executing doSomething : " + param);
    }
}
