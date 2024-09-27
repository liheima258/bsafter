package com.example.bsafter.controller.uafter;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Email;
import com.example.bsafter.entity.User;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.EmailService;
import com.example.bsafter.service.UserLoginService;
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
 * 功能：有关用户的sql操作
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/wx")
public class UserLoginController {

    @Autowired
    UserLoginService userLoginService;
    @Autowired
    EmailService emailService;
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    JavaMailSender javaMailSender;

    //用户登录
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User dbUser = userLoginService.getById(user);
        if (dbUser == null) {
            throw new ServiceException("账号不存在!");
        }
        if (!user.getUpassword().equals(dbUser.getUpassword())) {
            throw new ServiceException("密码错误!");
        }
        //生成token
        String token= TokenUtils.createToken(user.getUid(),user.getUpassword(),"user");
        dbUser.setToken(token);
        return Result.success(dbUser);
    }

    //用户注册
    @PostMapping("/register")
    public Result register(@RequestBody User user){
        User dbManager= userLoginService.getById(user);
        if(dbManager != null){
            throw new ServiceException("账号已存在!");
        }
        userLoginService.save(user);
        return Result.success();
    }

    //修改密码
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestParam String uid,@RequestBody User user){
        String upassword = user.getUpassword();
        String newPassword = user.getNewPassword();
        if ( !userLoginService.getById(uid).getUpassword().equals(upassword)) throw new ServiceException("旧密码输入错误!");
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User :: getUid,uid).set(User :: getUpassword,newPassword);
        userLoginService.update(lambdaUpdateWrapper);
        return Result.success();
    }

    //忘记密码--->发送验证码
    @GetMapping("/sendEmailCode")
    public Result sendEmailCode(@RequestParam String uemail,@RequestParam String uid){
        //根据uid查找用户表
        User byId = userLoginService.getById(uid);
        //如果用户表里无此uid，则说明输错了或者此uid没有注册
        if (byId == null) throw new ServiceException("账号错误!");
        //用户的邮箱输错了
        if (!uemail.equals(byId.getUemail())) throw new ServiceException("邮箱错误!");
        //账号和邮箱均输入无误，则准备发验证码
        LambdaQueryWrapper<Email> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Email :: getEmail,uemail)
                .eq(Email :: getType,2);
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
                throw new ServiceException("验证码已发送!");
            }else{
                //如果验证码不在有效期内，则删除表中的此条记录，重现发送新的验证码(保证表中只有一条对应类型的验证码有效)
                emailService.removeById(one.getId());
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);  //发送人
        message.setTo(uemail);   //接收人
        message.setSubject("【自习室预约系统】验证码");  //邮件主题
        message.setSentDate(new Date());   //邮件发送时间
        String code = RandomUtil.randomNumbers(4);  //随机生成一个4长度的验证码
        String context = "<b>尊敬的用户：</b><br><br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，" +
                "自习室预约系统提醒您，本次的验证码是："+code+"，请妥善保管，切勿泄露，有效期五分钟";
        message.setText(context);  //邮件内容
        javaMailSender.send(message);
        //发送成功之后，把验证码存入数据库
        Email newCode = new Email();
        newCode.setEmail(uemail);
        newCode.setCode(code);
        newCode.setType(2);
        newCode.setTime(nowTimeStr);
        emailService.save(newCode);
        return Result.success();
    }

    //忘记密码--->重置密码
    @PutMapping("/reset")
    public Result reset(@RequestParam String code,@RequestBody User user){
        LambdaQueryWrapper<Email> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Email :: getEmail,user.getUemail())
                .eq(Email :: getType,2);
        Email one = emailService.getOne(lambdaQueryWrapper);
        if (!code.equals(one.getCode())) throw new ServiceException("验证码错误!");
        //验证码正确，则可以修改密码
        String uid = user.getUid();
        String newPassword = user.getNewPassword();
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User :: getUid,uid).set(User :: getUpassword,newPassword);
        userLoginService.update(lambdaUpdateWrapper);
        return Result.success();
    }
}

