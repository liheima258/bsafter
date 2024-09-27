package com.example.bsafter.controller.mafter;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bsafter.common.Result;
import com.example.bsafter.entity.Floor;
import com.example.bsafter.entity.WxOptions;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.FloorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能：楼层模块
 * 日期：2024/4/816:36
 */

@Slf4j
@RestController
@RequestMapping("/floor")
public class FloorController {

    @Autowired
     FloorService floorService;

    //查询显示所有楼层
    @PostMapping("/selectAll")
    public Result selectAll(@RequestParam  Integer size, @RequestParam  Integer current,@RequestBody Floor conditions) {
        IPage<Floor> iPage = new Page<>(current,size);
        LambdaQueryWrapper<Floor> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(conditions.getFname() != null && !"".equals(conditions.getFname()), Floor :: getFname, conditions.getFname());
        IPage<Floor> page = floorService.page(iPage,lambdaQueryWrapper);
        if(page.getRecords().size()==0){
            throw new ServiceException("未查找到楼层!");
        }
        return Result.success(page);
    }

    //删除楼层
    @DeleteMapping("/deleteFloor")
    public Result deleteFloor(@RequestParam String fname){
        floorService.removeById(fname);
        return Result.success();
    }

    //新增楼层
    @PostMapping("/addFloor")
    public  Result addFloor(@RequestBody Floor newFloor){
        if (StrUtil.isBlank(newFloor.getFname()) || StrUtil.isBlank(newFloor.getFname())) {
            return Result.error("输入不能为空!");
        } else {
            Floor dbfloor = floorService.getById(newFloor);
            if (dbfloor != null){
                throw new ServiceException("楼层已存在!");
            }
            floorService.save(newFloor);
            return Result.success();
        }
    }

    //  微信  查询所有楼层，显示在下拉菜单中
    @GetMapping("/wxSelectFloor")
    public Result wxSelectFloor(){
        List<Floor> floorList = floorService.list();
        List<WxOptions> floorOptionsList = new ArrayList<>();
        for(int i = 0 ; i < floorList.size() ; i++){
            WxOptions floorOption = new WxOptions(floorList.get(i).getFname(),floorList.get(i).getFname());
            floorOptionsList.add(floorOption);
        }
        if (floorOptionsList.size() == 0) throw new ServiceException("当前无楼层");
        return Result.success(floorOptionsList);
    }

    //楼层信息修改
    @PutMapping("/updateFloor")
    public  Result updateFloor(@RequestBody Floor newFloor, @RequestParam String oldFloorName){
        if (oldFloorName.equals(newFloor.getFname())) return Result.success();
        Floor dbFloor = floorService.getById(newFloor.getFname());
        if (dbFloor != null) throw new ServiceException("修改失败!名称重复!");
        LambdaUpdateWrapper<Floor> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Floor:: getFname,oldFloorName).set(Floor::getFname,newFloor.getFname());
        floorService.update(lambdaUpdateWrapper);
        return Result.success();
    }
}

