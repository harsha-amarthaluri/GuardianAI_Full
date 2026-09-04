package com.guardianai.utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$");

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 8;
    }

    public static boolean isPasswordMatch(String p1, String p2) {
        if (p1 == null || p2 == null) return false;
        return p1.equals(p2);
    }

    public static boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    public static boolean isValidLongitude(double lon) {
        return lon >= -180.0 && lon <= 180.0;
    }
}
