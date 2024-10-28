package com.zy.client.bean;

/*******************************************************
 * Created by ZhangYu on 2024/10/23
 * Description :
 * History   :
 *******************************************************/

public class UserBean {
  private Integer id;

  private String name;

  private String mobile_no;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMobile_no() {
    return mobile_no;
  }

  public void setMobile_no(String mobile_no) {
    this.mobile_no = mobile_no;
  }

  @Override
  public String toString() {
    return "UserBean{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", mobile_no='" + mobile_no + '\'' +
      '}';
  }
}
