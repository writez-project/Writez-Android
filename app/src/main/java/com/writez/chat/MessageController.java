package com.writez.chat;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class MessageController {

    private DatabaseReference mFirebaseDatabaseReference;
    public static final String MESSAGES_CHILD = "messages";
    private static final String TAG = "MessageController";

    public MessageController() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }


    public DatabaseReference getMessageReference(User partner, User user) {
        return mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + getChatId(partner, user));
    }

    private void sendNotification(User partner, Message message, User user) {
        if (partner != null && partner.getToken()!= null &&!partner.getToken().isEmpty()) {
            Map notification = new HashMap<>();
            notification.put("username", user.getName());
            if (message.getText() != null) {
                notification.put("message", message.getText());
            } else {
                notification.put("message", "Media");
            }
            notification.put("targetToken", partner.getToken());
            mFirebaseDatabaseReference.child("notificationRequests").push().setValue(notification);
        }
    }

    public void sendMessage(User partner, Message message, User user) {
        if(!user.getUid().equals(partner.getUid())) {
            String chatId = getChatId(partner, user);
            //is media? -> prevent two notifications
            if (message.getText() != null) {
                sendNotification(partner, message, user);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + chatId).push().setValue(message);
                setUserChat(user, chatId, partner, message);
            }

        }
    }

    public void updateMessage(User partner, Message message, User user, String key) {
        if(!user.getUid().equals(partner.getUid())) {
            String chatId = getChatId(partner, user);
            sendNotification(partner, message, user);
            mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + chatId).child(key).setValue(message);
            setUserChat(user, chatId, partner, message);
        }
    }
    public void deleteMessage(User partner, User user, String key) {
        if(!user.getUid().equals(partner.getUid())) {
            String chatId = getChatId(partner, user);
            mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + chatId).child(key).removeValue();
        }
    }

    public void sendMedia(final User partner, Message message, final User user, Uri mUri) {
        if(!user.getUid().equals(partner.getUid())) {
            final Uri uri = mUri;
            mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + getChatId(partner, user)).push()
                    .setValue(message, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError,
                                               DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(user.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());
                                MediaController mediaController = new MediaController();
                                mediaController.putImageInStorage(storageReference, uri, key, partner, user);
                            } else {
                                Log.w(TAG, "Unable to write message to database.",
                                        databaseError.toException());
                            }
                        }
                    });
        }
    }

    private void setUserChat(User user, String chatId, User partner, Message lastMessage) {
        if (!user.getUid().equals(partner.getUid())) {
            mFirebaseDatabaseReference.child("user/" + user.getUid() + "/chats/" + chatId).setValue(new Chat(partner, true, chatId, lastMessage));
            mFirebaseDatabaseReference.child("user/" + partner.getUid() + "/chats/" + chatId).setValue(new Chat(user, false, chatId, lastMessage));
        }
    }
    public void setUserChatRead(User user, User partner) {
        if(!user.getUid().equals(partner.getUid())){
            mFirebaseDatabaseReference.child("user/" + user.getUid() + "/chats/" + getChatId(partner, user)+"/read").setValue(true);
        }
    }

    private String getChatId(User partner, User user) {
        final String chatId;
        if (partner.getUid().contentEquals("global")) {
            chatId = "global";
        } else {
            int compare = partner.getUid().compareTo(user.getUid());
            if (compare < 0) {
                //uid is smaller
                chatId = partner.getUid() + "_" + user.getUid();
            } else {
                if (compare > 0) {
                    //uid is larger
                    chatId = user.getUid() + "_" + partner.getUid();
                } else {
                    //uid is equal to getuid
                    chatId = user.getUid() + "_" + partner.getUid();
                }
            }
        }
        return chatId;
    }
}
