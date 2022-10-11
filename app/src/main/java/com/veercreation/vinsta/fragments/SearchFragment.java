package com.veercreation.vinsta.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.adapter.UserSearchAdapter;
import com.veercreation.vinsta.databinding.FragmentSearchBinding;
import com.veercreation.vinsta.model.User;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    FragmentSearchBinding binding;
    ArrayList<User> userArrayList = new ArrayList<>();
    UserSearchAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =  FragmentSearchBinding.inflate(inflater, container, false);
         adapter = new UserSearchAdapter(getContext() , userArrayList);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.usersOnVinsta.setLayoutManager(layoutManager);
        binding.usersOnVinsta.setAdapter(adapter);




        return  binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        //load userList
        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if(!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid()))
                        userArrayList.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}