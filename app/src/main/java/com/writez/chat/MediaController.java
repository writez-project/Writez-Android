package com.writez.chat;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MediaController {

    private static final String TAG = "MediaController";
    private MessageController messageController;

    public MediaController() {
        messageController = new MessageController();
    }


    public void putImageInStorage(final StorageReference storageReference, Uri uri, final String key, final User partner, final User user) {
        storageReference.putFile(uri).addOnCompleteListener(
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Task<Uri> urlTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    // Continue with the task to get the download URL
                                    return storageReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Message message = new Message(
                                                null,
                                                user.getName(),
                                                user.getPhotoUrl(),
                                                user.getUid(),
                                                partner.getUid(),
                                                downloadUri.toString(), ServerValue.TIMESTAMP);
                                        messageController.updateMessage(partner, message, user, key);
                                    } else {
                                        messageController.deleteMessage(partner,user,key);
                                    }
                                }
                            });
                        } else {
                            messageController.deleteMessage(partner,user,key);
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }

                    }
                });
    }
}
