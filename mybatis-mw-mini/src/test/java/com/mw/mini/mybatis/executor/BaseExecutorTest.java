package com.mw.mini.mybatis.executor;

import com.mw.mini.mybatis.cache.impl.PerpetualCache;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/*******************************************************
 * Created by ZhangYu on 2024/12/31
 * Description : BaseExecutor测试
 *******************************************************/
public class BaseExecutorTest {

    @Test
    public void testExecutionPlaceholder(){
        PerpetualCache localCache = new PerpetualCache("LocalCache");
        String key="1";
        localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        //Object result =  (List) localCache.getObject(key);
    }

}