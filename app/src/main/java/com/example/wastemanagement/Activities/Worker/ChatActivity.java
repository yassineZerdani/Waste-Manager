package com.example.wastemanagement.Activities.Worker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.wastemanagement.Adapters.ChatAdapter;
import com.example.wastemanagement.Models.ChatMessage;
import com.example.wastemanagement.Models.User;
import com.example.wastemanagement.Utilities.Constants;
import com.example.wastemanagement.Utilities.PreferenceManager;
import com.example.wastemanagement.databinding.ActivityChatBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatController;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatController = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString("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"),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatController);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, "PirGNgGPu6NM6EqLNHRx");
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.inputMessage.setText(null);
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, "PirGNgGPu6NM6EqLNHRx")
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, "PirGNgGPu6NM6EqLNHRx")
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, Comparator.comparing(obj -> obj.dateObject));
            if(count == 0){
                chatController.notifyDataSetChanged();
            }
            else {
                chatController.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadReceiverDetails(){
        binding.textName.setText("admin");
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh-mm a", Locale.getDefault()).format(date);
    }
}