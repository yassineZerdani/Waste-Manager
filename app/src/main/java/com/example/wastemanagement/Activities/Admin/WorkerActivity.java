package com.example.wastemanagement.Activities.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.wastemanagement.Listeners.WorkerListener;
import com.example.wastemanagement.Models.User;
import com.example.wastemanagement.R;
import com.example.wastemanagement.Utilities.Constants;
import com.example.wastemanagement.Utilities.PreferenceManager;
import com.example.wastemanagement.databinding.ActivityWorkerBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerActivity extends AppCompatActivity implements WorkerListener {

    private ActivityWorkerBinding binding;
    private User user;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        loadUserDetails();
        binding.update.setOnClickListener(v -> {
            user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
            onWorkerClicked(user);
        });
        binding.sendMail.setOnClickListener( v -> sendEmail());
        binding.sendSMS.setOnClickListener( v -> sendSMS());
        binding.sendCall.setOnClickListener( v -> sendCall());
        binding.delete.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).delete();
            Intent intent = new Intent(getApplicationContext(), WorkersActivity.class);
            startActivity(intent);
            finish();
        });

    }



    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    private void loadUserDetails(){
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.imageService.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.textName.setText(user.name);

    }


    private void getUsers(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {

                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            if(binding.textAuthor.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_USER_NAME);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_USER_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                    }
                });
    }

    public void sendSMS(){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , new String("0984768:738478"));
        smsIntent.putExtra("sms_body"  , "uilhiuh");
    }

    public void sendCall(){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+"8802177690"));//change the number
    }



    public void sendEmail(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"yassinezerdani.gd@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Objet...");
        i.putExtra(Intent.EXTRA_TEXT   , "Message...");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWorkerClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), UpdateWorkerActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

}