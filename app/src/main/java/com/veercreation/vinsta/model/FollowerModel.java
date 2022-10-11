package com.veercreation.vinsta.model;


public class FollowerModel {
    private String followedBy;
    private Long followedAt;


    public FollowerModel() {
    }

    public FollowerModel(String followedBy, Long followedAt, String followerPic) {
        this.followedBy = followedBy;
        this.followedAt = followedAt;
    }

    public String getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(String followedBy) {
        this.followedBy = followedBy;
    }

    public Long getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(Long followedAt) {
        this.followedAt = followedAt;
    }

}
