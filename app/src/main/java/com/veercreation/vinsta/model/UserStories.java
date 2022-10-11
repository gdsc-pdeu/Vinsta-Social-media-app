package com.veercreation.vinsta.model;

public class UserStories {
    private  String imageUrl;
    private long storyAt;

    public UserStories(String imageUrl, long storyAt) {
        this.imageUrl = imageUrl;
        this.storyAt = storyAt;
    }

    public UserStories() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getStoryAt() {
        return storyAt;
    }

    public void setStoryAt(long storyAt) {
        this.storyAt = storyAt;
    }
}
