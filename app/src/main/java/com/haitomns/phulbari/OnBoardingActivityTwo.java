package com.haitomns.phulbari;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoardingActivityTwo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding_two);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton nextButton = findViewById(R.id.OnBoardingButtonRightTwo);
        ImageButton previousButton = findViewById(R.id.OnBoardingButtonLeftTwo);

        nextButton.setOnClickListener(v -> {
            startActivity(new Intent(OnBoardingActivityTwo.this, OnBoardingActivityThree.class));
            finish();
        });

        previousButton.setOnClickListener(v -> {
            startActivity(new Intent(OnBoardingActivityTwo.this, OnBoardingActivityOne.class));
            finish();
        });
    }
}