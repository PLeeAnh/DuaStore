package com.duastore.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Lop tien ich quy doi tham so "period" (hoac from/to tuy chinh) tren cac
 * trang bao cao/dashboard thanh mot khoang ngay [from, to] cu the.
 */
public final class PeriodRangeUtil {

    public record DateRange(LocalDate from, LocalDate to) {
    }

    public static DateRange resolve(String period, String from, String to) {
        if (period != null && !period.isEmpty()) {
            LocalDate today = LocalDate.now();
            return switch (period) {
                case "today" -> new DateRange(today, today);
                case "yesterday" -> new DateRange(today.minusDays(1), today.minusDays(1));
                case "this-week" -> new DateRange(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today);
                case "last-7" -> new DateRange(today.minusDays(6), today);
                case "this-month" -> new DateRange(today.withDayOfMonth(1), today);
                case "last-month" -> {
                    LocalDate lastMonth = today.minusMonths(1);
                    yield new DateRange(lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
                }
                case "this-quarter" -> {
                    int quarter = (today.getMonthValue() - 1) / 3;
                    yield new DateRange(LocalDate.of(today.getYear(), quarter * 3 + 1, 1), today);
                }
                case "this-year" -> new DateRange(LocalDate.of(today.getYear(), 1, 1), today);
                default -> new DateRange(today.withDayOfMonth(1), today);
            };
        }
        LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : LocalDate.now();
        return new DateRange(fromDate, toDate);
    }

    private PeriodRangeUtil() {
    }
}
