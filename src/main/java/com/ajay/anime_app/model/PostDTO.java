package com.ajay.anime_app.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class PostDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String content;

    private Long userId;
    private Long likeCount;


    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}
