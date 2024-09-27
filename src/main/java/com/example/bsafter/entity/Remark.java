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
@TableName("remark")
public class Remark implements Serializable {
    @TableId
    private int id;
    private String uname;
    private String content;
    private String remarktime;
}
