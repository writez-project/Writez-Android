package com.writez.chat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserController {

    private DatabaseReference mFirebaseDatabaseReference;

    public UserController() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void setUserData(String uid, String key, String value) {
        mFirebaseDatabaseReference.child("user/" + uid + "/" + key).setValue(value);
    }

    public void setUser(User user) {
        mFirebaseDatabaseReference.child("user/" + user.getUid()).setValue(user);
    }
    /*
    public void setUserChat(User user, String chatId, User partner, Message lastMessage) {
        mFirebaseDatabaseReference.child("user/" + user.getUid() + "/chats/" + chatId).setValue(new Chat(partner, true, chatId, lastMessage));
        mFirebaseDatabaseReference.child("user/" + partner.getUid() + "/chats/" + chatId).setValue(new Chat(user, false, chatId, lastMessage));
    }*/
}
