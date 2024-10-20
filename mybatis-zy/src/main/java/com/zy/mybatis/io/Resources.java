package com.zy.mybatis.io;

import java.io.InputStream;

/*******************************************************
 * Created by ZhangYu on 2024/10/10
 * Description : 加载配置文件
 * History   :
 *******************************************************/
public class Resources {

    /**
     * 获取配置文件字节流
     *
     * @param path
     * @return
     */
    public static InputStream getResource(String path) {
        return Resources.class.getClassLoader().getResourceAsStream(path);
    }
}
