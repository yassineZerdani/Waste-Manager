package com.example.wastemanagement.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wastemanagement.Listeners.WorkerListener;
import com.example.wastemanagement.Models.User;
import com.example.wastemanagement.databinding.WorkerContainerBinding;

import java.util.List;

public class WorkersAdapter extends RecyclerView.Adapter<WorkersAdapter.WorkerViewHolder> {

    private final List<User> users;
    private final WorkerListener userListener;

    public WorkersAdapter(List<User> users, WorkerListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public WorkersAdapter.WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WorkerContainerBinding usersContainerBinding = WorkerContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new WorkersAdapter.WorkerViewHolder(usersContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkersAdapter.WorkerViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {

        return users.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        WorkerContainerBinding binding;
        WorkerViewHolder(WorkerContainerBinding usersContainerBinding){
            super(usersContainerBinding.getRoot());
            binding = usersContainerBinding;
        }
        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onWorkerClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
