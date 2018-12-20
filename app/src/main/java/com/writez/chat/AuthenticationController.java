package com.writez.chat;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;


public class AuthenticationController {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private User mUser;
    private boolean loggedIn = false;


    public AuthenticationController(Context context) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Intent intent = new Intent();
            intent.setClass(context, SignInActivity.class);
            context.startActivity(intent);

        } else {
            loggedIn = true;
            mUser = new User(
                    mFirebaseUser.getUid(),
                    mFirebaseUser.getEmail(),
                    mFirebaseUser.getPhotoUrl().toString(),
                    mFirebaseUser.getDisplayName(),
                    FirebaseInstanceId.getInstance().getToken());

        }

    }

    public User getAuthUser() {
        return mUser;
    }

    //Der benutzer muss beim Anmelden geupdated werden damit der Token von einem evt neuen gerät übernommen wird.
    public void updateUser() {
        UserController userController = new UserController();
        userController.setUserData(mUser.getUid(), "email", mUser.getEmail());
        userController.setUserData(mUser.getUid(), "name", mUser.getName());
        userController.setUserData(mUser.getUid(), "token", mUser.getToken());
        userController.setUserData(mUser.getUid(), "photoUrl", mUser.getPhotoUrl());
    }

    public void signOut() {
        mFirebaseAuth.signOut();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
