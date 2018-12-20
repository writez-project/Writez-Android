package com.writez.chat;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Message {

    private String text;
    private String id;
    private String name;
    private String photoUrl;
    private String fromId;
    private String toId;
    private String imageUrl;
    private Object timeStamp;

    public Message() {
    }

    public Message(String text, String name, String photoUrl, String fromId, String toId, String imageUrl, Object timeStamp) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.fromId = fromId;
        this.toId = toId;
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        if (text != null && text.isEmpty()) {
            return "Media";
        }
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeString() {
        if(timeStamp != null && !timeStamp.toString().isEmpty()){
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();//get your local time zone.
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(tz);//set time zone.
            String localTime="";
            try {
               localTime = sdf.format(Long.parseLong(timeStamp.toString()));
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