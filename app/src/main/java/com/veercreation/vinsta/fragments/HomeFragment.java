package com.veercreation.vinsta.fragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
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
import com.veercreation.vinsta.adapter.PostAdapter;
import com.veercreation.vinsta.adapter.StoryAdapter;
import com.veercreation.vinsta.databinding.FragmentHomeBinding;
import com.veercreation.vinsta.model.PostModel;
import com.veercreation.vinsta.model.StoryModel;
import com.veercreation.vinsta.model.User;
import com.veercreation.vinsta.model.UserStories;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    RecyclerView storyRV;
    ShimmerRecyclerView dashboardRV;
    ArrayList<StoryModel> storyList = new ArrayList<>();
    ArrayList<PostModel> postList = new ArrayList<>();
    PostAdapter postAdapter;
    StoryAdapter adapter;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    ProgressDialog progressDialog;

    ActivityResultLauncher<String> galleryLauncher;

    FirebaseDatabase database;
    FirebaseAuth auth;

    User currentUser;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        dashboardRV = binding.dashboardRV;
        dashboardRV.showShimmerAdapter();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //story uploading progressbar
        progressDialog.setTitle("Story uploading");
        progressDialog.setMessage("Thodi der rook jaao jara");

        //story recycler view
        storyRV = binding.storyRV;
         adapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        storyRV.setLayoutManager(linearLayoutManager);
        storyRV.setNestedScrollingEnabled(false);
        storyRV.setAdapter(adapter);

        //post recycler view

        postAdapter = new PostAdapter(getContext(), postList);
        LinearLayoutManager linearLayoutManagerForPost = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        dashboardRV.setLayoutManager(linearLayoutManagerForPost);
        dashboardRV.setNestedScrollingEnabled(false);


        getStories();
        getPosts();

        database.getReference()
                .child("Users")
                .child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                        Picasso.get()
                                .load(currentUser.getProfile_picture())
                                .placeholder(R.drawable.user)
                                .into(binding.profileImageInHome);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.addStory.setOnClickListener(view ->
        {
            galleryLauncher.launch("image/*");
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                progressDialog.show();
                final StorageReference reference = storage.getReference()
                        .child("stories")
                        .child(auth.getUid())
                        .child(new Date().getTime() + "");

                reference.putFile(result)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        StoryModel storyModel = new StoryModel();
                                        storyModel.setStoryAt(new Date().getTime());
                                        database.getReference()
                                                .child("stories")
                                                .child(auth.getUid())
                                                .child("postedAt")
                                                .setValue(storyModel.getStoryAt())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        UserStories stories = new UserStories(uri.toString(), storyModel.getStoryAt());
                                                        database.getReference()
                                                                .child("stories")
                                                                .child(auth.getUid())
                                                                .child("users_stories")
                                                                .push()
                                                                .setValue(stories);
                                                        progressDialog.dismiss();

                                                    }
                                                });
                                    }
                                });
                            }
                        });


            }
        });


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        getPosts();
    }

    private void getPosts() {
        dashboardRV.showShimmerAdapter();
        database.getReference()
                .child("posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            PostModel postModel = dataSnapshot.getValue(PostModel.class);
                            assert postModel != null;
                            postModel.setPostId(dataSnapshot.getKey());
                            postList.add(postModel);
                            Log.i("postid", postModel.getPostId());
                        }
                        dashboardRV.setAdapter(postAdapter);
                        dashboardRV.hideShimmerAdapter();
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getStories() {
        database.getReference()
                .child("stories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            storyList.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                StoryModel storyModel = new StoryModel();
                                storyModel.setStoryBy(storySnapshot.getKey());
                                storyModel.setStoryAt(storySnapshot.child("postedAt").getValue(Long.class));
                                ArrayList<UserStories> stories = new ArrayList<>();
                                for (DataSnapshot snapshot1 : storySnapshot.child("users_stories").getChildren()) {
                                    UserStories userStories = snapshot1.getValue(UserStories.class);
                                    stories.add(userStories);
                                }
                                storyModel.setStories(stories);
                                storyList.add(storyModel);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void seeStory(){

    }
}