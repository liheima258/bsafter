package com.example.bsafter.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.Seat;
import com.example.bsafter.mapper.SeatMapper;
import org.springframework.stereotype.Service;


/**
 * 功能：座位模块
 * 日期：2024/4/719:17
 */
@Service
public class SeatService extends ServiceImpl<SeatMapper, Seat> {

    public void update_status(String fname,String rname,String sname,String newStatus){
        LambdaUpdateWrapper<Seat> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Seat::getFname, fname)
                    .eq(Seat::getRname, rname)
                .eq(Seat :: getSname,sname)
                .set(Seat :: getStatus,newStatus);
        this.update(lambdaUpdateWrapper);
    }


    public Seat getSeat(String fname,String rname,String sname){
        LambdaQueryWrapper<Seat> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Seat::getFname, fname)
                .eq(Seat::getRname, rname)
                .eq(Seat :: getSname,sname);
        return this.getOne(lambdaQueryWrapper);
    }
}
