package com.andreimesina.moments.model;

public class User {

    private String userId;
    private String photoUrl;
    private String email;
    private String name;
    
    private static volatile User instance;
    
    private User() {
        
    }
    
    public static synchronized User getInstance() {
        User user = instance;

        if(user == null) {
            synchronized (User.class) {
                user = instance;

                if(user == null) {
                    instance = user = new User();
                }
            }
        }

        return user;
    }

    public void clearData() {
        userId = "";
        photoUrl = "";
        email = "";
        name = "";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
