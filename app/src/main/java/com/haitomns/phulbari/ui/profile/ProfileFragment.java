package com.haitomns.phulbari.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.haitomns.phulbari.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private EditText nameEditText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        auth = FirebaseAuth.getInstance();

        final TextView textName = binding.textName;
        final TextView textEmail = binding.textEmail;
        final TextView textPassword = binding.textPassword;

        nameEditText = binding.textName;

        Button doneButton = binding.updateButton;

        profileViewModel.getName().observe(getViewLifecycleOwner(), textName::setText);
        profileViewModel.getEmail().observe(getViewLifecycleOwner(), textEmail::setText);
        profileViewModel.getPassword().observe(getViewLifecycleOwner(), textPassword::setText);

        doneButton.setOnClickListener(v -> {
            if (nameEditText != null) {
                String newName = nameEditText.getText().toString();

                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    if (!newName.isEmpty()) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Profile update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(getActivity(), "One or more fields are empty.", Toast.LENGTH_SHORT).show();
            }
        });
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}