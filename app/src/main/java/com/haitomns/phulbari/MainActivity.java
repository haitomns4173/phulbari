package com.haitomns.phulbari;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static final String PREFS_NAME = "SplashPrefs";
    private static final String IMAGE_INDEX_KEY = "imageIndex";
    private static final int SPLASH_DISPLAY_LENGTH = 3000;

    private final int[] splashBackgroundImages = {R.drawable.splash_image_1, R.drawable.splash_image_2, R.drawable.splash_image_3, R.drawable.splash_image_4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView splashImage = findViewById(R.id.splash_image);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int imageIndex = preferences.getInt(IMAGE_INDEX_KEY, 0);

        splashImage.setImageResource(splashBackgroundImages[imageIndex]);

        imageIndex = (imageIndex + 1) % splashBackgroundImages.length;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(IMAGE_INDEX_KEY, imageIndex);
        editor.apply();

        SharedPreferences getIntroSignInState = getSharedPreferences("app_state", MODE_PRIVATE);
        boolean introTest = getIntroSignInState.getBoolean("intro_state", false);
        boolean signUpState = getIntroSignInState.getBoolean("sign_up_state", false);
        boolean signInTest = getIntroSignInState.getBoolean("sign_in_state", false);

        new Handler().postDelayed(() -> {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(MainActivity.this, HomeScreen.class));
            }
            else{
                if(introTest){
                    if(signUpState){
                        if(signInTest){
                            startActivity(new Intent(MainActivity.this, HomeScreen.class));
                        }
                        else{
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                    }
                    else{
                        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                    }
                }
                else{
                    startActivity(new Intent(MainActivity.this, OnBoardingActivityOne.class));
                }
            }
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}