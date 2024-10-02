package com.haitomns.phulbari;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class FlowerDetailActivity extends AppCompatActivity {

    private TextView flowerNameTextView;
    private TextView waterRequirementTextView;
    private TextView sunlightRequirementTextView;
    private TextView soilTypeTextView;
    private TextView propagationMethodTextView;
    private ImageView flowerImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_detail);

        flowerNameTextView = findViewById(R.id.flower_name);
        waterRequirementTextView = findViewById(R.id.water_requirement);
        sunlightRequirementTextView = findViewById(R.id.sunlight_requirement);
        soilTypeTextView = findViewById(R.id.soil_type);
        propagationMethodTextView = findViewById(R.id.propagation_method);
        flowerImageView = findViewById(R.id.flower_image);

        String flowerName = getIntent().getStringExtra("flower_name");
        flowerNameTextView.setText(flowerName);

        loadFlowerDetails(flowerName);
    }

    private void loadFlowerDetails(String flowerName) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(getDatabasePath("flowers.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = null;

        try {
            cursor = db.query("flowers_data", null, "Flower = ?", new String[]{flowerName}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String waterRequirement = cursor.getString(cursor.getColumnIndex("Water Requirement"));
                String sunlightRequirement = cursor.getString(cursor.getColumnIndex("Sunlight Requirement"));
                String soilType = cursor.getString(cursor.getColumnIndex("Soil Type"));
                String propagationMethod = cursor.getString(cursor.getColumnIndex("Propagation Method"));
                String imageName = flowerName + ".jpg";

                waterRequirementTextView.setText(waterRequirement);
                sunlightRequirementTextView.setText(sunlightRequirement);
                soilTypeTextView.setText(soilType);
                propagationMethodTextView.setText(propagationMethod);

                // Load image from assets
                try {
                    InputStream inputStream = getAssets().open("flowers/" + imageName);
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    flowerImageView.setImageDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
}
