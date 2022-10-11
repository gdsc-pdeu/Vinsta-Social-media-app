package com.veercreation.vinsta.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.veercreation.vinsta.MainActivity;
import com.veercreation.vinsta.databinding.ActivitySignUpBinding;
import com.veercreation.vinsta.model.User;
import java.util.Locale;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding activitySignUpBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(activitySignUpBinding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        activitySignUpBinding.loginTextView.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });
        activitySignUpBinding.signupButton.setOnClickListener(view -> {
            signUpUser();
        });
    }

    private void signUpUser() {
        String email = Objects.requireNonNull(activitySignUpBinding.emailEditTextSignup.getText()).toString();
        String password = Objects.requireNonNull(activitySignUpBinding.passwordEditTextSignup.getText()).toString();
        String name = Objects.requireNonNull(activitySignUpBinding.nameEditText.getText()).toString();
        String username = Objects.requireNonNull(activitySignUpBinding.usernameEditText.getText()).toString();
        String dummyCoverPic = "https://firebasestorage.googleapis.com/v0/b/vinsta-d08d4.appspot.com/o/cover_photo%2Fsample_cover.png?alt=media&token=efe4fa24-9133-4d1e-9c7c-96d076ba0898";
        String dummyProfilePic = "https://firebasestorage.googleapis.com/v0/b/vinsta-d08d4.appspot.com/o/cover_photo%2Fuser.png?alt=media&token=e6f76621-420f-4ab8-898d-4b17b10eb511";

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(name , email , password , dummyCoverPic , dummyProfilePic);
                        user.setuserId(auth.getUid());
                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        database.getReference().child("Users").child(id).setValue(user);
                        Toast.makeText(SignUpActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this , MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        String error = task.getException().getMessage().toLowerCase(Locale.ROOT);
                        if(error.contains("email")){
                            activitySignUpBinding.emailEditTextSignup.setError(error);
                        }
                        else {
                            activitySignUpBinding.nameEditText.setError(error);
                        }
                    }
                });
    }
}