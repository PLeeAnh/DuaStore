package com.duastore.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility xử lý chuỗi tiếng Việt.
 * Bỏ dấu để tìm kiếm không phân biệt dấu (VD: "chai nuoc" → "Chai nước")
 */
public final class VietnameseUtils {

    private VietnameseUtils() {}

    /**
     * Bỏ dấu tiếng Việt khỏi chuỗi.
     * VD: "Chai nước мỹ" → "chai nuoc my"
     */
    public static String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{M}+", "")   // Bỏ marks (dấu)
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Tạo slug từ chuỗi tiếng Việt (bỏ dấu, thayspace bằng -)
     */
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return removeDiacritics(input)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
