package com.zybooks.evidencetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangePasswordActivity extends AppCompatActivity {

    // UI Elements
    private EditText editCurrentPassword;
    private EditText editNewPassword;
    private EditText editConfirmPassword;

    private Button buttonUpdatePassword;
    private Button buttonBack;

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainChangePassword),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        dbHelper = new DatabaseHelper(this);

        prefs = getSharedPreferences(
                "evidence_prefs",
                MODE_PRIVATE);

        editCurrentPassword =
                findViewById(R.id.editCurrentPassword);

        editNewPassword =
                findViewById(R.id.editNewPassword);

        editConfirmPassword =
                findViewById(R.id.editConfirmPassword);

        buttonUpdatePassword =
                findViewById(R.id.buttonUpdatePassword);

        buttonBack =
                findViewById(R.id.buttonBack);

        buttonUpdatePassword.setOnClickListener(v -> {

            String username =
                    prefs.getString(
                            "logged_in_username",
                            "");

            String currentPassword =
                    editCurrentPassword
                            .getText()
                            .toString()
                            .trim();

            String newPassword =
                    editNewPassword
                            .getText()
                            .toString()
                            .trim();

            String confirmPassword =
                    editConfirmPassword
                            .getText()
                            .toString()
                            .trim();

            if (currentPassword.isEmpty()
                    || newPassword.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(
                                this,
                                "Please complete all fields",
                                Toast.LENGTH_SHORT)
                        .show();

                return;
            }

            if (!newPassword.equals(confirmPassword)) {

                Toast.makeText(
                                this,
                                "New passwords do not match",
                                Toast.LENGTH_SHORT)
                        .show();

                return;
            }

            if (newPassword.length() < 6) {

                Toast.makeText(
                                this,
                                "Password must be at least 6 characters",
                                Toast.LENGTH_SHORT)
                        .show();

                return;
            }

            boolean updated =
                    dbHelper.updatePassword(
                            username,
                            currentPassword,
                            newPassword);

            if (updated) {

                Toast.makeText(
                                this,
                                "Password updated successfully",
                                Toast.LENGTH_SHORT)
                        .show();

                finish();

            } else {

                Toast.makeText(
                                this,
                                "Current password is incorrect",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        buttonBack.setOnClickListener(v -> finish());
    }
}