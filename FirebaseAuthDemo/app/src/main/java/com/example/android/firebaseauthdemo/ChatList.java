package com.example.android.firebaseauthdemo;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;


public class ChatList extends ArrayAdapter<Chat>{

    private Activity context;
    private List<Chat> chatList;
    String userID;
    String imgUrl;

    public ChatList(Activity context, List<Chat> chatList){
        super(context, R.layout.chat_list_layout, chatList);
        this.context = context;
        this.chatList= chatList;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewChat = inflater.inflate(R.layout.chat_list_layout, null, true);

        Chat chat = chatList.get(position);
        final CircularImageView imageViewProfilePic = (CircularImageView) listViewChat.findViewById(R.id.profilePicture);
        final ImageView imageViewPlaceholder = (ImageView) listViewChat.findViewById(R.id.placeHolder);
        final ImageView chatImg = (ImageView) listViewChat.findViewById(R.id.chatImg);

        //User Info
        userID = chat.getUid();
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imgUrl = dataSnapshot.child("profilepic").getValue(String.class);
                if(imgUrl != null && getContext()!= null){
                    imageViewPlaceholder.setVisibility(View.INVISIBLE);
                    Glide
                            .with(context.getApplicationContext())
                            .load(imgUrl)
                            .transform(new CircleTransform(context))
                            .into(imageViewProfilePic);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        TextView textViewUserName = (TextView) listViewChat.findViewById(R.id.textViewUserName);
        TextView textViewMessage = (TextView) listViewChat.findViewById(R.id.textViewMessage);
        TextView textViewDate = (TextView) listViewChat.findViewById(R.id.textViewDate);
        TextView textViewTime = (TextView) listViewChat.findViewById(R.id.textViewTime);
        String chatMsg = chat.getMsg();
        String strCheck = "null";
        if(chatMsg.length() > 23){
            strCheck = chatMsg.substring(0, Math.min(chat.getMsg().length(), 23));
        }

        textViewUserName.setText(chat.getName());
        if(strCheck.equals("https://firebasestorage")){
            textViewMessage.setVisibility(View.INVISIBLE);
            Glide
                    .with(context.getApplicationContext())
                    .load(chatMsg)
                    .into(chatImg);
        } else {
            textViewMessage.setText(chat.getMsg());
        }
        textViewDate.setText(chat.getDate());
        textViewTime.setText(chat.getTime());

        return listViewChat;
    }


}
