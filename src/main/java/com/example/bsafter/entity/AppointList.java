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
@TableName("appointList")
public class AppointList implements Serializable {
    @TableId
    private int id;
    private String fname;
    private String rname;
    private String sname;
    private int starttime;
    private int endtime;
}
