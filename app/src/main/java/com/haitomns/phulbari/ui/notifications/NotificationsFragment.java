package com.haitomns.phulbari.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haitomns.phulbari.databinding.FragmentNotificationsBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private DatabaseReference sensorDataReference;
    private DatabaseReference wateringDataReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView sensorTextView = binding.textNotifications;
        final TextView wateringTextView = binding.wateringData; // Referencing the watering_data TextView

        notificationsViewModel.getText().observe(getViewLifecycleOwner(), sensorTextView::setText);

        // Initialize Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://phulbari-haitomns-default-rtdb.asia-southeast1.firebasedatabase.app/");
        sensorDataReference = firebaseDatabase.getReference("sensorData");
        wateringDataReference = firebaseDatabase.getReference("wateringData");
        sensorDataReference.keepSynced(true);
        wateringDataReference.keepSynced(true);

        // Fetch sensor data from Firebase
        fetchSensorData(sensorTextView);

        // Fetch watering data from Firebase
        fetchWateringData(wateringTextView);

        return root;
    }

    private void fetchSensorData(TextView sensorTextView) {
        sensorDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DataSnapshot> allRecords = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    allRecords.add(snapshot);
                }

                int recordsToShow = Math.min(allRecords.size(), 3);
                List<DataSnapshot> lastThreeRecords = allRecords.subList(allRecords.size() - recordsToShow, allRecords.size());
                Collections.reverse(lastThreeRecords);

                StringBuilder sensorData = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                for (DataSnapshot snapshot : lastThreeRecords) {
                    int soilMoisture = snapshot.child("soilMoisture").getValue(Integer.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    Date date = new Date(timestamp);
                    String formattedDate = sdf.format(date);

                    sensorData.append("• ").append(formattedDate).append("\n")
                            .append("Soil Moisture Level : ").append(soilMoisture).append("\n")
                            .append("------------------------------------------");
                    sensorData.append("\n");
                }
                sensorTextView.setText(sensorData.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sensorTextView.setText("Failed to load data: " + databaseError.getMessage());
            }
        });
    }

    private void fetchWateringData(TextView wateringTextView) {
        wateringDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DataSnapshot> allRecords = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    allRecords.add(snapshot);
                }

                int recordsToShow = Math.min(allRecords.size(), 3);
                List<DataSnapshot> lastThreeRecords = allRecords.subList(allRecords.size() - recordsToShow, allRecords.size());
                Collections.reverse(lastThreeRecords);

                StringBuilder wateringData = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                for (DataSnapshot snapshot : lastThreeRecords) {
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    int wateringDuration = snapshot.child("wateringDuration").getValue(Integer.class);

                    Date date = new Date(timestamp);
                    String formattedDate = sdf.format(date);

                    wateringData.append("• ").append(formattedDate).append("\n")
                            .append("Watering Duration : ").append(wateringDuration).append(" seconds\n")
                            .append("-----------------------------------------");
                    wateringData.append("\n");
                }
                wateringTextView.setText(wateringData.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                wateringTextView.setText("Failed to load data: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
