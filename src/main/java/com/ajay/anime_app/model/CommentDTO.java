package com.ajay.anime_app.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class CommentDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String content;

    private Long user;

    private Long post;
    private Long likeCount;
    private String username;
    private boolean likedByCurrentUser;
    private Long parentId;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(final Long user) {
        this.user = user;
    }

    public Long getPost() {
        return post;
    }

    public void setPost(final Long post) {
        this.post = post;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
