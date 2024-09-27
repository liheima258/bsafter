package com.example.bsafter.entity;

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
@TableName("email")
public class Email implements Serializable {
    @TableId
    private int    id;
    private String email;
    private String code;
    /*
        管理员 ：1代表忘记密码验证
        用户 ： 2代表忘记密码验证
     */
    private int    type;
    private String time;
}
