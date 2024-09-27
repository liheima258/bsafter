package com.example.bsafter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.Room;
import com.example.bsafter.mapper.RoomMapper;
import org.springframework.stereotype.Service;


/**
 * 功能：自习室模块
 * 日期：2024/4/719:17
 */
@Service
public class RoomService extends ServiceImpl<RoomMapper, Room> {

    public void update_restseat(String fname, String rname, boolean flg) {
        LambdaUpdateWrapper<Room> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if (flg) {
            lambdaUpdateWrapper.eq(Room::getFname, fname)
                    .eq(Room::getRname, rname)
                    .setSql("restseat=restseat+1");
        } else {
            lambdaUpdateWrapper.eq(Room::getFname, fname)
                    .eq(Room::getRname, rname)
                    .setSql("restseat=restseat-1");
        }
        this.update(lambdaUpdateWrapper);
    }

    public Room getRoom(String fname, String rname){
        LambdaQueryWrapper<Room> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room::getFname, fname)
                .eq(Room::getRname, rname);
        return this.getOne(lambdaQueryWrapper);
    }
}
