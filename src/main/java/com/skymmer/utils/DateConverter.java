package com.skymmer.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateConverter {
    public static LocalDateTime convertToLocalDateTime(String dateStr) {
        // 定义日期格式（yyyy-MM-dd）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 将字符串转换为 LocalDate
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        
        // 转换为 LocalDateTime（假设时间是午夜 00:00:00）
        LocalDateTime localDateTime = localDate.atStartOfDay();

        return localDateTime;

    }

    public static Timestamp convertToTimestamp(String dateStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 将字符串转换为 LocalDate
        LocalDate localDate = LocalDate.parse(dateStr, formatter);

        // 转换为 LocalDateTime（假设时间是午夜 00:00:00）
        LocalDateTime localDateTime = localDate.atStartOfDay();

        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        return timestamp;

    }

}
