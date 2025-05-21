package org.xiaoyu.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoyu.dao.CarSaleDAO;
import org.xiaoyu.record.CarSale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>类的作用说明</p>
 *
 * @version 1.0
 * @since 2025/03/20 16:56:29
 */
@Slf4j
@Service
public class DatabaseService {
    @Autowired
    private CarSaleDAO carSaleDAO;

    @Tool(description = "查询指定月份的每日销售数据")
    public String query(@ToolParam(description = "查询的月份日期，格式：yyyy-MM") String date) {
        log.info("query:{}", date);
        // 1. 参数校验
        if (!date.matches("^\\d{4}-\\d{2}$")) {
            return "日期格式错误，应为yyyy-MM";
        }
        // 2. 构造时间范围
        LocalDate start = LocalDate.parse(date + "-01");
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        // 3. 正确使用日期字段（假设存储字段名为createTime）
        QueryWrapper<CarSale> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", startTime, endTime);
//        List<CarSale> carSaleList = carSaleDAO.selectList(wrapper);
        // 3. 构建分组统计查询（假设使用MySQL）
        wrapper.select("DATE(create_time) AS day", "COUNT(*) AS count")
                .groupBy("DATE(create_time)")
                .orderByAsc("day");
        List<Map<String, Object>> result = carSaleDAO.selectMaps(wrapper);

// 生成CSV
        StringBuilder csv = new StringBuilder();
        csv.append("日期,销售数量\n");
        for (Map<String, Object> row : result) {
            String day = row.get("day").toString();
            Long count = (Long) row.get("count");
            csv.append(day).append(',').append(count).append('\n');
        }
        return csv.toString();
    }
}
