package com.zybooks.evidencetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/*
 * File: CurrentUserAdapter.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Adapter class that connects user account data to the RecyclerView.
 * Responsible for creating and binding views for each user account.
 */

public class CurrentUserAdapter extends RecyclerView.Adapter<CurrentUserAdapter.UserViewHolder> {
    private final List<UserAccount> userList;
    private final String currentUsername;
    private final OnEditClickListener editClickListener;
    private final OnDisableClickListener disableClickListener;

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnDisableClickListener {
        void onDisableClick(int position);
    }

    public CurrentUserAdapter(List<UserAccount> userList,
                              String currentUsername,
                              OnEditClickListener editClickListener,
                              OnDisableClickListener disableClickListener) {
        this.userList = userList;
        this.currentUsername = currentUsername;
        this.editClickListener = editClickListener;
        this.disableClickListener = disableClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_current_user, parent, false);
        return new UserViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAccount user = userList.get(position);

        holder.textName.setText(user.getFirstName() + " " + user.getLastName());
        holder.textUsername.setText("Username: " + user.getUsername());
        holder.textEmail.setText("Email: " + user.getEmail());
        holder.textRole.setText("Role: " + user.getRole());

        boolean isCurrentUser =
                user.getUsername().equals(currentUsername);

        if (isCurrentUser) {

            holder.buttonDisable.setEnabled(false);
            holder.buttonDisable.setText("Current User");

        } else {

            holder.buttonDisable.setEnabled(true);
            holder.buttonDisable.setText("Disable");
        }

        holder.buttonEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(position);
            }
        });

        holder.buttonDisable.setOnClickListener(v -> {
            if (disableClickListener != null) {
                disableClickListener.onDisableClick(position);
            }
        });
    }
    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textUsername;
        TextView textEmail;
        TextView textRole;
        Button buttonEdit;
        Button buttonDisable;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRole = itemView.findViewById(R.id.textRole);

            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDisable = itemView.findViewById(R.id.buttonDisable);
        }
    }
}
