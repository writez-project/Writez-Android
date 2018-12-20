package com.writez.chat;

public class User {


    private String uid;
    private String email;
    private String photoUrl;
    private String name;
    private String token;

    public User() {
    }

    public User(String uid, String email, String photoUrl, String name, String token) {
        this.uid = uid;
        this.email = email;
        this.photoUrl = photoUrl;
        this.name = name;
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
