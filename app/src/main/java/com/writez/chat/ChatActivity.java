package com.writez.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        ImageView messageImageView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
            messengerImageView = itemView.findViewById(R.id.messengerImageView);
        }
    }

    private static final String LOADING_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/writez-7ee0a.appspot.com/o/Loader_Placeholder%2FLoading_icon2.gif?alt=media&token=964417a0-4e01-4410-aa31-a9048ef8e5f4";
    private static final String TAG = "ChatActivity";

    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_IMAGE = 2;


    //private GoogleApiClient mGoogleApiClient;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    private FirebaseAuth mFirebaseAuth;
    private User mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

    private MessageController messageController;
    private User partner;
    private AuthenticationController authenticationController = new AuthenticationController(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        final String uid = extras.getString("partnerId");
        final String uName = extras.getString("partnerName");
        final String targetToken = extras.getString("targetToken");

        partner = new User(
                extras.getString("partnerId"),
                extras.getString("partnerEmail"),
                extras.getString("partnerPhotoUrl"),
                extras.getString("partnerName"),
                extras.getString("targetToken")
        );


        setTitle(uName);


        //Auth
        //AuthenticationController authenticationController = new AuthenticationController(this);
        mFirebaseUser = authenticationController.getAuthUser();
        messageController = new MessageController();


        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(dataSnapshot.getKey());
                }
                return message;
            }
        };

        //DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD+"/"+privateChatIds);
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messageController.getMessageReference(partner, mFirebaseUser), parser)
                        .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message,
                MessageViewHolder>(options) {

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, int position, Message message) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);


                //hotfix - Bug with group chats
                //todo better fix for version 1.1
                if(!partner.getUid().equals("global")){
                    messageController.setUserChatRead(mFirebaseUser,partner);
                }

                if(message.getFromId().equals(mFirebaseUser.getUid())){
                    holder.itemView.setScaleX(-1);
                    holder.messageTextView.setScaleX(-1);
                    holder.messageImageView.setScaleX(-1);
                    holder.messengerTextView.setScaleX(-1);
                    holder.messengerImageView.setScaleX(-1);
                }else {
                    holder.itemView.setScaleX(1);
                    holder.messageTextView.setScaleX(1);
                    holder.messageImageView.setScaleX(1);
                    holder.messengerTextView.setScaleX(1);
                    holder.messengerImageView.setScaleX(1);
                }

                if (message.getText() != null) {
                    holder.messageTextView.setText(message.getText());
                    holder.messageTextView.setVisibility(TextView.VISIBLE);
                    holder.messageImageView.setVisibility(ImageView.GONE);
                } else if (message.getImageUrl() != null) {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(holder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(holder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(holder.messageImageView.getContext())
                                .load(message.getImageUrl())
                                .into(holder.messageImageView);
                    }
                    holder.messageImageView.setVisibility(ImageView.VISIBLE);
                    holder.messageTextView.setVisibility(TextView.GONE);
                }
                holder.messengerTextView.setText(message.getTimeString() + " " +message.getName());
                if (message.getPhotoUrl() == null) {
                    holder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ChatActivity.this)
                            .load(message.getPhotoUrl())
                            .into(holder.messengerImageView);
                }
            }

        };
        if(mFirebaseAdapter.getItemCount()<1){
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }


        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // scroll to bottom if new or on init
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });


        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(140)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new
                        Message(mMessageEditText.getText().toString(),
                        mFirebaseUser.getName(),
                        mFirebaseUser.getPhotoUrl(),
                        mFirebaseUser.getUid(),
                        uid,
                        null,
                        ServerValue.TIMESTAMP);
                messageController.sendMessage(partner, message, mFirebaseUser);
                mMessageEditText.setText("");
            }
        });
        //IMAGE
        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
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
    public void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    Message tempMessage = new Message(null, mFirebaseUser.getName(), mFirebaseUser.getPhotoUrl(), mFirebaseUser.getUid(), partner.getUid(),
                            LOADING_IMAGE_URL, ServerValue.TIMESTAMP);
                    messageController.sendMedia(partner, tempMessage, mFirebaseUser, uri);
                }
            }
        }
    }
}
