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
@TableName("appointment")
public class Appointment implements Serializable {
    @TableId
    private int id;
    private String uid;
    private String uname;
    private String uphone;
    private String fname;
    private String rname;
    private String sname;
    //用户支付的金额
    private int payment;
    // applytime=用户提交申请的时间
    private String applytime;
    // audittime=审核完成时间
    private String audittime;
    //审核完成之后，signtime=用户签到时间，starttime=预订开始时间，excepttime=预订结束时间,endtime == 用户正常结束 ？ excepttime : 提前结束时的时间
    private String signtime;
    private String starttime;
    private String excepttime;
    private String endtime;
    /*
       用户发起预约->status=等待审核
                  未审核通过之前取消预约->status=用户取消预约
                  审核通过->status=等待签到
                  审核不通过->status=审核不通过
                  完成签到->status=使用中
                  用户不再使用->status=使用结束
     */
    private String status;

    @TableField(exist = false)
    //使用时长 单位：分钟
    private int useTime;
}
