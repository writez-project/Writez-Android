package com.writez.chat;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Chat {

    private String chatId;
    private boolean read;
    private String pPhotoUrl = "";
    private String pName = "";
    private String pUid = "";
    private String pEmail = "";
    private String pToken = "";
    private String msg = "";
    private Object time;


    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Chat(String chatId, boolean read) {
        this.chatId = chatId;
        this.read = read;
    }

    public Chat(User partner, boolean read, String chatId, Message lastMsg) {
        this.chatId = chatId;
        this.read = read;
        this.pPhotoUrl = partner.getPhotoUrl();
        this.pName = partner.getName();
        this.pEmail = partner.getEmail();
        this.pToken = partner.getToken();
        this.pUid = partner.getUid();
        this.msg = lastMsg.getText();
        this.time = lastMsg.getTimeStamp();
    }

    public String getChatId() {
        return chatId;
    }

    public boolean isRead() {
        return read;
    }

    public String getpPhotoUrl() {
        return pPhotoUrl;
    }

    public String getpName() {
        return pName;
    }

    public String getpUid() {
        return pUid;
    }

    public String getpEmail() {
        return pEmail;
    }

    public String getpToken() {
        return pToken;
    }

    public String getMsg() {
        return msg;
    }

    public Object getTime() {
        return time;
    }

    public String getTimeString() {
        if(time != null && !time.toString().isEmpty()){
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();//get your local time zone.
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(tz);//set time zone.
            String localTime="";
            try {
                localTime = sdf.format(Long.parseLong(time.toString()));
            }catch (Exception ex){
                Log.w("Failed to parse Time", ex);
                return null;
            }
            return localTime;
        }else{
            return "";
        }
    }
}
