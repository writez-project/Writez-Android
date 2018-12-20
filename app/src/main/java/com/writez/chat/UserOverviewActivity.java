package com.writez.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserOverviewActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<User, UserViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mUserRecyclerView;


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userImageView;
        public TextView userText;


        public UserViewHolder(View v) {
            super(v);

            userText = itemView.findViewById(R.id.userText);
            userImageView = itemView.findViewById(R.id.userImageView);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_overview);

        // Initialize ProgressBar and RecyclerView.
        mUserRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mUserRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mLinearLayoutManager.setReverseLayout(true);

        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        AuthenticationController authenticationController = new AuthenticationController(this);


        final User mUser = authenticationController.getAuthUser();
        Log.e("ize me", mUser.getEmail());


        final SnapshotParser<User> parser = new SnapshotParser<User>() {
            @Override
            public User parseSnapshot(DataSnapshot dataSnapshot) {
                User usr = dataSnapshot.getValue(User.class);
                if (usr != null) {
                    Log.e("UserParser", usr.getName());
                } else {
                    Log.e("UserParser", "User is Null");
                }
                return usr;
            }
        };


        DatabaseReference usrRef = mFirebaseDatabaseReference.child("user");
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(usrRef, parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<User,
                UserViewHolder>(options) {

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new UserViewHolder(inflater.inflate(R.layout.item_user, parent, false));
            }

            @Override
            protected void onBindViewHolder(final UserViewHolder holder, int position, User user) {
                holder.userImageView.setVisibility(View.VISIBLE);
                holder.userText.setVisibility(View.VISIBLE);
                holder.itemView.setVisibility(View.VISIBLE);
                if (!user.getUid().matches(mUser.getUid())) {
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    holder.userText.setText(user.getName());
                    holder.itemView.setTag(user.getUid());
                    final User fUser = user;
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Intent i = new Intent(UserOverviewActivity.this, privateChatActivity.class);
                            //i.putExtra("user",friendlyUser.getUid()); //Your id
                            Intent i = new Intent(UserOverviewActivity.this, ChatActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("partnerId", fUser.getUid());
                            extras.putString("partnerName", fUser.getName());
                            extras.putString("targetToken", fUser.getToken());
                            extras.putString("partnerEmail", fUser.getEmail());
                            extras.putString("partnerPhotoUrl", fUser.getPhotoUrl());
                            i.putExtras(extras);
                            startActivity(i);


                        }
                    });
                    if (user.getPhotoUrl() == null) {
                        holder.userImageView
                                .setImageDrawable(ContextCompat
                                        .getDrawable(UserOverviewActivity.this,
                                                R.drawable.ic_account_circle_black_36dp));
                    } else {
                        Glide.with(UserOverviewActivity.this)
                                .load(user.getPhotoUrl())
                                .into(holder.userImageView);
                    }
                } else {
                    Log.e("UserDisplay", user.getName()+ " "+mUser.getName());
                    holder.userImageView.setVisibility(View.GONE);
                    holder.userText.setVisibility(View.GONE);
                    holder.itemView.setVisibility(View.GONE);

                }

            }

        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int count = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (count - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mUserRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mUserRecyclerView.setLayoutManager(mLinearLayoutManager);
        mUserRecyclerView.setAdapter(mFirebaseAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
        mFirebaseAdapter.notifyDataSetChanged();
    }


}
