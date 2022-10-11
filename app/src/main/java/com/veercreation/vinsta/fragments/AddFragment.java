package com.veercreation.vinsta.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.databinding.FragmentAddBinding;
import com.veercreation.vinsta.model.PostModel;
import com.veercreation.vinsta.model.User;

import java.util.Date;
import java.util.Objects;

public class AddFragment extends Fragment {
    FragmentAddBinding binding;
    Uri postUri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Post Uploading");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        database.getReference().child("Users")
                .child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        Picasso.get()
                                .load(user.getProfile_picture())
                                .placeholder(R.drawable.user)
                                .into(binding.userImage);
                        binding.name.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.editTextPostDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String description = binding.editTextPostDesc.getText().toString();
                if (!description.isEmpty()) {
                    binding.postButton.setEnabled(true);
                    binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_bg));
                    binding.postButton.setTextColor(getContext().getColor(R.color.black));
                } else {
                    binding.postButton.setEnabled(false);
                    binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.followed_bg));
                    binding.postButton.setTextColor(getContext().getColor(R.color.postText));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.selectPhotoImage.setOnClickListener(view -> selectPic(1));
        binding.postButton.setOnClickListener(view -> addPostToDatabase());

        return binding.getRoot();
    }



    private void selectPic(int reqCode) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, reqCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null) {
            if (requestCode == 1) {
                postUri = data.getData();
                binding.postingImage.setImageURI(postUri);
            }
        }

    }

    void addPostToDatabase() {

        final StorageReference userPostPath = storage.getReference()
                .child("posts")
                .child(Objects.requireNonNull(auth.getUid()))
                .child(new Date().getTime() + "");

        if(postUri==null){
            Toast.makeText(requireContext() , "Photo to select kro boss" , Toast.LENGTH_SHORT).show();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.postButton.setEnabled(false);
            progressDialog.show();

            userPostPath.putFile(postUri).addOnSuccessListener(taskSnapshot -> {
                userPostPath.getDownloadUrl().addOnSuccessListener(uri -> {
                    PostModel post = new PostModel();
                    post.setPostImage(uri.toString());
                    post.setPostDesc(binding.editTextPostDesc.getText().toString());
                    post.setPostedBy(auth.getUid());
                    post.setPostedAt(String.valueOf(new Date().getTime()));
                    database.getReference().child("posts")
                            .push()
                            .setValue(post)
                            .addOnSuccessListener(unused -> {
                                binding.progressBar.setVisibility(View.INVISIBLE);
                                binding.postButton.setEnabled(true);
                                Toast.makeText(getContext(), "Posted", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });
                });
            });
        }

    }
}