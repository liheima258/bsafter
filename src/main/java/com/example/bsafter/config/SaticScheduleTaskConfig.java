package com.example.bsafter.config;

import com.example.bsafter.entity.Appointment;
import com.example.bsafter.service.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 功能：定时任务类
 * 日期：2024/6/2419:48
 */
@Configuration      //主要用于标记配置类，兼备Component的效果。
@EnableScheduling   //开启定时任务
public class SaticScheduleTaskConfig {

    @Resource
    private AppointmentService appointmentService;
    @Resource
    private UserService userService;
    @Resource
    private FloorService floorService;
    @Resource
    private RoomService roomService;
    @Resource
    private SeatService seatService;
    @Resource
    private AppointListService appointListService;

    //添加定时任务
    @Scheduled(cron = "* 0/2 * * * ?")
    public void configureTasks() throws ParseException {
        List<Appointment> list = appointmentService.list();
        for (int i = 0; i < list.size(); i++) {
            SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date nowTime = new Date();
            Appointment temp = list.get(i);
            if (temp.getStatus().equals("等待审核") || temp.getStatus().equals("等待签到") || temp.getStatus().equals("使用中")) {
                Date endTime = spf.parse(temp.getExcepttime());
                /*
                    时间到，则回收座位---->日志表修改---->状态---->使用结束
                                               ---->实际结束时间---->等于预计结束时间
                                  ---->用户表修改---->状态---->未使用
                                  ---->楼层修改---->学习人数-1
                                  ---->自习室修改---->剩余座位+1
                                  ---->座位修改---->状态---->空闲
                                  ---->预约列表修改---->删除记录
                */
                if (endTime.before(nowTime)) {
                    //修改日志表
                    temp.setEndtime(temp.getExcepttime());
                    temp.setStatus("使用结束");
                    appointmentService.updateById(temp);
                    //修改用户表
                    userService.update_ustatus(temp.getUid(), "未使用");
                    //修改楼层表
                    floorService.update_usenumber(temp.getFname(), false);
                    //修改自习室表
                    roomService.update_restseat(temp.getFname(), temp.getRname(), true);
                    //修改座位表
                    seatService.update_status(temp.getFname(), temp.getRname(), temp.getSname(), "空闲");
                    //修改预约列表
                    appointListService.deleteOne(temp.getFname(), temp.getRname(), temp.getSname(), temp.getStarttime(), temp.getExcepttime());
                }
            }
        }
    }
}
