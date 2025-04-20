package com.example.wx;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 16:36
 */
@Service
public class DemoService {

    @Tool(name = "query_order", description = "查询物流信息。当用户需要根据订单号查询物流信息时，调用此工具")
    public String hello(String orderId) {
        List<Map<String, String>> trackingInfo = new ArrayList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("time", "2024-01-20 10:00:00");
        item1.put("status", "包裹已揽收");
        item1.put("location", "深圳转运中心");
        trackingInfo.add(item1);

        Map<String, String> item2 = new HashMap<>();
        item2.put("time", "2024-01-20 15:30:00");
        item2.put("status", "运输中");
        item2.put("location", "深圳市");
        trackingInfo.add(item2);

        Map<String, String> item3 = new HashMap<>();
        item3.put("time", "2024-01-21 09:00:00");
        item3.put("status", "到达目的地");
        item3.put("location", "北京市");
        trackingInfo.add(item3);

        Map<String, String> item4 = new HashMap<>();
        item4.put("time", "2024-01-21 14:00:00");
        item4.put("status", "派送中");
        item4.put("location", "北京市朝阳区");
        trackingInfo.add(item4);

        Map<String, String> item5 = new HashMap<>();
        item5.put("time", "2024-01-21 16:30:00");
        item5.put("status", "已签收");
        item5.put("location", "北京市朝阳区三里屯");
        trackingInfo.add(item5);

        // 格式化物流信息
        StringBuilder result = new StringBuilder();
        result.append("物流单号：").append(orderId).append("\n\n物流轨迹：\n");
        for (Map<String, String> item : trackingInfo) {
            result.append("[")
                    .append(item.get("time"))
                    .append("] ")
                    .append(item.get("status"))
                    .append(" - ")
                    .append(item.get("location"))
                    .append("\n");
        }

        return result.toString();
    }
}
