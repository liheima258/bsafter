package com.example.bsafter.controller.mafter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Manager;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 功能：有关管理员的sql操作
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    ManagerService managerService;

    //查询显示所有管理员
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam Integer size, @RequestParam Integer current, @RequestBody Manager conditions) {
        IPage<Manager> iPage = new Page<>(current, size);
        LambdaQueryWrapper<Manager> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getMname() != null && !Objects.equals(conditions.getMname(), ""), Manager::getMname, conditions.getMname())
                .eq(conditions.getMstatus() != null && !Objects.equals(conditions.getMstatus(), "") && !conditions.getMstatus().equals("全部"), Manager::getMstatus, conditions.getMstatus())
                .orderByDesc(Manager::getMname);
        IPage<Manager> page = managerService.page(iPage, lambdaQueryWrapper);
        if (page.getRecords().size() == 0) {
            throw new ServiceException("未查找到管理员!");
        }
        return Result.success(page);
    }

    //根据mid查询管理员信息
    @GetMapping("/selectOne")
    public Result selectOne(@RequestParam String mid) {
        return Result.success(managerService.getById(mid));
    }

    //根据mid删除管理员
    @DeleteMapping("/deleteManager")
    public Result deleteManager(@RequestParam String mid) {
        managerService.removeById(mid);
        return Result.success();
    }


    //根据mid拉黑管理员
    @PutMapping("/blockManager")
    public Result blockManager(@RequestParam String mid) {
        LambdaUpdateWrapper<Manager> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Manager::getMid, mid).set(Manager::getMstatus, "禁止操作");
        managerService.update(updateWrapper);
        return Result.success();
    }

    //根据mid释放管理员
    @PutMapping("/removeBlockManager")
    public Result removeBlockManager(@RequestParam String mid) {
        LambdaUpdateWrapper<Manager> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Manager::getMid, mid).set(Manager::getMstatus, "正常");
        managerService.update(updateWrapper);
        return Result.success();
    }

    //根据mid修改管理员信息
    @PutMapping("/updateManager")
    public Result updateManager(@RequestBody Manager manager) {
        managerService.updateById(manager);
        return Result.success();
    }

    //添加管理员
    @PostMapping("/addManager")
    public Result addManager(@RequestBody Manager manager) {
        Manager dbmanager = managerService.getById(manager);
        if (dbmanager != null) {
            throw new ServiceException("管理员账号已存在!");
        }
        managerService.save(manager);
        return Result.success();
    }

    //修改管理员密码
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Manager manager) {
        String mid = manager.getMid();
        String mpassword = manager.getMpassword();
        String newPassword = manager.getNewPassword();
        if (!managerService.getById(mid).getMpassword().equals(mpassword))
            throw new ServiceException("旧密码输入错误!");
        LambdaUpdateWrapper<Manager> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Manager::getMid, mid).set(Manager::getMpassword, newPassword);
        managerService.update(lambdaUpdateWrapper);
        return Result.success();
    }
}

