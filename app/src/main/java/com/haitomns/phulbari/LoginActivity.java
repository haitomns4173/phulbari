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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView signupPress = findViewById(R.id.signup);

        signupPress.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.signinEmail);
        password = findViewById(R.id.signinPassword);

        Button login = findViewById(R.id.signinButton);
        login.setOnClickListener(v -> {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
            else {
                auth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences writeIntroSignInState = getSharedPreferences("app_state", MODE_PRIVATE);
                        SharedPreferences.Editor introSignInWrite = writeIntroSignInState.edit();

                        introSignInWrite.putBoolean("sign_in_state", true);
                        introSignInWrite.apply();

                        startActivity(new Intent(LoginActivity.this, HomeScreen.class));
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}