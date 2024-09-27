package com.example.bsafter.service;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.Floor;
import com.example.bsafter.mapper.FloorMapper;
import org.springframework.stereotype.Service;


/**
 * 功能：楼层模块
 * 日期：2024/4/719:17
 */
@Service
public class FloorService extends ServiceImpl<FloorMapper, Floor> {

    public void update_usenumber(String fname,boolean flg){
        LambdaUpdateWrapper<Floor> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        if(flg) lambdaUpdateWrapper.eq(Floor :: getFname,fname).setSql("usenumber=usenumber+1");
        else lambdaUpdateWrapper.eq(Floor :: getFname,fname).setSql("usenumber=usenumber-1");
        this.update(lambdaUpdateWrapper);
    }
}
