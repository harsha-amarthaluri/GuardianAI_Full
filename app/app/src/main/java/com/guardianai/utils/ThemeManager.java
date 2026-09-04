package com.guardianai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREF_NAME = "guardian_theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public static void applySavedTheme(Context context) {
        int savedMode = getThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    public static void setThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static int getThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, THEME_DARK); // Default to Dark mode
    }
}
