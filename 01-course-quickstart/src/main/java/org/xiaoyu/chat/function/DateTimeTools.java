package org.xiaoyu.chat.function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

@Slf4j
public class DateTimeTools {

    @Tool(description = "获取用户时区中的当前日期和时间")
    String getCurrentDateTime() {
        String currentDateTime = LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        log.info("getCurrentDateTime:{}",currentDateTime);
        return currentDateTime;
    }

}