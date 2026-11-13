package com.lms.dto;

import jakarta.validation.constraints.NotBlank;

public class DiscussionReplyRequest {

    @NotBlank(message = "Reply content cannot be blank")
    private String content;

    public DiscussionReplyRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

