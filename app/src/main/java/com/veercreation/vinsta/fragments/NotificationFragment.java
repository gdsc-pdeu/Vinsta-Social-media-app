package com.veercreation.vinsta.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veercreation.vinsta.adapter.NotificationAdapter;
import com.veercreation.vinsta.databinding.FragmentNotificationBinding;
import com.veercreation.vinsta.keys.DatabaseUtilities;
import com.veercreation.vinsta.model.Notification;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationFragment extends Fragment {
    FragmentNotificationBinding binding;
    RecyclerView recyclerviewOfNotification;
    ArrayList<Notification> listOfNotification;
    NotificationAdapter notificationAdapter;
    FirebaseDatabase database;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listOfNotification = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        recyclerviewOfNotification = binding.notificationRV;
        notificationAdapter = new NotificationAdapter(getContext(), listOfNotification);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerviewOfNotification.setLayoutManager(layoutManager);
        recyclerviewOfNotification.setAdapter(notificationAdapter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadNotification();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadNotification();
    }

    private void loadNotification(){
        database.getReference()
                .child(DatabaseUtilities.NOTIFICATION)
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listOfNotification.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Notification notification = dataSnapshot.getValue(Notification.class);
                            notification.setNotiID(dataSnapshot.getKey());
                            listOfNotification.add(notification);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}