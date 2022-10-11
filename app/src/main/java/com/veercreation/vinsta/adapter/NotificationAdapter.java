package com.veercreation.vinsta.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.activities.CommentActivity;
import com.veercreation.vinsta.databinding.NotificationRvDesignBinding;
import com.veercreation.vinsta.global.Function;
import com.veercreation.vinsta.keys.DatabaseUtilities;
import com.veercreation.vinsta.keys.NotificationTypes;
import com.veercreation.vinsta.model.Notification;
import com.veercreation.vinsta.model.User;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {
    ArrayList<Notification> notificationArrayList;
    Context context;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationArrayList) {
        this.notificationArrayList = notificationArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_rv_design, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Notification model = notificationArrayList.get(position);

        String type = model.getType();

        FirebaseDatabase.getInstance().getReference()
                .child(DatabaseUtilities.USERS)
                .child(model.getNotiBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    String notification = "";

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        String username = user.getName();
                        Picasso.get()
                                .load(user.getProfile_picture())
                                .placeholder(R.drawable.user)
                                .into(holder.binding.usersPhotoInNotification);
                        if (type.equals(NotificationTypes.LIKE)) {
                            notification = "<b>" + username + "</b>" + " Liked your post";
                        } else if(type.equals(NotificationTypes.COMMENT)){
                            notification = "<b>" + username + "</b>" + " Commented on your post";
                        } else if(type.equals(NotificationTypes.FOLLOW)){
                            notification = "<b>" + username + "</b>" + " Started following you";
                        }
                        holder.binding.notificationTextView.setText(Html.fromHtml(notification));
                        holder.binding.notificationTime.setText(Function.setTime(model.getNotiAt()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.notificationCnt.setOnClickListener(view ->{
            if(!type.equals(NotificationTypes.FOLLOW)) {
                FirebaseDatabase.getInstance().getReference()
                        .child(DatabaseUtilities.NOTIFICATION)
                        .child(model.getPostedBy())
                        .child(model.getNotiID())
                        .child(NotificationTypes.OPENEDORNOT)
                        .setValue(true);
                holder.binding.notificationTextView.setTextColor(Color.parseColor("#FFFFFF"));
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", model.getPostID());
                intent.putExtra("postedBy", model.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else  {
                Toast.makeText(context , "Aap bhi follow back de do" , Toast.LENGTH_SHORT).show();
            }
        });

        boolean checkOpen = model.isCheckOpen();
        if(checkOpen){
            holder.binding.notificationTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }

    }

    @Override
    public int getItemCount() {
        return notificationArrayList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        NotificationRvDesignBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = NotificationRvDesignBinding.bind(itemView);
        }
    }
}
