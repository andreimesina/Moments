package com.andreimesina.moments.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private static final String APP_KEY = "!Moments!Shared!prefs123";

    // get a String value by key
    public static String getString(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    // save a String value by key
    public static void setString(Context ctx, String key, String value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // get a boolean value by key
    public static boolean getBoolean(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    // save a boolean value by key
    public static void setBoolean(Context ctx, String key, boolean value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // get an int value by key
    public static int getInt(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    // save an int value by key
    public static void setInt(Context ctx, String key, int value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // delete a value by key
    public static void deleteValue(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    // delete all values
    public static void deleteAllValues(Context ctx) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesUtils.APP_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

