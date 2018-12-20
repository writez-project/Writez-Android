package com.writez.chat;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatOverviewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {


    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Chat, ChatOverviewActivity.ChatViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mUserRecyclerView;
    public Button mNewChatButton;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView chatImageView;
        public CircleImageView chatNewMsg;
        public TextView chatTime;
        public TextView chatName;
        public TextView chatMessage;




        public ChatViewHolder(View v) {
            super(v);
            chatName = itemView.findViewById(R.id.chatName);
            chatMessage = itemView.findViewById(R.id.chatMessage);
            chatImageView = itemView.findViewById(R.id.chatImageView);
            chatNewMsg = itemView.findViewById(R.id.chatNewMsg);
            chatTime = itemView.findViewById(R.id.chatTime);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);
        setTitle(getString(R.string.title_activity_chat_overview));

        //delete notifications
        NotificationManager notificationManager = (NotificationManager)getSystemService(this.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        // Initialize ProgressBar and RecyclerView.
        mUserRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mUserRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar =  findViewById(R.id.progressBar);
        mNewChatButton = findViewById(R.id.newChatButton);
        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mLinearLayoutManager.setReverseLayout(true);

        AuthenticationController authenticationController = new AuthenticationController(this);
        final User mUser = authenticationController.getAuthUser();

        mNewChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatOverviewActivity.this, UserOverviewActivity.class));
            }
        });

        SnapshotParser<Chat> parser = new SnapshotParser<Chat>() {
            @Override
            public Chat parseSnapshot(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.e("ChatParser", chat.getpName());
                } else {
                    Log.e("ChatParser", "Chat is Null");
                }
                return chat;
            }
        };


        DatabaseReference chatRef = mFirebaseDatabaseReference.child("user/" + mUser.getUid() + "/chats");

        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(chatRef.orderByChild("time"), parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat,
                ChatOverviewActivity.ChatViewHolder>(options) {

            @Override
            public ChatOverviewActivity.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ChatOverviewActivity.ChatViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
            }

            @Override
            protected void onBindViewHolder(final ChatOverviewActivity.ChatViewHolder holder, int position, final Chat chat) {
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    holder.chatName.setText(chat.getpName());
                    holder.itemView.setTag(chat.getpUid());
                    holder.chatTime.setText(chat.getTimeString());
                    if(chat.getMsg().isEmpty()){
                        holder.chatMessage.setText("Media");
                    }else{
                        holder.chatMessage.setText(chat.getMsg());
                    }

                    if(chat.isRead()){
                        holder.chatNewMsg.setVisibility(View.GONE);
                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(ChatOverviewActivity.this, ChatActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("partnerId", chat.getpUid());
                            extras.putString("partnerName", chat.getpName());
                            extras.putString("targetToken", chat.getpToken());
                            extras.putString("partnerEmail", chat.getpEmail());
                            extras.putString("partnerPhotoUrl", chat.getpPhotoUrl());
                            i.putExtras(extras);
                            startActivity(i);


                        }
                    });

                    if (chat.getpPhotoUrl() == null) {
                        holder.chatImageView
                                .setImageDrawable(ContextCompat
                                        .getDrawable(ChatOverviewActivity.this,
                                                R.drawable.ic_account_circle_black_36dp));
                    } else {
                        Glide.with(ChatOverviewActivity.this)
                                .load(chat.getpPhotoUrl())
                                .into(holder.chatImageView);
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sign_out_menu:
                Intent i = new Intent(this, SignInActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("logOut", true);
                i.putExtras(extras);
                startActivity(i);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
