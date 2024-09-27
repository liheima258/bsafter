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
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("manager")

/*
超级管理员：可以修改任何管理员的信息，可以删除，拉黑和取消拉黑任何管理员

普通管理员：只可以修改自己的信息，不可以删除，拉黑和取消拉黑任何管理员

任何管理员都可以删除，拉黑和取消拉黑任意用户（注意删除用户要同时删除用户的日志和用户当前的座位申请）
 */
public class Manager implements Serializable {
    @TableId
    private String  mid;
    private String  mpassword;
    private String  mname;
    private String  msex;
    private String  maddr;
    private String  mphone;
    private String  memail;
    private String  mvx;
    //正常 禁止操作
    private String  mstatus;
    //普通 超级管理员
    private String  mauthority;

    @TableField(exist = false)
    private String  mdpassword;
    @TableField(exist = false)
    private String  token;
    @TableField(exist = false)
    private String  newPassword;
    @TableField(exist = false)
    private String  confirm;
}
