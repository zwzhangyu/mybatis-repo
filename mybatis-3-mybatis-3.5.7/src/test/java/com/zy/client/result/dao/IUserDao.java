package com.zy.client.result.dao;


import com.zy.client.result.po.User;

import java.util.List;

public interface IUserDao {



    List<User> listUserOrderInfo(User req);

}
