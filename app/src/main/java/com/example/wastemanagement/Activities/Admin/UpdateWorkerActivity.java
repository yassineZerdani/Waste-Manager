package com.example.wastemanagement.Activities.Admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.wastemanagement.Models.User;
import com.example.wastemanagement.Utilities.Constants;
import com.example.wastemanagement.Utilities.PreferenceManager;
import com.example.wastemanagement.databinding.ActivityAddWorkerBinding;
import com.example.wastemanagement.databinding.ActivityUpdateWorkerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class UpdateWorkerActivity extends AppCompatActivity {

    private ActivityUpdateWorkerBinding binding;
    private String encodedImage;
    private User user;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        setListeners();
    }

    private void setListeners(){
        binding.addProf.setOnClickListener(v -> {
            if(isValidUserDetails()){
                updateUser();
                startActivity(new Intent(this, WorkersActivity.class));
            }
            binding.textAddImage.setOnClickListener(d -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            });
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadUserDetails(){
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.imageService.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.nomProf.setText(user.name);
        binding.emailProf.setText(user.email);
        binding.passwordProf.setText(user.password);
    }



    private void updateUser(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS).document(user.id);

        userRef.update(Constants.KEY_USER_EMAIL, binding.emailProf.getText().toString());
        userRef.update(Constants.KEY_USER_NAME, binding.nomProf.getText().toString());
        userRef.update(Constants.KEY_USER_PASSWORD, binding.passwordProf.getText().toString());
        userRef.update(Constants.KEY_USER_IMAGE, encodedImage);

    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageService.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch(FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidUserDetails(){
        if(encodedImage == null){
            showToast("Select Service Image");
            return false;
        }
        else if(binding.nomProf.getText().toString().trim().isEmpty()){
            showToast("Enter Service Name");
            return false;
        }
        else if(binding.emailProf.getText().toString().trim().isEmpty()){
            showToast("Enter Service Address");
            return false;
        }
        else {
            return true;
        }
    }
}