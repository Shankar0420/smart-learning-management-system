package com.lms.dto;

import java.time.LocalDateTime;

public class AiChatResponse {
    private String reply;
    private String model;
    private boolean simulated;
    private LocalDateTime timestamp;

    public AiChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AiChatResponse(String reply, String model, boolean simulated) {
        this.reply = reply;
        this.model = model;
        this.simulated = simulated;
        this.timestamp = LocalDateTime.now();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public void setSimulated(boolean simulated) {
        this.simulated = simulated;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

