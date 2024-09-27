package com.example.bsafter.controller.mafter;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Email;
import com.example.bsafter.entity.Manager;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.EmailService;
import com.example.bsafter.service.ManagerLoginService;
import com.example.bsafter.service.ManagerService;
import com.example.bsafter.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 功能：登录和注册验证
 * 日期：2024/4/718:52
 */
@Slf4j
@RestController
public class LoginController {
    @Autowired
    ManagerLoginService managerLoginService;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    EmailService emailService;
    @Autowired
    ManagerService managerService;
    @Value("${spring.mail.username}")
    private String from;



    @PostMapping("/login")
    public Result login(@RequestBody Manager manager){
        Manager dbManager= managerLoginService.getById(manager);
        if(dbManager == null){
            throw new ServiceException("账号不存在!");
        }
        if(!manager.getMpassword().equals(dbManager.getMpassword())){
            throw new ServiceException("密码错误!");
        }
        //生成token
        String token= TokenUtils.createToken(manager.getMid(),manager.getMpassword(),"admin");
        dbManager.setToken(token);
        return Result.success(dbManager);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Manager manager){
        Manager dbManager= managerLoginService.getById(manager);
        if(dbManager != null){
            throw new ServiceException("账号已存在!");
        }
        managerLoginService.save(manager);
        return Result.success();
    }

    //忘记密码--->发送验证码
    @GetMapping("/sendEmailCode")
    public Result sendEmailCode(@RequestParam String memail,@RequestParam String mid){
        if (StrUtil.isBlank(mid) || StrUtil.isBlank(memail)) throw new ServiceException("请输入账号和邮箱!");
        //根据mid查找管理员表
        Manager byId = managerService.getById(mid);
        //如果管理员表里无此mid，则说明输错了或者此mid没有注册
        if (byId == null) throw new ServiceException("账号输入错误!");
        //管理员的邮箱输错了
        if (!memail.equals(byId.getMemail())) throw new ServiceException("邮箱输入错误!");
        //账号和邮箱均输入无误，则准备发验证码
        LambdaQueryWrapper<Email> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Email :: getEmail,memail)
                .eq(Email :: getType,1);
        Email one = emailService.getOne(lambdaQueryWrapper);
        //如果表中有  此邮箱的  忘记密码类型  的验证码
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        String nowTimeStr = localDateTimeNow.format(formatter);
        if (one != null){
            String timeStr = one.getTime();
            LocalDateTime time = LocalDateTime.parse(timeStr, formatter);
            Duration duration = Duration.between(time, localDateTimeNow);
            //如果验证码在有效期内，则不会发送验证码
            if (duration.toMinutes() < 5){
                throw new ServiceException("当前已发送验证码,请查看邮箱!");
            }else{
                //如果验证码不在有效期内，则删除表中的此条记录，重现发送新的验证码(保证表中只有一条对应类型的验证码有效)
                emailService.removeById(one.getId());
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);  //发送人
        message.setTo(memail);   //接收人
        message.setSubject("【自习室预约系统】验证码");  //邮件主题
        message.setSentDate(new Date());   //邮件发送时间
        String code = RandomUtil.randomNumbers(4);  //随机生成一个4长度的验证码
        String context = "<b>尊敬的管理员：</b><br><br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，" +
                "自习室预约系统提醒您，本次的验证码是："+code+"，请妥善保管，切勿泄露，有效期五分钟";
        message.setText(context);  //邮件内容
        javaMailSender.send(message);
        //发送成功之后，把验证码存入数据库
        Email newCode = new Email();
        newCode.setEmail(memail);
        newCode.setCode(code);
        newCode.setType(1);
        newCode.setTime(nowTimeStr);
        emailService.save(newCode);
        return Result.success();
    }

    //忘记密码--->重置密码
    @PutMapping("/reset")
    public Result reset(@RequestParam String code,@RequestBody Manager manager){
        LambdaQueryWrapper<Email> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Email :: getEmail,manager.getMemail())
                .eq(Email :: getType,1);
        Email one = emailService.getOne(lambdaQueryWrapper);
        if (!code.equals(one.getCode())) throw new ServiceException("验证码错误!");
        //验证码正确，则可以修改密码
        String mid = manager.getMid();
        String newPassword = manager.getNewPassword();
        LambdaUpdateWrapper<Manager> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Manager :: getMid,mid).set(Manager :: getMpassword,newPassword);
        managerService.update(lambdaUpdateWrapper);
        return Result.success();
    }
}