package com.haitomns.phulbari.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mName;
    private final MutableLiveData<String> mEmail;
    private final MutableLiveData<String> mPassword;

    public ProfileViewModel() {
        mName = new MutableLiveData<>();
        mEmail = new MutableLiveData<>();
        mPassword = new MutableLiveData<>();

        String password = "NoNotEasy";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mName.setValue(user.getDisplayName());
            mEmail.setValue(user.getEmail());
            mPassword.setValue(password);
        }
    }

    public LiveData<String> getName() {
        return mName;
    }

    public LiveData<String> getEmail() {
        return mEmail;
    }

    public LiveData<String> getPassword() {
        return mPassword;
    }
}