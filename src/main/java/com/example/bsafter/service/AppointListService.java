package com.example.bsafter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.AppointList;
import com.example.bsafter.mapper.AppointListMapper;
import org.springframework.stereotype.Service;


/**
 * 功能：预约列表
 */
@Service
public class AppointListService extends ServiceImpl<AppointListMapper, AppointList> {

    public void insert(String fname, String rname, String sname, String starttime, String excepttime) {
        AppointList temp = new AppointList();
        int start, end;
        if (starttime.charAt(11) == '0') {
            start = Integer.parseInt(starttime.substring(12, 13));
        } else {
            start = Integer.parseInt(starttime.substring(11, 13));
        }
        if (excepttime.charAt(11) == '0') {
            end = Integer.parseInt(excepttime.substring(12, 13));
        } else {
            end = Integer.parseInt(excepttime.substring(11, 13));
        }
        temp.setFname(fname);
        temp.setRname(rname);
        temp.setSname(sname);
        temp.setStarttime(start);
        temp.setEndtime(end);
        this.save(temp);
    }

    public void deleteOne(String fname, String rname, String sname, String starttime, String excepttime) {
        int start, end;
        if (starttime.charAt(11) == '0') {
            start = Integer.parseInt(starttime.substring(12, 13));
        } else {
            start = Integer.parseInt(starttime.substring(11, 13));
        }
        if (excepttime.charAt(11) == '0') {
            end = Integer.parseInt(excepttime.substring(12, 13));
        } else {
            end = Integer.parseInt(excepttime.substring(11, 13));
        }
        LambdaQueryWrapper<AppointList> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppointList::getFname,fname)
                .eq(AppointList::getRname,rname)
                .eq(AppointList::getSname,sname)
                .eq(AppointList::getStarttime,start)
                .eq(AppointList::getEndtime,end);
        this.remove(lambdaQueryWrapper);
    }
}
