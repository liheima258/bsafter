package com.example.bsafter.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.User;
import com.example.bsafter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 功能：用户
 * 日期：2024/4/719:17
 */
@Service
public class UserService extends ServiceImpl<UserMapper,User> {

    public void update_UstatusAndBalance(String uid,String newStatus,int payment,boolean flg){
        LambdaUpdateWrapper<User> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        if (flg){
            lambdaUpdateWrapper.eq(User :: getUid,uid)
                    .set(User :: getUstatus,newStatus)
                    .set(User :: getBalance,this.getById(uid).getBalance()+payment);
        }else{
            lambdaUpdateWrapper.eq(User :: getUid,uid)
                    .set(User :: getUstatus,newStatus)
                    .set(User :: getBalance,this.getById(uid).getBalance()-payment);
        }
        this.update(lambdaUpdateWrapper);
    }

    public void update_ustatus(String uid,String newStatus){
        LambdaUpdateWrapper<User> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User :: getUid,uid)
                    .set(User :: getUstatus,newStatus);
        this.update(lambdaUpdateWrapper);
    }

}
