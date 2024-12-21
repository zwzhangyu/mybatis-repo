package cn.bugstack.mybatis.test.dao;

import cn.bugstack.mybatis.test.po.User;

import java.util.List;

public interface IUserDao {

    User queryUserInfoById(Long id);

    User queryUserInfo(User req);

    List<User> listUserInfo(User req);


    List<User> listUserOrderInfo(User req);

}
