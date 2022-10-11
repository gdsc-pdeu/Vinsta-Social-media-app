package com.veercreation.vinsta.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.databinding.FragmentSearchBinding;
import com.veercreation.vinsta.databinding.UserSearchSampleBinding;
import com.veercreation.vinsta.keys.DatabaseUtilities;
import com.veercreation.vinsta.keys.NotificationTypes;
import com.veercreation.vinsta.model.FollowerModel;
import com.veercreation.vinsta.model.Notification;
import com.veercreation.vinsta.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.viewHolder> {
    ArrayList<User> list;
    Context context;


    public UserSearchAdapter(Context context, ArrayList<User> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_search_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get()
                .load(user.getProfile_picture())
                .placeholder(R.drawable.user)
                .into(holder.binding.userImage);
        holder.binding.name.setText(user.getName());

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getuserId())
                .child("followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.followButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.followed_bg));
                    holder.binding.followButton.setTextColor(context.getResources().getColor(R.color.black));
                    holder.binding.followButton.setText(R.string.following);
                    holder.binding.followButton.setEnabled(false);
                } else {
                    holder.binding.followButton.setOnClickListener(view -> {
                        FollowerModel followerModel = new FollowerModel();
                        followerModel.setFollowedBy(FirebaseAuth.getInstance().getUid());
                        followerModel.setFollowedAt(new Date().getTime());
                        FirebaseDatabase.getInstance().getReference()
                                .child("Users")
                                .child(user.getuserId())
                                .child("followers")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                .setValue(followerModel)
                                .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(user.getuserId())
                                        .child("followerCount")
                                        .setValue(user.getFollowerCount() + 1)
                                        .addOnSuccessListener(unused1 -> Toast.makeText(context, "You followed " + user.getName(), Toast.LENGTH_SHORT).show()));
                        holder.binding.followButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.followed_bg));
                        holder.binding.followButton.setTextColor(context.getResources().getColor(R.color.black));
                        holder.binding.followButton.setText(R.string.following);
                        holder.binding.followButton.setEnabled(false);

                        Notification notification = new Notification();
                        notification.setNotiAt(new Date().getTime());
                        notification.setNotiBy(FirebaseAuth.getInstance().getUid());
                        notification.setType(NotificationTypes.FOLLOW);

                        FirebaseDatabase.getInstance().getReference()
                                .child(DatabaseUtilities.NOTIFICATION)
                                .child(user.getuserId())
                                .push()
                                .setValue(notification);

                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView name, username;

        UserSearchSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UserSearchSampleBinding.bind(itemView);
        }

    }

    public void userIsFollowingAlready() {

    }
}
