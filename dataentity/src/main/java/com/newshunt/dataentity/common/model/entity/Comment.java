/*
 * Created by Rahul Ravindran at 25/9/19 11:32 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import java.io.Serializable;

/**
 * Comment as an entity that can be used across the board.
 * <p/>
 * Each comment has an owner and it belongs to specific entity. Entity can be
 * STORY, BOOK or COMMENT itself. Comment by default is in LIVE state.
 * <p/>
 * If flagged by someone it moves to IN_REVIEW and up-on review it can move back
 * to LIVE or INVALID state.
 *
 * @author shreyas.desai
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = -3187350899691944635L;
    private Long id;
    private String entityId;
    private String entityType;
    private Long voteUpCount = 0L;
    private Long voteDownCount = 0L;
    private String content;
    private String name;
    private String timeStamp;
    private String tag;
    private String email;
    private int totalCommentsCount;
    private String nextPageUrl;
    private String commentId;
    private String userId;
    private Long replyCount;
    private String parentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVoteUpCount() {
        return voteUpCount;
    }

    public void setVoteUpCount(Long voteUpCount) {
        this.voteUpCount = voteUpCount;
    }

    public Long getVoteDownCount() {
        return voteDownCount;
    }

    public void setVoteDownCount(Long voteDownCount) {
        this.voteDownCount = voteDownCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getTotalCommentsCount() {
        return totalCommentsCount;
    }

    public void setTotalCommentsCount(int totalCommentsCount) {
        this.totalCommentsCount = totalCommentsCount;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Comment [id=").append(id).append(", entityId=").append(entityId)
                .append(", entityType=").append(entityType).append(", voteUpCount=").append(voteUpCount)
                .append(", voteDownCount=").append(voteDownCount).append(", content=").append(content)
                .append(", name=").append(name).append(", timeStamp=").append(timeStamp).append(", tag=")
                .append(tag).append(", email=").append(email).append(", totalCommentsCount=")
                .append(totalCommentsCount).append(", nextPageUrl=").append(nextPageUrl)
                .append(", commentId=").append(commentId).append(", userId=").append(userId)
                .append(", replyCount=").append(replyCount).append(", parentId=").append(parentId)
                .append("]");
        return builder.toString();
    }

    public enum Status {
        LIVE, IN_REVIEW, INVALID
    }

}
