package com.example.bsafter.controller.mafter;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Notice;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 功能：公告模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    //查询显示所有公告
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam  Integer size, @RequestParam  Integer current,@RequestBody Notice conditions) {
        IPage<Notice> iPage = new Page<>(current,size);
        LambdaQueryWrapper<Notice> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getOutline() != null && !"".equals(conditions.getOutline()), Notice :: getOutline, conditions.getOutline());
        IPage<Notice> page = noticeService.page(iPage,lambdaQueryWrapper);
        if(page.getRecords().size()==0){
            throw new ServiceException("未查找到公告!");
        }
        return Result.success(page);
    }

    //删除公告
    @DeleteMapping("/deleteNotice")
    public Result deleteNotice(@RequestParam int id){
       noticeService.removeById(id);
       return Result.success();
    }

    //新增公告
    @PostMapping("/addNotice")
    public  Result addNotice(@RequestBody Notice newNotice){
        if (StrUtil.isBlank(newNotice.getOutline() )|| StrUtil.isBlank(newNotice.getTitle()) || StrUtil.isBlank(newNotice.getContent())) {
            return Result.error("输入不能为空!");
        } else {
            noticeService.save(newNotice);
        }
        return Result.success();
    }

    //公告修改
    @PutMapping("/updateNotice")
    public  Result updateNotice(@RequestBody Notice newNotice){
        noticeService.updateById(newNotice);
        return Result.success();
    }

    // 微信 显示所有公告
    @GetMapping("/wxShowAll")
    public Result wxShowAll() {
        return Result.success(noticeService.list());
    }
}

