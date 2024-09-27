package com.example.bsafter.controller.mafter;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.AppointList;
import com.example.bsafter.entity.Seat;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 功能：预约列表模块
 */

@Slf4j
@RestController
@RequestMapping("/appointList")
public class AppointListController {

    @Autowired
    SeatService seatService;
    @Autowired
    AppointListService appointListService;



    // 微信 显示当前自习室的所有座位
    @GetMapping("/wxSelectAllSeat")
    public Result wxSelectAllSeat(@RequestParam String fname, @RequestParam String rname,
                                  @RequestParam String startTime, @RequestParam String exceptTime) {
        int starttime = Integer.parseInt(startTime);
        int excepttime = Integer.parseInt(exceptTime);
        //先判断时间区间是否合法
        if (starttime >= excepttime) throw new ServiceException("时间错误!");
        //获取预约列表
        LambdaQueryWrapper<AppointList> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppointList::getFname, fname).eq(AppointList::getRname, rname);
        List<AppointList> AppointList = appointListService.list(lambdaQueryWrapper);
        //获取当前楼层的当前自习室的所有座位，并将状态全部设置为空闲
        LambdaQueryWrapper<Seat> QueryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper.eq(Seat::getFname, fname).eq(Seat::getRname, rname);
        List<Seat> SeatList = seatService.list(QueryWrapper);
        for (int k = 0; k < SeatList.size(); k++) {
            Seat temp = SeatList.get(k);
            temp.setStatus("空闲");
            SeatList.set(k, temp);
        }
        //只要查询区间在任意一个已有区间的范围内，则设置座位状态为使用中
        for (int i = 0; i < SeatList.size(); i++) {
            Seat s = SeatList.get(i);
            for (int j = 0; j < AppointList.size(); j++) {
                if (SeatList.get(i).getFname().equals(AppointList.get(j).getFname()) &&
                        SeatList.get(i).getRname().equals(AppointList.get(j).getRname()) &&
                        SeatList.get(i).getSname().equals(AppointList.get(j).getSname())) {
                    AppointList temp = AppointList.get(j);
                    int start = temp.getStarttime();
                    int end = temp.getEndtime();
                    if (!(excepttime <= start || starttime >= end)) {
                        s.setStatus("使用中");
                        SeatList.set(i, s);
                    }
                }
            }
        }
        return Result.success(SeatList);
    }
}

