package com.veercreation.vinsta.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veercreation.vinsta.R;
import com.veercreation.vinsta.adapter.CommentAdapter;
import com.veercreation.vinsta.databinding.ActivityCommentBinding;
import com.veercreation.vinsta.keys.DatabaseUtilities;
import com.veercreation.vinsta.keys.NotificationTypes;
import com.veercreation.vinsta.model.Comment;
import com.veercreation.vinsta.model.Notification;
import com.veercreation.vinsta.model.PostModel;
import com.veercreation.vinsta.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class CommentActivity extends AppCompatActivity {
    ActivityCommentBinding binding;
    Intent intent;
    String postId, postedBy, postDesc;
    FirebaseAuth auth;
    FirebaseDatabase database;

    ArrayList<Comment> commentArrayList = new ArrayList<>();
    CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        CommentActivity.this.setTitle("Comments");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        adapter = new CommentAdapter(getApplicationContext(), commentArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.commentRV.setLayoutManager(linearLayoutManager);
        binding.commentRV.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        database.getReference()
                .child("posts")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PostModel postModel = snapshot.getValue(PostModel.class);
                        postDesc = postModel.getPostDesc();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Database error occurred", Toast.LENGTH_SHORT).show();
                    }
                });

        database.getReference()
                .child("Users")
                .child(postedBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        postDesc = " <b>" + user.getName() + "</b>: " + postDesc;
                        binding.captionInCommentActivity.setText(Html.fromHtml(postDesc));
                        Picasso.get()
                                .load(user.getProfile_picture())
                                .placeholder(R.drawable.user)
                                .into(binding.profileImage);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Database error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
        binding.commentPostButton.setOnClickListener(view ->
                {
                    if (binding.commentEditText.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Comment to lakh bhai", Toast.LENGTH_SHORT).show();

                    } else {

                        Comment comment = new Comment();
                        comment.setCommentBody(binding.commentEditText.getText().toString());
                        comment.setCommentedAt(new Date().getTime());
                        comment.setCommentedBy(auth.getUid());

                        database.getReference()
                                .child("posts")
                                .child(postId)
                                .child("comments")
                                .push()
                                .setValue(comment).addOnSuccessListener(unused -> {
                            database.getReference()
                                    .child("posts")
                                    .child(postId)
                                    .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int commentCount = 0;
                                    if (snapshot.exists()) {
                                        commentCount = snapshot.getValue(Integer.class);
                                    }
                                    database.getReference()
                                            .child("posts")
                                            .child(postId)
                                            .child("commentCount")
                                            .setValue(commentCount + 1)
                                            .addOnSuccessListener(unused1 ->
                                            {
                                                binding.commentEditText.setText("");
                                                Toast.makeText(getApplicationContext(), "commented", Toast.LENGTH_SHORT).show();
                                                binding.commentRV.smoothScrollToPosition(adapter.getItemCount());
                                                Notification notification = new Notification();
                                                notification.setNotiAt(new Date().getTime());
                                                notification.setType(NotificationTypes.COMMENT);
                                                //notification send by
                                                notification.setNotiBy(FirebaseAuth.getInstance().getUid());
                                                notification.setPostID(postId);
                                                notification.setPostedBy(postedBy);


                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(DatabaseUtilities.NOTIFICATION)
                                                        //notification send to
                                                        .child(postedBy)
                                                        .push()
                                                        .setValue(notification);

                                            }).addOnFailureListener(e ->
                                    {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();


                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        });
                    }
                }
        );

        database.getReference()
                .child("posts")
                .child(postId)
                .child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            commentArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Comment comment = dataSnapshot.getValue(Comment.class);
                                commentArrayList.add(comment);
                            }
                            adapter.notifyDataSetChanged();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}