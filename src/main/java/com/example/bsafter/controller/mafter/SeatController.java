package com.example.bsafter.controller.mafter;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Options;
import com.example.bsafter.entity.Room;
import com.example.bsafter.entity.Seat;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.FloorService;
import com.example.bsafter.service.RoomService;
import com.example.bsafter.service.SeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能：座位模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    SeatService seatService;
    @Autowired
    RoomService roomService;
    @Autowired
    FloorService floorService;


    //查询显示所有座位
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam  Integer size, @RequestParam  Integer current, @RequestBody Seat conditions) {
        IPage<Seat> iPage = new Page<>(current,size);
        LambdaQueryWrapper<Seat> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(conditions.getStatus() != null && !conditions.getStatus().equals("") && !conditions.getStatus().equals("全部"),Seat :: getStatus, conditions.getStatus())
                .like(conditions.getRname() != null && !conditions.getRname().equals(""),Seat :: getRname, conditions.getRname())
                .like(conditions.getSname() != null && !conditions.getSname().equals(""),Seat :: getSname, conditions.getSname());
        IPage<Seat> page = seatService.page(iPage, lambdaQueryWrapper);
        if(page.getRecords().size()==0){
            throw new ServiceException("未查找到座位!");
        }
        return Result.success(page);
    }

    //删除座位
    @DeleteMapping("/deleteSeat")
    public Result deleteSeat(@RequestParam int sid){
        Seat seat = seatService.getById(sid);
        LambdaQueryWrapper<Room> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room :: getFname,seat.getFname())
                .eq(Room :: getRname,seat.getRname());
        Room room = roomService.getOne(lambdaQueryWrapper);
        LambdaUpdateWrapper<Room> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Room :: getRid,room.getRid()).setSql("sumseat=sumseat-1,restseat=restseat-1");
        roomService.update(lambdaUpdateWrapper);
        seatService.removeById(sid);
        return Result.success();
    }

    //查询当前楼层中的自习室，显示在下拉菜单中
    @GetMapping("/selectRoom")
    public Result selectRoom(@RequestParam String fname){
        LambdaQueryWrapper<Room> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room :: getFname,fname);
        List<Room> roomList = roomService.list(lambdaQueryWrapper);
        if(roomList.size() == 0) throw new ServiceException("添加失败!当前无自习室!");
        List<Options> roomOptionsList =new ArrayList<>();
        for(int i = 0 ; i < roomList.size() ; i++){
            Options roomOptions =  new Options(roomList.get(i).getRname(),roomList.get(i).getRname(),roomList.get(i).getRname());
            roomOptionsList.add(roomOptions);
        }
        return Result.success(roomOptionsList);
    }

    //新增座位
    @PostMapping("/addSeat")
    public  Result addSeat(@RequestBody Seat newSeat){
        if (StrUtil.isBlank(newSeat.getSname())) {
            return Result.error("输入不能为空!");
        } else {
            Seat dbseat = seatService.getSeat(newSeat.getFname(),newSeat.getRname(),newSeat.getSname());
            if (dbseat != null){
                throw new ServiceException("座位已存在!");
            }
            newSeat.setStatus("空闲");
            seatService.save(newSeat);
            LambdaUpdateWrapper<Room> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(Room :: getFname,newSeat.getFname())
                    .eq(Room :: getRname,newSeat.getRname()).setSql("sumseat=sumseat+1,restseat=restseat+1");
            roomService.update(lambdaUpdateWrapper);
            return Result.success();
        }
    }

    //座位信息修改
    @PutMapping("/updateSeat")
    public  Result updateSeat(@RequestBody Seat newSeat, @RequestParam String oldSeatName){
        if (oldSeatName.equals(newSeat.getSname())) return Result.success();
        Seat dbSeat = seatService.getSeat(newSeat.getFname(),newSeat.getRname(),newSeat.getSname());
        if (dbSeat != null) throw new ServiceException("修改失败!名称重复!");
        seatService.updateById(newSeat);
        return Result.success();
    }

}

