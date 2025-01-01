package com.mw.mini.mybatis.mapper;

import com.mw.mini.mybatis.bean.UserBean;

import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/12/26
 * Description : 用户表mapper接口
 *******************************************************/
public interface UserMapper {


   // UserBean queryUserInfoById(UserBean userBean);


    List<UserBean> queryUserInfo(String userName, Long id, String mobileNo);

}
