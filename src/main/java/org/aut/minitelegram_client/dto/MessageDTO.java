package org.aut.minitelegram_client.dto;

import java.util.List;

public class MessageDTO {
    private Long   messageId;
    private String senderDisplayName;
    private String receiverDisplayName;
    private String sentAt;
    private Long   replyToMessageId;

    private String content;
    private List<String> mediaUrls;

    public Long getMessageId() {
        return messageId;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getReceiverDisplayName() {
        return receiverDisplayName;
    }

    public String getSentAt() {
        return sentAt;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public String getContent() {
        return content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}
