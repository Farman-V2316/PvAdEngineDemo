package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ndindialap7 on 29/5/17.
 */

public class Comment implements Parcelable {

    @SerializedName("answerId")
    @Expose
    private String answerId;

    @SerializedName("postId")
    @Expose
    private String postId;

    @SerializedName("question")
    @Expose
    private String question;

    @SerializedName("comment")
    @Expose
    private String comment;

    @SerializedName("publishedDate")
    @Expose
    private String publishedDate;

    @SerializedName("keywords")
    @Expose
    private String keywords;

    @SerializedName("languageId")
    @Expose
    private int languageId;

    @SerializedName("actualLanguageId")
    @Expose
    private int actualLanguageId;

    @SerializedName("answerTypeId")
    @Expose
    private int answerTypeId;

    @SerializedName("answerCount")
    @Expose
    private String answerCount;

    @SerializedName("views")
    @Expose
    private String views;

    @SerializedName("reactions")
    @Expose
    private List<Reaction> reactions = new ArrayList<>();

    @SerializedName("hot")
    @Expose
    private boolean hot;

    @SerializedName("hideTime")
    @Expose
    private boolean hideTime;

    @SerializedName("play")
    @Expose
    private boolean play;

    @SerializedName("save")
    @Expose
    private boolean save;

    @SerializedName("who")
    @Expose
    private Who who;

    @SerializedName("you")
    @Expose
    private You you;

    @SerializedName("rank")
    @Expose
    private String rank;

    @SerializedName("newsTypeId")
    @Expose
    String newsTypeId;


    public Comment() {
    }

    protected Comment(Parcel in) {
        answerId = in.readString();
        postId = in.readString();
        question = in.readString();
        comment = in.readString();
        publishedDate = in.readString();
        keywords = in.readString();
        languageId = in.readInt();
        actualLanguageId = in.readInt();
        answerTypeId = in.readInt();
        answerCount = in.readString();
        views = in.readString();
        reactions = in.createTypedArrayList(Reaction.CREATOR);
        hot = in.readByte() != 0;
        hideTime = in.readByte() != 0;
        play = in.readByte() != 0;
        save = in.readByte() != 0;
        who = in.readParcelable(Who.class.getClassLoader());
        you = in.readParcelable(You.class.getClassLoader());
        rank = in.readString();
        newsTypeId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(answerId);
        dest.writeString(postId);
        dest.writeString(question);
        dest.writeString(comment);
        dest.writeString(publishedDate);
        dest.writeString(keywords);
        dest.writeInt(languageId);
        dest.writeInt(actualLanguageId);
        dest.writeInt(answerTypeId);
        dest.writeString(answerCount);
        dest.writeString(views);
        dest.writeTypedList(reactions);
        dest.writeByte((byte) (hot ? 1 : 0));
        dest.writeByte((byte) (hideTime ? 1 : 0));
        dest.writeByte((byte) (play ? 1 : 0));
        dest.writeByte((byte) (save ? 1 : 0));
        dest.writeParcelable(who, flags);
        dest.writeParcelable(you, flags);
        dest.writeString(rank);
        dest.writeString(newsTypeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public int getActualLanguageId() {
        return actualLanguageId;
    }

    public void setActualLanguageId(int actualLanguageId) {
        this.actualLanguageId = actualLanguageId;
    }

    public int getAnswerTypeId() {
        return answerTypeId;
    }

    public void setAnswerTypeId(int answerTypeId) {
        this.answerTypeId = answerTypeId;
    }

    public String getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(String answerCount) {
        this.answerCount = answerCount;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public boolean isHot() {
        return hot;
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }

    public boolean isHideTime() {
        return hideTime;
    }

    public void setHideTime(boolean hideTime) {
        this.hideTime = hideTime;
    }

    public boolean isPlay() {
        return play;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public Who getWho() {
        return who;
    }

    public void setWho(Who who) {
        this.who = who;
    }

    public You getYou() {
        return you;
    }

    public void setYou(You you) {
        this.you = you;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getNewsTypeId() {
        return newsTypeId;
    }

    public void setNewsTypeId(String newsTypeId) {
        this.newsTypeId = newsTypeId;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "answerId='" + answerId + '\'' +
                ", postId='" + postId + '\'' +
                ", question='" + question + '\'' +
                ", comment='" + comment + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", keywords='" + keywords + '\'' +
                ", languageId=" + languageId +
                ", actualLanguageId=" + actualLanguageId +
                ", answerTypeId=" + answerTypeId +
                ", answerCount='" + answerCount + '\'' +
                ", views='" + views + '\'' +
                ", reactions=" + reactions +
                ", hot=" + hot +
                ", hideTime=" + hideTime +
                ", play=" + play +
                ", save=" + save +
                ", who=" + who +
                ", you=" + you +
                ", rank='" + rank + '\'' +
                ", newsTypeId='" + newsTypeId + '\'' +
                '}';
    }
}