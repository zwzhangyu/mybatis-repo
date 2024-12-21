package cn.bugstack.mybatis.reflection;

import cn.bugstack.mybatis.reflection.invoker.Invoker;
import cn.bugstack.mybatis.test.po.User;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/*******************************************************
 * Created by ZhangYu on 2024/12/15
 * Description :
 * History   :
 *******************************************************/
@Slf4j
public class ReflectorTest {

    @Test
    public void test1() throws Exception {
        UserBean userBean = UserBean.class.newInstance();
        MetaObject metaObject = SystemMetaObject.forObject(userBean);

        metaObject.setValue("username","111");
        log.info("MetaObject:{}",userBean);
    }

    @Data
    static class UserBean{
        private int id;
        private String username;
        private int age;
    }

}