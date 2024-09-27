package com.example.bsafter.controller.mafter;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Appointment;
import com.example.bsafter.entity.User;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


/**
 * 功能：首页和预约模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    AppointmentService appointmentService;
    @Autowired
    UserService userService;
    @Autowired
    FloorService floorService;
    @Autowired
    RoomService roomService;
    @Autowired
    SeatService seatService;
    @Autowired
    AppointListService appointListService;


    //查询显示所有预约日志
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam Integer size, @RequestParam Integer current, @RequestBody Appointment conditions) {
        IPage<Appointment> iPage = new Page<>(current, size);
        LambdaQueryWrapper<Appointment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getApplytime() != null && !conditions.getApplytime().equals(""), Appointment::getApplytime, conditions.getApplytime())
                .eq(conditions.getStatus() != null && !conditions.getStatus().equals("") && !conditions.getStatus().equals("全部"), Appointment::getStatus, conditions.getStatus())
                .orderByDesc(Appointment::getApplytime);
        IPage<Appointment> page = appointmentService.page(iPage, lambdaQueryWrapper);
        if (page.getRecords().size() == 0) {
            throw new ServiceException("当前没有预约记录,请稍后再试!");
        }
        return Result.success(page);
    }

    //根据id删除记录
    @DeleteMapping("/deleteLog")
    public Result deleteLog(@RequestParam int id) {
        appointmentService.removeById(id);
        return Result.success();
    }

    //显示所有正在预约的信息
    @PostMapping("/allRequest")
    public Result allRequest(@RequestParam Integer size, @RequestParam Integer current, @RequestBody Appointment conditions) {
        IPage<Appointment> iPage = new Page<>(current, size);
        LambdaQueryWrapper<Appointment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getUname() != null && !conditions.getUname().equals(""), Appointment::getUname, conditions.getUname())
                .eq(conditions.getUphone() != null && !conditions.getUphone().equals(""), Appointment::getUphone, conditions.getUphone())
                .eq(Appointment::getStatus, "等待审核")
                .orderByDesc(Appointment::getApplytime);
        IPage<Appointment> page = appointmentService.page(iPage, lambdaQueryWrapper);
        if (page.getRecords().size() == 0) {
            throw new ServiceException("未查找到预约记录!");
        }
        return Result.success(page);
    }

    /*
        用户仅能申请使用状态为 空闲 的座位
        用户 如果没有预约权限  || 用户状态不是未使用 || 用户余额不足 || 用户余额为0 ，则无法提交申请

       用户提交申请---->日志表修改---->用户信息、座位信息、申请时间、预订开始时间、预订结束时间、状态（等待审核）、支付金额
			    ---->用户表修改---->状态---->预约中
			                 ---->余额---->当前余额-支付金额
                ---->自习室修改---->剩余座位-1
			    ---->座位修改---->状态---->使用中
			    ---->预约列表修改---->添加座位信息、starttime、endtime
     */
    @PutMapping("/apply")
    public Result apply(@RequestBody Appointment nowApply, @RequestParam int sumPrice) throws ParseException {
        if (StrUtil.isBlank(nowApply.getUname()) || StrUtil.isBlank(nowApply.getUphone()))
            throw new ServiceException("请先完善个人信息!");
        User byId = userService.getById(nowApply.getUid());
        if (byId.getUauthority().equals("禁止预约") || !byId.getUstatus().equals("未使用"))
            throw new ServiceException("抱歉,您当前无法预约!");
        //添加记录到日志表
        nowApply.setApplytime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        nowApply.setPayment(sumPrice);
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowTime = new Date();
        String nowTimeStr = spf.format(nowTime);
        String str = nowTimeStr.substring(0, 11);
        //比较预约时间是否合法,时间不合法，则不允许预约
        String startTimeStr;
        if (nowApply.getStarttime().charAt(1) == ':') {
            startTimeStr = str + "0" + nowApply.getStarttime() + ":00";
        } else {
            startTimeStr = str + nowApply.getStarttime() + ":00";
        }
        Date startTime = spf.parse(startTimeStr);
        if (startTime.before(nowTime)) throw new ServiceException("预约时间错误!");
        //时间合法，则允许预约，存入预约日志表
        nowApply.setStarttime(startTimeStr);
        if (nowApply.getExcepttime().charAt(1) == ':') {
            nowApply.setExcepttime(str + "0" + nowApply.getExcepttime() + ":00");
        } else {
            nowApply.setExcepttime(str + nowApply.getExcepttime() + ":00");
        }
        appointmentService.save(nowApply);
        //修改用户表
        userService.update_UstatusAndBalance(nowApply.getUid(), "预约中", sumPrice, false);
        //修改自习室表
        roomService.update_restseat(nowApply.getFname(), nowApply.getRname(), false);
        //修改座位表
        seatService.update_status(nowApply.getFname(), nowApply.getRname(), nowApply.getSname(), "使用中");
        //添加记录到预约列表
        appointListService.insert(nowApply.getFname(), nowApply.getRname(), nowApply.getSname(), nowApply.getStarttime(), nowApply.getExcepttime());
        return Result.success();
    }

    /*
       审核不通过---->日志表修改---->状态---->审核不通过
                            ---->支付金额---->0
			   ---->用户表修改---->状态---->未使用
			                ---->余额---->当前余额+支付金额
               ---->自习室修改---->剩余座位+1
			   ---->座位修改---->状态---->空闲
			   ---->预约列表修改---->删除记录
     */
    @PutMapping("/refuse")
    public Result refuse(@RequestBody Appointment nowApply) {
        //获取用户当前支付金额
        int payment = appointmentService.getById(nowApply.getId()).getPayment();
        //修改状态
        appointmentService.update_status(nowApply.getId(), "审核不通过");
        //修改支付金额为0
        appointmentService.update_payment(nowApply.getId());
        //修改用户表
        userService.update_UstatusAndBalance(nowApply.getUid(), "未使用", payment, true);
        //修改自习室表
        roomService.update_restseat(nowApply.getFname(), nowApply.getRname(), true);
        //修改座位表
        seatService.update_status(nowApply.getFname(), nowApply.getRname(), nowApply.getSname(), "空闲");
        //修改预约列表
        appointListService.deleteOne(nowApply.getFname(), nowApply.getRname(), nowApply.getSname(), nowApply.getStarttime(), nowApply.getExcepttime());
        return Result.success();
    }

    /*
     审核通过之前或者签到之前用户取消预约----> 日志表修改---->状态---->用户取消预约
                                                ---->支付金额---->0
								  ---->用户表修改---->状态---->未使用
								               ---->余额---->当前余额+支付金额
                                  ---->自习室修改---->剩余座位+1
								  ---->座位修改---->状态---->空闲
								  ---->预约列表修改---->删除记录
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestParam int id, @RequestParam String uid) {
        //获取当前appointment中的对象
        Appointment appointment = appointmentService.getById(id);
        //获取用户当前支付金额
        int payment = appointment.getPayment();
        //修改状态
        appointmentService.update_status(appointment.getId(), "用户取消预约");
        //修改支付金额为0
        appointmentService.update_payment(appointment.getId());
        //修改用户表
        userService.update_UstatusAndBalance(uid, "未使用", payment, true);
        //修改自习室表
        roomService.update_restseat(appointment.getFname(), appointment.getRname(), true);
        //修改座位表
        seatService.update_status(appointment.getFname(), appointment.getRname(), appointment.getSname(), "空闲");
        //修改预约列表
        appointListService.deleteOne(appointment.getFname(), appointment.getRname(), appointment.getSname(), appointment.getStarttime(), appointment.getExcepttime());
        return Result.success();
    }


    /*
       审核通过---->日志表修改---->状态---->等待签到
            	               审核时间---->当前系统时间
			 ---->用户表修改---->状态---->待签到
     */
    @PutMapping("/allow")
    public Result allow(@RequestBody Appointment nowApply) {
        //获取当前时间
        Date nowTime = new Date();
        //修改状态
        appointmentService.update_status(nowApply.getId(), "等待签到");
        //修改审核时间
        appointmentService.update_audittime(nowApply.getId(), nowTime);
        //修改用户表
        userService.update_ustatus(nowApply.getUid(), "待签到");
        return Result.success();
    }

    /*
    用户签到后---->日志表修改---->状态---->使用中
					      ---->签到时间---->当前系统时间
		    ---->用户表修改---->状态---->使用中
            ---->楼层修改---->学习人数+1
     */

    @PutMapping("/sign")
    public Result sign(@RequestParam int id, @RequestParam String uid) throws ParseException {
        //获取当前appointment中的对象
        Appointment appointment = appointmentService.getById(id);
        //判断当前时间是否在预约开始时间之后，否则不允许签到
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowTime = new Date();
        //时间不合法，则不允许签到
        Date startTime = spf.parse(appointment.getStarttime());
        if (startTime.after(nowTime)) throw new ServiceException("当前不可签到!");
        //修改日志表状态
        appointmentService.update_status(appointment.getId(), "使用中");
        //修改签到时间
        appointmentService.update_SignTime(appointment.getId(), nowTime);
        //修改用户表
        userService.update_ustatus(uid, "使用中");
        //修改楼层表
        floorService.update_usenumber(appointment.getFname(), true);
        return Result.success();
    }

    /*
     用户提前结束---->日志表修改---->状态---->使用结束
                           ---->实际结束时间---->当前系统时间
                           ---->支付金额---->[当前系统时间-签到时间(不满一小时算作一小时)] * 座位单价
              ---->用户表修改---->状态---->未使用
                           ---->余额---->当前余额+返还金额
			  ---->楼层修改---->学习人数-1
              ---->自习室修改---->剩余座位+1
		 	  ---->座位修改---->状态---->空闲
		 	  ---->预约列表修改---->删除记录
     */
    @PutMapping("/advanceLeave")
    public Result advanceLeave(@RequestParam int id, @RequestParam String uid) throws ParseException {
        //获取当前appointment中的对象
        Appointment appointment = appointmentService.getById(id);
        int price = roomService.getRoom(appointment.getFname(), appointment.getRname()).getPrice();
        //计算使用时间
        Date time = new Date();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        String startTimeStr = appointment.getAudittime();
        String endTimeStr = localDateTimeNow.format(formatter);
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
        Duration duration = Duration.between(startTime, endTime);
        long useTime = duration.toHours() + (duration.toMinutes() % 60 == 0 ? 0 : 1);
        //计算需要返还的金额
        int useMoney = (int) (useTime * price);
        int returnMoney = appointment.getPayment() - useMoney;
        //修改状态
        appointmentService.update_status(appointment.getId(), "使用结束");
        //修改支付金额
        appointmentService.update_payment(appointment.getId(), useMoney);
        //修改实际结束时间
        appointmentService.update_endtime(appointment.getId(), time);
        //修改用户表
        userService.update_UstatusAndBalance(uid, "未使用", returnMoney, true);
        //修改楼层表
        floorService.update_usenumber(appointment.getFname(), false);
        //修改自习室表
        roomService.update_restseat(appointment.getFname(), appointment.getRname(), true);
        //修改座位表
        seatService.update_status(appointment.getFname(), appointment.getRname(), appointment.getSname(), "空闲");
        //修改预约列表
        appointListService.deleteOne(appointment.getFname(), appointment.getRname(), appointment.getSname(), appointment.getStarttime(), appointment.getExcepttime());
        return Result.success();
    }


    // 微信 查询显示当前用户的所有订单
    @GetMapping("/wxShowAll")
    public Result wxShowAll(@RequestParam String uid) {
        LambdaQueryWrapper<Appointment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Appointment::getUid, uid)
                .orderByDesc(Appointment::getApplytime);
        List<Appointment> appointmentList = appointmentService.list(lambdaQueryWrapper);
        if (appointmentList.size() == 0) throw new ServiceException("无订单记录");
        /*
            如果status==使用结束 则useTime = endTime - signtime
            如果status==等待审核 || 用户取消预约 || 审核不通过 || 等待签到 则useTime = 0
            如果status==使用中 则useTime = 当前系统时间 - signtime
         */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        for (int i = 0; i < appointmentList.size(); i++) {
            if (appointmentList.get(i).getStatus().equals("使用结束")) {
                if ("———".equals(appointmentList.get(i).getSigntime())){
                    appointmentList.get(i).setUseTime(0);
                }else{
                    String signTimeStr = appointmentList.get(i).getSigntime();
                    String endTimeStr = appointmentList.get(i).getEndtime();
                    LocalDateTime signTime = LocalDateTime.parse(signTimeStr, formatter);
                    LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
                    Duration duration = Duration.between(signTime, endTime);
                    long useTime = duration.toMinutes();
                    appointmentList.get(i).setUseTime((int) useTime);
                }
            } else if (appointmentList.get(i).getStatus().equals("使用中")) {
                String signTimeStr = appointmentList.get(i).getSigntime();
                String nowTimeStr = localDateTimeNow.format(formatter);
                LocalDateTime signTime = LocalDateTime.parse(signTimeStr, formatter);
                LocalDateTime nowTime = LocalDateTime.parse(nowTimeStr, formatter);
                Duration duration = Duration.between(signTime, nowTime);
                long useTime = duration.toMinutes();
                appointmentList.get(i).setUseTime((int) useTime);
            } else {
                appointmentList.get(i).setUseTime(0);
            }
        }
        return Result.success(appointmentList);
    }

    // 微信 查询显示当前订单的详情
    @GetMapping("/wxShowOne")
    public Result wxShowOne(@RequestParam int id) {
        Appointment byId = appointmentService.getById(id);
        /*
            如果status==使用结束 则useTime = endTime - signtime
            如果status==等待审核 || 用户取消预约 || 审核不通过 || 等待签到 则useTime = 0
            如果status==使用中 则useTime = 当前系统时间 - signtime
         */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        if (byId.getStatus().equals("使用结束")) {
            if ("———".equals(byId.getSigntime())){
                byId.setUseTime(0);
            }else{
                String signTimeStr = byId.getSigntime();
                String endTimeStr = byId.getEndtime();
                LocalDateTime signTime = LocalDateTime.parse(signTimeStr, formatter);
                LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
                Duration duration = Duration.between(signTime, endTime);
                long useTime = duration.toMinutes();
                byId.setUseTime((int) useTime);
            }
        } else if (byId.getStatus().equals("使用中")) {
            String signTimeStr = byId.getSigntime();
            String nowTimeStr = localDateTimeNow.format(formatter);
            LocalDateTime signTime = LocalDateTime.parse(signTimeStr, formatter);
            LocalDateTime nowTime = LocalDateTime.parse(nowTimeStr, formatter);
            Duration duration = Duration.between(signTime, nowTime);
            long useTime = duration.toMinutes();
            byId.setUseTime((int) useTime);
        } else {
            byId.setUseTime(0);
        }
        return Result.success(byId);
    }
}


