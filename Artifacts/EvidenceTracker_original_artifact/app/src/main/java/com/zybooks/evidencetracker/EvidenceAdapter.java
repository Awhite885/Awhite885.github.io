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
 * File: EvidenceAdapter.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Adapter class that connects evidence data to the RecyclerView.
 * Responsible for creating and binding views for each evidence item.
 */


public class EvidenceAdapter extends RecyclerView.Adapter<EvidenceAdapter.EvidenceViewHolder> {

    // Interface for handling delete and update button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    // Interface for handling update button clicks
    public interface OnUpdateClickListener {
        void onUpdateClick(int position);
    }

    // Properties
    private final List<EvidenceItem> evidenceList;
    private final OnDeleteClickListener deleteClickListener;
    private final OnUpdateClickListener updateClickListener;

    // Constructor
    public EvidenceAdapter(List<EvidenceItem> evidenceList,
                           OnDeleteClickListener deleteClickListener,
                           OnUpdateClickListener updateClickListener) {
        this.evidenceList = evidenceList;
        this.deleteClickListener = deleteClickListener;
        this.updateClickListener = updateClickListener;
    }

    @NonNull
    // Inflate the layout for each item in the RecyclerView
    @Override
    public EvidenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evidence, parent, false);
        return new EvidenceViewHolder(view);
    }

    // Bind data to the views in each item
    @Override
    public void onBindViewHolder(@NonNull EvidenceViewHolder holder, int position) {
        EvidenceItem item = evidenceList.get(position);

        // Set the text for each view in the item
        holder.textCase.setText("Case: " + item.getCaseId());
        holder.textItem.setText("Item: " + item.getItemDescription());
        holder.textStatus.setText("Status: " + item.getStatus());
        holder.textLocation.setText("Location: " + item.getLocation());
        holder.textDateTime.setText("Date & Time: " + item.getDateTime());

        // Set click listeners for delete and update buttons
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
        holder.buttonUpdate.setOnClickListener(v -> {
            if (updateClickListener != null) {
                updateClickListener.onUpdateClick(position);
            }
        });
    }

    // Return the number of items in the list
    @Override
    public int getItemCount() {
        return evidenceList.size();
    }

    // ViewHolder class to hold references to the views in each item
    static class EvidenceViewHolder extends RecyclerView.ViewHolder {
        // UI elements
        TextView textCase;
        TextView textItem;
        TextView textStatus;
        TextView textLocation;
        TextView textDateTime;
        Button buttonDelete;
        Button buttonUpdate;

        // Constructor
        public EvidenceViewHolder(@NonNull View itemView) {
            super(itemView);
            textCase = itemView.findViewById(R.id.textCase);
            textItem = itemView.findViewById(R.id.textItem);
            textStatus = itemView.findViewById(R.id.textStatus);
            textLocation = itemView.findViewById(R.id.textLocation);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonUpdate = itemView.findViewById(R.id.buttonUpdate);
        }
    }
}