package com.veercreation.vinsta.adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.activities.CommentActivity;
import com.veercreation.vinsta.databinding.DashboardRvDesignBinding;
import com.veercreation.vinsta.global.Function;
import com.veercreation.vinsta.keys.DatabaseUtilities;
import com.veercreation.vinsta.keys.NotificationTypes;
import com.veercreation.vinsta.model.Notification;
import com.veercreation.vinsta.model.PostModel;
import com.veercreation.vinsta.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewholder> {

    ArrayList<PostModel> postList;
    Context context;
    DateFormat dateFormatter = new SimpleDateFormat("hh:mm:aa dd-MMM");


    public PostAdapter(Context context, ArrayList<PostModel> postList) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_design, parent, false);
        return new viewholder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        PostModel model = postList.get(position);

            //setting post image
            Picasso.get()
                    .load(model.getPostImage())
                    .placeholder(R.drawable.sample_cover)
                    .into(holder.binding.postImage);
            holder.binding.likesTextView.setText(Integer.toString(model.getPostLike()));
            holder.binding.commentsTextView.setText(Integer.toString(model.getCommentCount()));
            holder.binding.postTime.setText(Function.setTime(Long.parseLong(model.getPostedAt())));


            //setting userdata
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(model.getPostedBy())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Picasso.get()
                                    .load(user.getProfile_picture())
                                    .placeholder(R.drawable.user)
                                    .into(holder.binding.profileImageInPost);
                            holder.binding.usernameInPost.setText(user.getName());
                            String postDesc = " <b>" + user.getName() + "</b> " + model.getPostDesc();
                            holder.binding.postDesc.setText(Html.fromHtml(postDesc));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(model.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.i("likes" ,"liked" );
                            holder.binding.likesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                        } else {
                            holder.binding.likesTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                Log.i("likes" ,"liked now" );
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(model.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(true)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(model.getPostId())
                                                            .child("postLike")
                                                            .setValue(model.getPostLike() + 1)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    holder.binding.likesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                                                                    holder.binding.likesTextView.setText(model.getPostLike() + 1 + "");

                                                                    Notification notification = new Notification();
                                                                    notification.setNotiAt(new Date().getTime());
                                                                    notification.setType(NotificationTypes.LIKE);
                                                                    //notification send by
                                                                    notification.setNotiBy(FirebaseAuth.getInstance().getUid());

                                                                    notification.setPostID(model.getPostId());
                                                                    notification.setPostedBy(model.getPostedBy());


                                                                    FirebaseDatabase.getInstance().getReference()
                                                                            .child(DatabaseUtilities.NOTIFICATION)
                                                                            //notification send to
                                                                            .child(model.getPostedBy())
                                                                            .push()
                                                                            .setValue(notification);
                                                                }
                                                            });
                                                }
                                            });

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        holder.binding.commentsTextView.setOnClickListener(view -> {
            Intent intent = new Intent(context , CommentActivity.class);
            intent.putExtra("postId" , model.getPostId());
            intent.putExtra("postedBy" , model.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        Log.i("postsize", postList.size() + "");
        return postList.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        DashboardRvDesignBinding binding;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            binding = DashboardRvDesignBinding.bind(itemView);
        }
    }
}
