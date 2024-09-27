package com.example.bsafter.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@SuppressWarnings("SpellCheckingInspection")
@Data  //设置get set
@AllArgsConstructor  //设置包含所有参数的构造方法
@NoArgsConstructor   //设置无参的构造方法
@Builder             //用于创建对象
@TableName("user")
public class User implements Serializable {
    @TableId
    private String  uid;
    private String  upassword;
    private String  uname;
    private String  usex;
    private String  uaddr;
    private String  uphone;
    private String  uemail;
    private String  uvx;
    // 未使用 预约中 待签到 使用中
    private String  ustatus;
    //允许预约 禁止预约
    private String  uauthority;
    //用户账号余额
    private  int balance;
    @TableField(exist = false)
    private String  udpassword;
    @TableField(exist = false)
    private String newPassword;
    @TableField(exist = false)
    private String token;
}
