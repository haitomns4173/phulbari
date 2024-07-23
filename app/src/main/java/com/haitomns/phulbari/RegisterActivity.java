package com.haitomns.phulbari;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, password, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView signInPress = findViewById(R.id.signup);

        signInPress.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        name = findViewById(R.id.signupName);

        Button register = findViewById(R.id.signupButton);

        register.setOnClickListener(v -> {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();
            String nameText = name.getText().toString();

            if (emailText.isEmpty() || passwordText.isEmpty() || nameText.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            } else if (passwordText.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User registered successfully
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Update user profile with display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nameText)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                                    SharedPreferences writeIntroSignInState = getSharedPreferences("app_state", MODE_PRIVATE);
                                    SharedPreferences.Editor introSignInWrite = writeIntroSignInState.edit();

                                    introSignInWrite.putBoolean("sign_up_state", true);
                                    introSignInWrite.apply();

                                    Intent intent = new Intent(RegisterActivity.this, HomeScreen.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Error updating profile: " + Objects.requireNonNull(profileTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
