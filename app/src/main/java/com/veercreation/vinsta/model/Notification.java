package com.veercreation.vinsta.model;

public class Notification {
private String notiBy;
private  long notiAt;
private String type;
private String postID;

    public String getNotiID() {
        return notiID;
    }

    public void setNotiID(String notiID) {
        this.notiID = notiID;
    }

    private String notiID;
private  String postedBy;
private boolean checkOpen;

    public String getNotiBy() {
        return notiBy;
    }

    public void setNotiBy(String notiBy) {
        this.notiBy = notiBy;
    }

    public long getNotiAt() {
        return notiAt;
    }

    public void setNotiAt(long notiAt) {
        this.notiAt = notiAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public boolean isCheckOpen() {
        return checkOpen;
    }

    public void setCheckOpen(boolean checkOpen) {
        this.checkOpen = checkOpen;
    }
}
