package com.example.bsafter.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bsafter.entity.Email;
import com.example.bsafter.mapper.EmailMapper;
import org.springframework.stereotype.Service;


/**
 * 功能：验证码模块
 * 日期：2024/4/719:17
 */
@Service
public class EmailService extends ServiceImpl<EmailMapper, Email> {
}
