package com.example.cookshare.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ContextHelper {

    /**
     * Lấy thông tin ngày tháng hiện tại
     */
    public static String getCurrentDateInfo() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("vi-VN"));
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Lấy thứ trong tuần (Thứ 2, Thứ 3, ...)
     */
    public static String getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        String[] daysOfWeek = { "Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy" };
        return daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * Lấy giờ hiện tại
     */
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(calendar.getTime());
    }

    /**
     * Xác định buổi trong ngày
     */
    public static String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 11) {
            return "sáng";
        } else if (hour >= 11 && hour < 14) {
            return "trưa";
        } else if (hour >= 14 && hour < 18) {
            return "chiều";
        } else if (hour >= 18 && hour < 22) {
            return "tối";
        } else {
            return "đêm";
        }
    }

    /**
     * Xác định mùa trong năm (ở Việt Nam)
     */
    public static String getSeason() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based

        if (month >= 12 || month <= 2) {
            return "đông";
        } else if (month >= 3 && month <= 5) {
            return "xuân";
        } else if (month >= 6 && month <= 8) {
            return "hè";
        } else {
            return "thu";
        }
    }

    /**
     * Lấy thông tin thời tiết mô phỏng (có thể tích hợp API thật sau)
     * Ở Việt Nam thường nóng ẩm, có thể có mưa
     */
    public static String getWeatherInfo() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;

        // Mô phỏng thời tiết theo mùa ở Việt Nam
        if (month >= 12 || month <= 2) {
            return "mát mẻ, khô ráo";
        } else if (month >= 3 && month <= 5) {
            return "ấm áp, có thể có mưa phùn";
        } else if (month >= 6 && month <= 8) {
            return "nóng, ẩm, có thể có mưa nhiều";
        } else {
            return "mát mẻ, dễ chịu";
        }
    }

    /**
     * Lấy context đầy đủ để gửi cho AI
     */
    public static String getFullContext() {
        StringBuilder context = new StringBuilder();
        context.append("Thông tin hiện tại:\n");
        context.append("- Ngày: ").append(getCurrentDateInfo()).append("\n");
        context.append("- Thứ: ").append(getDayOfWeek()).append("\n");
        context.append("- Giờ: ").append(getCurrentTime()).append("\n");
        context.append("- Buổi: ").append(getTimeOfDay()).append("\n");
        context.append("- Mùa: ").append(getSeason()).append("\n");
        context.append("- Thời tiết: ").append(getWeatherInfo()).append("\n");
        return context.toString();
    }
}
