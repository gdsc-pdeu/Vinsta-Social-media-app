package com.veercreation.vinsta.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.veercreation.vinsta.activities.LogInActivity;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.adapter.FollowerAdapter;
import com.veercreation.vinsta.databinding.FragmentProfileBinding;
import com.veercreation.vinsta.model.FollowerModel;
import com.veercreation.vinsta.model.User;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    RecyclerView myFriendsRV;
    ArrayList<FollowerModel> myFriendList;
    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase database;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        myFriendsRV = binding.myFriendRV;
        myFriendList = new ArrayList<>();
        FollowerAdapter followerAdapter = new FollowerAdapter(myFriendList, getContext());
        LinearLayoutManager myFriendRvManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myFriendsRV.setLayoutManager(myFriendRvManager);
        myFriendsRV.setAdapter(followerAdapter);

        database.getReference().child("Users")
                .child(auth.getUid())
                .child("followers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            FollowerModel follow = dataSnapshot.getValue(FollowerModel.class);
                            myFriendList.add(follow);
                            Log.i("userdata" , snapshot.getValue().toString());
                        }
                        followerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editCoverPic.setOnClickListener(view1 -> selectPic(1));
        binding.profileImageInProfile.setOnClickListener(view1 -> selectPic(2));
        binding.floatingActionButton.setOnClickListener(view12 -> {
            auth.signOut();
            Intent intent = new Intent(getContext() , LogInActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        loadUserData();
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
            Uri uri = data.getData();

            Log.i("request", Integer.toString(requestCode));

            if (requestCode == 1) {
                binding.profileCover.setImageURI(uri);

                final StorageReference reference = firebaseStorage.getReference()
                        .child("cover_photo").child(FirebaseAuth.getInstance().getUid());
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(auth.getUid()).child("cover_picture").setValue(uri.toString());
                            }
                        });
                    }
                });
            } else if (requestCode == 2) {
                binding.profileImageInProfile.setImageURI(uri);
                final StorageReference reference = firebaseStorage.getReference()
                        .child("profile_photo").child(FirebaseAuth.getInstance().getUid());
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(auth.getUid()).child("profile_picture").setValue(uri.toString());
                            }
                        });
                    }
                });

            }

        }
    }

    public void loadUserData(){
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                        Picasso.get()
                                .load(user.getCover_picture())
                                .placeholder(R.drawable.user)
                                .into(binding.profileCover);
                        Log.i("results", user.getCover_picture());
                        Picasso.get()
                                .load(user.getProfile_picture())
                                .placeholder(R.drawable.user)
                                .into(binding.profileImageInProfile);
                    binding.usernameInProfile.setText(user.getName());
                    binding.followersCount.setText(user.getFollowerCount()+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}