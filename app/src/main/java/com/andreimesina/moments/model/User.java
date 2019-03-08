package com.andreimesina.moments.model;

public class User {
    private static Integer userId;
    private static String email;

    public static Integer getUserId() {
        return userId;
    }

    public static void setUserId(Integer newUserId) {
        userId = newUserId;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String newEmail) {
        email = newEmail;
    }
}
