package com.example.bsafter.controller.mafter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.User;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 功能：有关用户的sql操作
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    //查询显示所有用户
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam Integer size, @RequestParam Integer current, @RequestBody User conditions) {
        IPage<User> iPage = new Page<>(current, size);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getUname() != null && !Objects.equals(conditions.getUname(), ""), User::getUname, conditions.getUname())
                .like(conditions.getUid() != null && !Objects.equals(conditions.getUid(), ""), User::getUid, conditions.getUid())
                .eq(conditions.getUstatus() != null && !Objects.equals(conditions.getUstatus(), "") && !conditions.getUstatus().equals("全部"), User::getUstatus, conditions.getUstatus())
                .orderByDesc(User::getUname);
        IPage<User> page = userService.page(iPage, lambdaQueryWrapper);
        if (page.getRecords().size() == 0) {
            throw new ServiceException("未查找到用户!");
        }
        return Result.success(page);
    }


    //根据mid删除用户
    @DeleteMapping("/deleteUser")
    public Result deleteUser(@RequestParam String uid) {
        userService.removeById(uid);
        return Result.success();
    }


    //根据uid拉黑用户
    @PutMapping("/blockUser")
    public Result blockUser(@RequestParam String uid) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUid, uid).set(User::getUauthority, "禁止预约");
        userService.update(updateWrapper);
        return Result.success();
    }

    //根据uid释放用户
    @PutMapping("/removeBlockUser")
    public Result removeBlockUser(@RequestParam String uid) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUid, uid).set(User::getUauthority, "允许预约");
        userService.update(updateWrapper);
        return Result.success();
    }

    //根据uid修改用户信息
    @PutMapping("/updateUser")
    public Result updateUser(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    //添加用户
    @PostMapping("/addUser")
    public Result addUser(@RequestBody User user) {
        User dbuser = userService.getById(user);
        if (dbuser != null) {
            throw new ServiceException("用户账号已存在!");
        }
        userService.save(user);
        return Result.success();
    }

    //微信 余额充值
    @PutMapping("/recharge")
    public Result recharge(@RequestParam String uid, @RequestParam int rechargeNum) {
        User byId = userService.getById(uid);
        byId.setBalance(byId.getBalance() + rechargeNum);
        userService.updateById(byId);
        return Result.success(byId);
    }

    //微信 获取余额
    @GetMapping("/getBalance")
    public Result getBalance(@RequestParam String uid) {
        User byId = userService.getById(uid);
        return Result.success(byId.getBalance());
    }
}

