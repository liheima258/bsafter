package com.example.bsafter.controller.mafter;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Floor;
import com.example.bsafter.entity.Options;
import com.example.bsafter.entity.Room;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.FloorService;
import com.example.bsafter.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能：自习室模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    RoomService roomService;
    @Autowired
    FloorService floorService;

    //查询显示所有自习室
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam Integer size, @RequestParam Integer current, @RequestBody Room conditions) {
        IPage<Room> iPage = new Page<>(current, size);
        LambdaQueryWrapper<Room> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(conditions.getFname() != null && !conditions.getFname().equals("") && !conditions.getFname().equals("全部"), Room::getFname, conditions.getFname())
                .like(conditions.getRname() != null && !conditions.getRname().equals(""), Room::getRname, conditions.getRname());
        IPage<Room> page = roomService.page(iPage, lambdaQueryWrapper);
        if (page.getRecords().size() == 0) {
            throw new ServiceException("未查找到自习室!");
        }
        return Result.success(page);
    }

    //删除自习室
    @DeleteMapping("/deleteRoom")
    public Result deleteRoom(@RequestParam int rid) {
        Room room = roomService.getById(rid);
        Floor floor = floorService.getById(room.getFname());
        LambdaUpdateWrapper<Floor> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Floor::getFname, floor.getFname()).setSql("sumroom=sumroom-1");
        floorService.update(lambdaUpdateWrapper);
        roomService.removeById(rid);
        return Result.success();
    }

    //查询所有楼层，显示在下拉菜单中
    @GetMapping("/selectFloor")
    public Result selectFloor() {
        List<Floor> floorList = floorService.list();
        if (floorList.size() == 0) throw new ServiceException("添加失败!当前无楼层!");
        List<Options> floorOptionsList = new ArrayList<>();
        Options floorOptions = new Options("全部", "全部", "全部");
        floorOptionsList.add(floorOptions);
        for (int i = 0; i < floorList.size(); i++) {
            floorOptions = new Options(floorList.get(i).getFname(), floorList.get(i).getFname(), floorList.get(i).getFname());
            floorOptionsList.add(floorOptions);
        }
        return Result.success(floorOptionsList);
    }


    //新增自习室
    @PostMapping("/addRoom")
    public Result addRoom(@RequestBody Room newRoom) {
        LambdaQueryWrapper<Room> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room::getRname, newRoom.getRname())
                .eq(Room::getFname, newRoom.getFname());
        Room dbroom = roomService.getOne(lambdaQueryWrapper);
        if (dbroom != null) {
            throw new ServiceException("自习室已存在!");
        }
        roomService.save(newRoom);
        LambdaUpdateWrapper<Floor> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Floor::getFname, newRoom.getFname()).setSql("sumroom=sumroom+1");
        floorService.update(lambdaUpdateWrapper);
        return Result.success();
    }

    //更改自习室名
    @PutMapping("/updateRoom")
    public Result updateRoom(@RequestBody Room newRoom, @RequestParam String oldRoomName, @RequestParam int oldPrice) {
        LambdaUpdateWrapper<Room> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if (!newRoom.getRname().equals(oldRoomName) && !(newRoom.getPrice() == oldPrice)) {
            lambdaUpdateWrapper.eq(Room::getRid, newRoom.getRid())
                    .set(Room::getRname, newRoom.getRname())
                    .set(Room::getPrice, newRoom.getPrice());
            roomService.update(lambdaUpdateWrapper);
        } else if (!newRoom.getRname().equals(oldRoomName)) {
            lambdaUpdateWrapper.eq(Room::getRid, newRoom.getRid())
                    .set(Room::getRname, newRoom.getRname());
            roomService.update(lambdaUpdateWrapper);
        } else if (!(newRoom.getPrice() == oldPrice)) {
            lambdaUpdateWrapper.eq(Room::getRid, newRoom.getRid())
                    .set(Room::getPrice, newRoom.getPrice());
            roomService.update(lambdaUpdateWrapper);
        }
        return Result.success();
    }

    // 微信 根据楼名查找所有的自习室
    @GetMapping("/wxSelectAllRoom")
    public Result wxSelectAllRoom(@RequestParam String fname) {
        LambdaQueryWrapper<Room> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room::getFname, fname);
        List<Room> roomList = roomService.list(lambdaQueryWrapper);
        if (roomList.size() == 0 ) throw new ServiceException("此楼层无自习室");
        return Result.success(roomList);
    }

    // 微信 根据楼名和自习室名查找自习室信息
    @GetMapping("/wxSelectOneRoom")
    public Result wxSelectOneRoom(@RequestParam String fname,@RequestParam String rname) {
        LambdaQueryWrapper<Room> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Room::getFname, fname).eq(Room :: getRname,rname);
        return Result.success( roomService.getOne(lambdaQueryWrapper));
    }
}

