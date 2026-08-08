package com.zybooks.evidencetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class AdminApprovalActivity extends AppCompatActivity
implements PendingUserAdapter.OnUserActionListener {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerPendingUsers;
    private TextView textNoPendingUsers;
    private PendingUserAdapter adapter;
    private List<UserAccount> pendingUsers;
    private Button buttonBack;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);

        buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        recyclerPendingUsers = findViewById(R.id.recyclerPendingUsers);

        textNoPendingUsers = findViewById(R.id.textNoPendingUsers);

        recyclerPendingUsers.setLayoutManager(
                new LinearLayoutManager(this));

        loadPendingUsers();
    }

    private void loadPendingUsers(){

        pendingUsers = dbHelper.getPendingUsers();

        if(pendingUsers.isEmpty()) {
            recyclerPendingUsers.setVisibility(View.GONE);
            textNoPendingUsers.setVisibility(View.VISIBLE);
        }
        else {
            recyclerPendingUsers.setVisibility(View.VISIBLE);
            textNoPendingUsers.setVisibility(View.GONE);

            adapter = new PendingUserAdapter(pendingUsers, this);
            recyclerPendingUsers.setAdapter(adapter);
        }
    }

    @Override
    public void onApprove(UserAccount user) {

        boolean update =
                dbHelper.updateAccountStatus(
                        user.getId(),
                        DatabaseHelper.STATUS_APPROVED);

        if (update) {
            Toast.makeText(
                    this,
                    "User approved: " + user.getUsername(),
                    Toast.LENGTH_SHORT).show();
            loadPendingUsers();
        }
        else {
            Toast.makeText(
                    this,
                    "Unable to approve account",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReject(UserAccount user) {
        boolean update =
                dbHelper.updateAccountStatus(
                        user.getId(),
                        DatabaseHelper.STATUS_REJECTED);
        if (update) {
            Toast.makeText(
                    this,
                    "User rejected: " + user.getUsername(),
                    Toast.LENGTH_SHORT).show();
            loadPendingUsers();
        }
        else {
            Toast.makeText(
                    this,
                    "Unable to reject account",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
