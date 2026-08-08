package com.zybooks.evidencetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PendingUserAdapter extends RecyclerView.Adapter<PendingUserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onApprove(UserAccount user);
        void onReject(UserAccount user);
    }

    private final List<UserAccount> users;
    private final OnUserActionListener listener;

    public PendingUserAdapter(
            List<UserAccount> users,
            OnUserActionListener listener) {

        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_pending_user,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        UserAccount user = users.get(position);

        holder.username.setText(
                "Username: " + user.getUsername());

        holder.role.setText(
                "Role: " + user.getRole());

        holder.requestDate.setText(
                "Requested: " + user.getRequestDate());

        holder.approve.setOnClickListener(
                v -> listener.onApprove(user));

        holder.reject.setOnClickListener(
                v -> listener.onReject(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView role;
        TextView requestDate;

        Button approve;
        Button reject;

        ViewHolder(View itemView) {
            super(itemView);

            username =
                    itemView.findViewById(R.id.textPendingUsername);

            role =
                    itemView.findViewById(R.id.textPendingRole);

            requestDate =
                    itemView.findViewById(R.id.textPendingRequestDate);

            approve =
                    itemView.findViewById(R.id.buttonApproveUser);

            reject =
                    itemView.findViewById(R.id.buttonRejectUser);
        }
    }
}