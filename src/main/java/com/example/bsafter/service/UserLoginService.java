package com.example.bsafter.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.User;
import com.example.bsafter.mapper.UserLoginMapper;
import org.springframework.stereotype.Service;


@Service
public class UserLoginService extends ServiceImpl<UserLoginMapper,User> {
}
