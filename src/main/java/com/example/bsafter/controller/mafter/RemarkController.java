package com.example.bsafter.controller.mafter;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Remark;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.RemarkService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 功能：留言模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/remark")
public class RemarkController {

    @Autowired
    RemarkService remarkService;

    //查询显示所有留言
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam  Integer size, @RequestParam  Integer current, @RequestBody Remark conditions) {
        IPage<Remark> iPage = new Page<>(current,size);
        LambdaQueryWrapper<Remark> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getRemarktime() != null && !conditions.getRemarktime().equals(""),Remark :: getRemarktime,conditions.getRemarktime())
                .orderByDesc(Remark :: getRemarktime);
        IPage<Remark> page = remarkService.page(iPage, lambdaQueryWrapper);
        if(page.getRecords().size()==0){
            throw new ServiceException("当前无留言,请稍后再试!");
        }
        return Result.success(page);
    }

    //根据id删除留言
    @DeleteMapping("/delete")
    public Result delete(@RequestParam int id){
        remarkService.removeById(id);
        return Result.success();
    }

    //显示某条留言
    @PostMapping("/selectOne")
    public Result selectOne(@RequestParam int id) {
        return Result.success(remarkService.getById(id));
    }

    // 微信 查询显示所有留言
    @GetMapping ("/wxSelectAll")
    public Result wxSelectAll() {
        LambdaQueryWrapper<Remark> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Remark :: getRemarktime);
        List<Remark> remarkList = remarkService.list(lambdaQueryWrapper);
        if(remarkList.size()==0){
            throw new ServiceException("当前无留言,请稍后再试!");
        }
        return Result.success(remarkList);
    }

    // 微信 提交留言
    @PostMapping("/wxSubmitRemark")
    public Result wxSubmitRemark(@RequestBody Remark remark){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowTime=new Date();
        remark.setRemarktime(spf.format(nowTime));
        remarkService.save(remark);
        return Result.success();
    }
}


