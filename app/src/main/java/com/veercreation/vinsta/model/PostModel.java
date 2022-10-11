package com.veercreation.vinsta.model;

public class PostModel {
    private String postId , postImage, postDesc, postedBy , postedAt;
    private int postLike , commentCount;


    public PostModel(String postId, String postImage, String postDesc, String postedBy, String postedAt) {
        this.postId = postId;
        this.postImage = postImage;
        this.postDesc = postDesc;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
    }

    public PostModel(){}

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(String postedAt) {
        this.postedAt = postedAt;
    }

    public int getPostLike() {
        return postLike;
    }

    public void setPostLike(int postLike) {
        this.postLike = postLike;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

}
