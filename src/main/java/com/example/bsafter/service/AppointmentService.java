package com.example.bsafter.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.Appointment;
import com.example.bsafter.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 功能：首页和预约模块
 * 日期：2024/4/719:17
 */
@Service
public class AppointmentService extends ServiceImpl<AppointmentMapper, Appointment> {

    public void update_status(int id,String newStatus){
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getStatus,newStatus);
        this.update(lambdaUpdateWrapper);
    }

    public void update_payment(int id){
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getPayment,0);
        this.update(lambdaUpdateWrapper);
    }

    public void update_payment(int id,int payment){
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getPayment,payment);
        this.update(lambdaUpdateWrapper);
    }

    public void update_audittime(int id,Date time){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getAudittime,spf.format(time));
        this.update(lambdaUpdateWrapper);
    }

    public void update_endtime(int id,Date time){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getEndtime,spf.format(time));
        this.update(lambdaUpdateWrapper);
    }


    public void update_SignTime(int id,Date time){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LambdaUpdateWrapper<Appointment> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Appointment :: getId,id)
                .set(Appointment :: getSigntime,spf.format(time));
        this.update(lambdaUpdateWrapper);
    }
}
