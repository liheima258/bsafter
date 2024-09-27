package com.example.bsafter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：下拉栏
 * 日期：2024/4/1416:05
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WxOptions {
    private String value;
    private String text;
}
