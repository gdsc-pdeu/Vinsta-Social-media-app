package com.veercreation.vinsta.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.veercreation.vinsta.MainActivity;
import com.veercreation.vinsta.databinding.ActivityLogInBinding;

import java.util.Locale;
import java.util.Objects;

public class LogInActivity extends AppCompatActivity {
    ActivityLogInBinding binding;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        binding.registerTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.loginButton.setOnClickListener(view -> logInUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser!=null){
            moveToHomeScreen();
        }
    }

    private void logInUser() {
        String email = Objects.requireNonNull(binding.emailEditText.getText()).toString();
        String password = Objects.requireNonNull(binding.passwordEditText.getText()).toString();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moveToHomeScreen();
                    } else {
                        Toast.makeText(LogInActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        String error = task.getException().getMessage().toLowerCase(Locale.ROOT);
                        if (error.contains("email")) {
                            binding.emailEditText.setError(error);
                        } else {
                            binding.passwordEditText.setError(error);
                        }
                    }
                });
    }

    private void moveToHomeScreen(){
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}