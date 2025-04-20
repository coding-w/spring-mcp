package com.example.wx.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 19:59
 */
public class ToolService {

    @Tool(name = "calculator", description = "简单的计算器工具")
    public String calculator(
            @ToolParam(description = "第一个数字 a") Double a,
            @ToolParam(description = "第二个数字 b") Double b,
            @ToolParam(description = "计算操作符(+ - * /)") String operation
    ) {
        double result = switch (operation) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> throw new IllegalArgumentException("未知操作");
        };
        return String.format("""
                这里是计算器工具
                %s %s %s = %s
                """, a, operation, b, result);
    }
}
