package com.haitomns.phulbari;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoardingActivityThree extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding_three);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton nextButton = findViewById(R.id.OnBoardingButtonRightThree);
        ImageButton previousButton = findViewById(R.id.OnBoardingButtonLeftThree);

        nextButton.setOnClickListener(v -> {
            SharedPreferences writeIntroSignInState = getSharedPreferences("app_state", MODE_PRIVATE);
            SharedPreferences.Editor introSignInWrite = writeIntroSignInState.edit();

            introSignInWrite.putBoolean("intro_state", true);
            introSignInWrite.putBoolean("sign_up_state", false);
            introSignInWrite.putBoolean("sign_in_state", false);
            introSignInWrite.apply();

            startActivity(new Intent(OnBoardingActivityThree.this, RegisterActivity.class));
            finish();
        });

        previousButton.setOnClickListener(v -> {
            startActivity(new Intent(OnBoardingActivityThree.this, OnBoardingActivityTwo.class));
            finish();
        });
    }
}