package com.zy.client.bean;

import lombok.Data;

import java.util.Date;

/*******************************************************
 * Created by ZhangYu on 2024/12/26
 * Description : 测试用户对象Bean
 *******************************************************/
@Data
public class UserBean {

    private Long id;

    // 用户ID
    private String userId;

    // 用户名称
    private String userName;

    // 用户手机号
    private String mobileNo;

    // 用户地址
    private String address;


    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

}
