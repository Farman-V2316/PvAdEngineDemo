package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommunityPost implements Parcelable, Cloneable {

    public static final String CONTENT_TYPE_IMAGE = "image";
    public static final String CONTENT_TYPE_VIDEO = "video";

    @SerializedName("postId")
    @Expose
    private String postId;

    @SerializedName("sourceId")
    @Expose
    private String sourceId;

    @SerializedName("languageId")
    @Expose
    private int languageId;

    @SerializedName("actualLanguageId")
    @Expose
    private int actualLanguageId;

    @SerializedName("genreId")
    @Expose
    private int genreId;

    @SerializedName("categoryId")
    @Expose
    private int categoryId;

    @SerializedName("newsTypeId")
    @Expose
    private String newsTypeId;

    @SerializedName("videoTypeId")
    @Expose
    private String videoTypeId;

    @SerializedName("bucketNum")
    @Expose
    private String bucketNum;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("publishedDate")
    @Expose
    private String publishedDate;

    @SerializedName("genreName")
    @Expose
    private String genreName;

    @SerializedName("link")
    @Expose
    private String link;

    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;

    @SerializedName("imageUrlSmall")
    @Expose
    private List<String> imageUrlSmall = new ArrayList<>();

    @SerializedName("imageUrlMedium")
    @Expose
    private List<String> imageUrlMedium = new ArrayList<>();

    @SerializedName("imageUrlLarge")
    @Expose
    private List<String> imageUrlLarge = new ArrayList<>();

    @SerializedName("thumbnails")
    @Expose
    private List<String> thumbnails = new ArrayList<>();

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

    @SerializedName("live")
    @Expose
    private boolean live;

    @SerializedName("directLink")
    @Expose
    private boolean directLink;

    @SerializedName("label")
    @Expose
    private String label;

    @SerializedName("keywords")
    @Expose
    private String keywords;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("simpleDescription")
    @Expose
    private String simpleDescription;

    @SerializedName("descriptionElements")
    @Expose
    private DescriptionElementsP descriptionElements;

    @SerializedName("views")
    @Expose
    private String views;

    @SerializedName("answers")
    @Expose
    private String answers;

    @SerializedName("shares")
    @Expose
    private String shares;

    @SerializedName("who")
    @Expose
    private Who who;

    @SerializedName("topAnswer")
    @Expose
    private Comment topAnswer;

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("you")
    @Expose
    private You you;

    @SerializedName("options")
    @Expose
    private List<Option> options = new ArrayList<>();

    @SerializedName("reactions")
    @Expose
    private List<Reaction> reactions = new ArrayList<>();

    @SerializedName("rating")
    @Expose
    private OverallRating overallRating;

    @SerializedName("disabled")
    @Expose
    private boolean disabled;

    @SerializedName("resolved")
    @Expose
    private boolean resolved;

    @SerializedName("resolvedBy")
    @Expose
    private String resolvedBy;

    @SerializedName("masterPostId")
    @Expose
    private String masterPostId;

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("displayVotes")
    @Expose
    private boolean displayVotes;

    @SerializedName("startTs")
    @Expose
    private String startTs;

    @SerializedName("endTs")
    @Expose
    private String endTs;

    private boolean isLivePoll;

    private boolean isNearYou;

    @SerializedName("showDirection")
    @Expose
    private boolean showDirection;

    @SerializedName("analyticsIdList")
    @Expose
    private List<String> analyticsIdList;

    @SerializedName("news")
    @Expose
    private boolean news;

    @SerializedName("label2")
    @Expose
    private String label2;

    @SerializedName("activityName")
    @Expose
    private String activityName;

    @SerializedName("activityTitle")
    @Expose
    private String activityTitle;

    @SerializedName("activityParams")
    @Expose
    private String activityParams;

    @SerializedName("activityAPIUrl")
    @Expose
    private String activityAPIUrl;

    @SerializedName("activityAPIParams")
    @Expose
    private String activityAPIParams;

    @SerializedName("position")
    @Expose
    private int position;


    @SerializedName("postCount")
    @Expose
    private int postCount;

    @SerializedName("PVLink")
    @Expose
    private String PVLink;

    @SerializedName("magazine")
    @Expose
    private boolean magazine;

    @SerializedName("duplicates")
    @Expose
    private List<DuplicatePost> duplicates = new ArrayList<>();

    @SerializedName("tags")
    @Expose
    private List<TagModel> tags = new ArrayList<>();

    @SerializedName("accessType")
    @Expose
    private String accessType;

    @SerializedName("readnearn")
    @Expose
    private boolean readnearn;

    @SerializedName("commentsDisabled")
    @Expose
    private boolean commentsDisabled;

    @SerializedName("cardType")
    @Expose
    private String cardType;

    @SerializedName("cardSubType")
    @Expose
    private String cardSubType;

    @SerializedName("feedSourceName")
    @Expose
    private String feedSourceName;

    @SerializedName("deepLink")
    @Expose
    private String deepLink;

    @SerializedName("viewType")
    @Expose
    private String viewType;

    @SerializedName("children")
    @Expose
    private List<CommunityPost>  children = new ArrayList<>();

    public transient boolean isViewedThroughAutoScroll;
    public transient int nbVisits;
    public transient long pausedCurrentPosition;
    public transient long resumedCurrentPosition;
    public transient String lastCachedImageUrl;
    public transient String lastCachedVideoUrl;
    public transient int positionInList;
    public transient String fetchedPage;
    public transient Map<String, String> abData;
    public transient boolean isFromPrefill;

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardSubType() {
        return cardSubType;
    }

    public void setCardSubType(String cardSubType) {
        this.cardSubType = cardSubType;
    }

    public String getFeedSourceName() {
        return feedSourceName;
    }

    public void setFeedSourceName(String feedSourceName) {
        this.feedSourceName = feedSourceName;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public List<CommunityPost> getChildren() {
        return children;
    }

    public void setChildren(List<CommunityPost> children) {
        this.children = children;
    }

    public boolean isNearYou() {
        return isNearYou;
    }

    public void setNearYou(boolean nearYou) {
        isNearYou = nearYou;
    }

    public boolean isLivePoll() {
        return isLivePoll;
    }

    public void setLivePoll(boolean livePoll) {
        isLivePoll = livePoll;
    }

    public CommunityPost() {
    }

    public boolean isNews() {
        return news;
    }

    public void setNews(boolean news) {
        this.news = news;
    }

    protected CommunityPost(Parcel in) {
        postId = in.readString();
        sourceId = in.readString();
        languageId = in.readInt();
        actualLanguageId = in.readInt();
        genreId = in.readInt();
        categoryId = in.readInt();
        newsTypeId = in.readString();
        videoTypeId = in.readString();
        bucketNum = in.readString();
        title = in.readString();
        publishedDate = in.readString();
        genreName = in.readString();
        link = in.readString();
        imageUrl = in.readString();
        imageUrlSmall = in.createStringArrayList();
        imageUrlMedium = in.createStringArrayList();
        imageUrlLarge = in.createStringArrayList();
        thumbnails = in.createStringArrayList();
        hot = in.readByte() != 0;
        hideTime = in.readByte() != 0;
        play = in.readByte() != 0;
        save = in.readByte() != 0;
        live = in.readByte() != 0;
        directLink = in.readByte() != 0;
        label = in.readString();
        keywords = in.readString();
        description = in.readString();
        simpleDescription = in.readString();
        descriptionElements = in.readParcelable(DescriptionElementsP.class.getClassLoader());
        views = in.readString();
        answers = in.readString();
        shares = in.readString();
        who = in.readParcelable(Who.class.getClassLoader());
        topAnswer = in.readParcelable(Comment.class.getClassLoader());
        location = in.readParcelable(Location.class.getClassLoader());
        you = in.readParcelable(You.class.getClassLoader());
        options = in.createTypedArrayList(Option.CREATOR);
        reactions = in.createTypedArrayList(Reaction.CREATOR);
        overallRating = in.readParcelable(OverallRating.class.getClassLoader());
        disabled = in.readByte() != 0;
        resolved = in.readByte() != 0;
        resolvedBy = in.readString();

        masterPostId = in.readString();
        status = in.readInt();
        displayVotes = in.readByte() != 0;
        startTs = in.readString();
        endTs = in.readString();
        isLivePoll = in.readByte() != 0;
        isNearYou = in.readByte() != 0;
        showDirection = in.readByte() != 0;
        analyticsIdList = in.createStringArrayList();
        news = in.readByte() != 0;
        label2 = in.readString();

        activityName = in.readString();
        activityTitle = in.readString();
        activityParams = in.readString();
        activityAPIUrl = in.readString();
        activityAPIParams = in.readString();
        position = in.readInt();
        postCount = in.readInt();
        PVLink = in.readString();
        magazine = in.readByte() != 0;
        duplicates = in.createTypedArrayList(DuplicatePost.CREATOR);
        tags = in.createTypedArrayList(TagModel.CREATOR);
        accessType = in.readString();
        readnearn = in.readByte() != 0;
        commentsDisabled = in.readByte() != 0;
        cardType = in.readString();
        cardSubType = in.readString();
        feedSourceName = in.readString();
        deepLink = in.readString();
        viewType = in.readString();
        children = in.createTypedArrayList(CommunityPost.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(sourceId);
        dest.writeInt(languageId);
        dest.writeInt(actualLanguageId);
        dest.writeInt(genreId);
        dest.writeInt(categoryId);
        dest.writeString(newsTypeId);
        dest.writeString(videoTypeId);
        dest.writeString(bucketNum);
        dest.writeString(title);
        dest.writeString(publishedDate);
        dest.writeString(genreName);
        dest.writeString(link);
        dest.writeString(imageUrl);
        dest.writeStringList(imageUrlSmall);
        dest.writeStringList(imageUrlMedium);
        dest.writeStringList(imageUrlLarge);
        dest.writeStringList(thumbnails);
        dest.writeByte((byte) (hot ? 1 : 0));
        dest.writeByte((byte) (hideTime ? 1 : 0));
        dest.writeByte((byte) (play ? 1 : 0));
        dest.writeByte((byte) (save ? 1 : 0));
        dest.writeByte((byte) (live ? 1 : 0));
        dest.writeByte((byte) (directLink ? 1 : 0));
        dest.writeString(label);
        dest.writeString(keywords);
        dest.writeString(description);
        dest.writeString(simpleDescription);
        dest.writeParcelable(descriptionElements, flags);
        dest.writeString(views);
        dest.writeString(answers);
        dest.writeString(shares);
        dest.writeParcelable(who, flags);
        dest.writeParcelable(topAnswer, flags);
        dest.writeParcelable(location, flags);
        dest.writeParcelable(you, flags);
        dest.writeTypedList(options);
        dest.writeTypedList(reactions);
        dest.writeParcelable(overallRating, flags);
        dest.writeByte((byte) (disabled ? 1 : 0));
        dest.writeByte((byte) (resolved ? 1 : 0));
        dest.writeString(resolvedBy);
        dest.writeString(masterPostId);
        dest.writeInt(status);
        dest.writeByte((byte) (displayVotes ? 1 : 0));
        dest.writeString(startTs);
        dest.writeString(endTs);
        dest.writeByte((byte) (isLivePoll ? 1 : 0));
        dest.writeByte((byte) (isNearYou ? 1 : 0));
        dest.writeByte((byte) (showDirection ? 1 : 0));
        dest.writeStringList(analyticsIdList);
        dest.writeByte((byte) (news ? 1 : 0));
        dest.writeString(label2);
        dest.writeString(activityName);
        dest.writeString(activityTitle);
        dest.writeString(activityParams);
        dest.writeString(activityAPIUrl);
        dest.writeString(activityAPIParams);
        dest.writeInt(position);
        dest.writeInt(postCount);
        dest.writeString(PVLink);
        dest.writeByte((byte) (magazine ? 1 : 0));
        dest.writeTypedList(duplicates);
        dest.writeTypedList(tags);
        dest.writeString(accessType);
        dest.writeByte((byte) (readnearn ? 1 : 0));
        dest.writeByte((byte) (commentsDisabled ? 1 : 0));
        dest.writeString(cardType);
        dest.writeString(cardSubType);
        dest.writeString(feedSourceName);
        dest.writeString(deepLink);
        dest.writeString(viewType);
        dest.writeTypedList(children);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommunityPost> CREATOR = new Creator<CommunityPost>() {
        @Override
        public CommunityPost createFromParcel(Parcel in) {
            return new CommunityPost(in);
        }

        @Override
        public CommunityPost[] newArray(int size) {
            return new CommunityPost[size];
        }
    };

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
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

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getNewsTypeId() {
        return newsTypeId;
    }

    public void setNewsTypeId(String newsTypeId) {
        this.newsTypeId = newsTypeId;
    }

    public String getVideoTypeId() {
        return videoTypeId;
    }

    public void setVideoTypeId(String videoTypeId) {
        this.videoTypeId = videoTypeId;
    }

    public String getBucketNum() {
        return bucketNum;
    }

    public void setBucketNum(String bucketNum) {
        this.bucketNum = bucketNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImageUrlSmall() {
        return imageUrlSmall;
    }

    public void setImageUrlSmall(List<String> imageUrlSmall) {
        this.imageUrlSmall = imageUrlSmall;
    }

    public List<String> getImageUrlMedium() {
        return imageUrlMedium;
    }

    public void setImageUrlMedium(List<String> imageUrlMedium) {
        this.imageUrlMedium = imageUrlMedium;
    }

    public List<String> getImageUrlLarge() {
        return imageUrlLarge;
    }

    public void setImageUrlLarge(List<String> imageUrlLarge) {
        this.imageUrlLarge = imageUrlLarge;
    }

    public List<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<String> thumbnails) {
        this.thumbnails = thumbnails;
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

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isDirectLink() {
        return directLink;
    }

    public void setDirectLink(boolean directLink) {
        this.directLink = directLink;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSimpleDescription() {
        return simpleDescription;
    }

    public void setSimpleDescription(String simpleDescription) {
        this.simpleDescription = simpleDescription;
    }

    public DescriptionElementsP getDescriptionElements() {
        return descriptionElements;
    }

    public void setDescriptionElements(DescriptionElementsP descriptionElements) {
        this.descriptionElements = descriptionElements;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public Who getWho() {
        return who;
    }

    public void setWho(Who who) {
        this.who = who;
    }

    public Comment getTopAnswer() {
        return topAnswer;
    }

    public void setTopAnswer(Comment topAnswer) {
        this.topAnswer = topAnswer;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public You getYou() {
        return you;
    }

    public void setYou(You you) {
        this.you = you;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public void addReaction(Reaction reaction) {
        if (reactions == null) {
            reactions = new ArrayList<>();
        }
        this.reactions.add(reaction);
    }

    public OverallRating getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(OverallRating overallRating) {
        this.overallRating = overallRating;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getMasterPostId() {
        return masterPostId;
    }

    public void setMasterPostId(String masterPostId) {
        this.masterPostId = masterPostId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDisplayVotes() {
        return displayVotes;
    }

    public void setDisplayVotes(boolean displayVotes) {
        this.displayVotes = displayVotes;
    }

    public String getStartTs() {
        return startTs;
    }

    public void setStartTs(String startTs) {
        this.startTs = startTs;
    }

    public String getEndTs() {
        return endTs;
    }

    public void setEndTs(String endTs) {
        this.endTs = endTs;
    }

    public boolean isShowDirection() {
        return showDirection;
    }

    public void setShowDirection(boolean showDirection) {
        this.showDirection = showDirection;
    }


    public List<String> getAnalyticsIdList() {
        return analyticsIdList;
    }

    public void setTrackingIdList(List<String> trackingIdList) {
        this.analyticsIdList = trackingIdList;
    }

    public String getLabel2() {
        return label2;
    }

    public void setLabel2(String label2) {
        this.label2 = label2;
    }


    @Override
    public CommunityPost clone() throws CloneNotSupportedException {
        return (CommunityPost) super.clone();
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityParams() {
        return activityParams;
    }

    public void setActivityParams(String activityParams) {
        this.activityParams = activityParams;
    }

    public String getActivityAPIUrl() {
        return activityAPIUrl;
    }

    public void setActivityAPIUrl(String activityAPIUrl) {
        this.activityAPIUrl = activityAPIUrl;
    }

    public String getActivityAPIParams() {
        return activityAPIParams;
    }

    public void setActivityAPIParams(String activityAPIParams) {
        this.activityAPIParams = activityAPIParams;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public String getPVLink() {
        return PVLink;
    }

    public void setPVLink(String PVLink) {
        this.PVLink = PVLink;
    }

    public boolean isMagazine() {
        return magazine;
    }

    public void setMagazine(boolean magazine) {
        this.magazine = magazine;
    }



    public List<DuplicatePost> getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(List<DuplicatePost> duplicates) {
        this.duplicates = duplicates;
    }

    public List<TagModel> getTags() {
        return tags;
    }

    public void setTags(List<TagModel> tags) {
        this.tags = tags;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    @Override
    public String toString() {
        return "CommunityPost{" +
                "postId='" + postId + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", languageId=" + languageId +
                ", actualLanguageId=" + actualLanguageId +
                ", genreId=" + genreId +
                ", categoryId=" + categoryId +
                ", newsTypeId='" + newsTypeId + '\'' +
                ", videoTypeId='" + videoTypeId + '\'' +
                ", bucketNum='" + bucketNum + '\'' +
                ", title='" + title + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", genreName='" + genreName + '\'' +
                ", link='" + link + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageUrlSmall=" + imageUrlSmall +
                ", imageUrlMedium=" + imageUrlMedium +
                ", imageUrlLarge=" + imageUrlLarge +
                ", thumbnails=" + thumbnails +
                ", hot=" + hot +
                ", hideTime=" + hideTime +
                ", play=" + play +
                ", save=" + save +
                ", live=" + live +
                ", directLink=" + directLink +
                ", label='" + label + '\'' +
                ", keywords='" + keywords + '\'' +
                ", description='" + description + '\'' +
                ", simpleDescription='" + simpleDescription + '\'' +
                ", descriptionElements=" + descriptionElements +
                ", views='" + views + '\'' +
                ", answers='" + answers + '\'' +
                ", shares='" + shares + '\'' +
                ", who=" + who +
                ", topAnswer=" + topAnswer +
                ", location=" + location +
                ", you=" + you +
                ", options=" + options +
                ", reactions=" + reactions +
                ", overallRating=" + overallRating +
                ", disabled=" + disabled +
                ", resolved=" + resolved +
                ", resolvedBy='" + resolvedBy + '\'' +
                ", masterPostId='" + masterPostId + '\'' +
                ", status=" + status +
                ", displayVotes=" + displayVotes +
                ", startTs='" + startTs + '\'' +
                ", endTs='" + endTs + '\'' +
                ", isLivePoll=" + isLivePoll +
                ", isNearYou=" + isNearYou +
                ", showDirection=" + showDirection +
                ", analyticsIdList=" + analyticsIdList +
                ", news=" + news +
                ", label2='" + label2 + '\'' +
                ", activityName='" + activityName + '\'' +
                ", activityTitle='" + activityTitle + '\'' +
                ", activityParams='" + activityParams + '\'' +
                ", activityAPIUrl='" + activityAPIUrl + '\'' +
                ", activityAPIParams='" + activityAPIParams + '\'' +
                ", position=" + position +
                ", postCount=" + postCount +
                ", PVLink='" + PVLink + '\'' +
                ", magazine=" + magazine +
                ", duplicates=" + duplicates +
                ", tags=" + tags +
                ", accessType='" + accessType + '\'' +
                '}';
    }

    public boolean isReadnearn() {
        return readnearn;
    }

    public void setReadnearn(boolean readnearn) {
        this.readnearn = readnearn;
    }

    public boolean isCommentsDisabled() {
        return commentsDisabled;
    }

    public void setCommentsDisabled(boolean commentsDisabled) {
        this.commentsDisabled = commentsDisabled;
    }

    public void resetCachedAdaptiveUrls() {
        lastCachedImageUrl = null;
        lastCachedVideoUrl = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityPost that = (CommunityPost) o;
        return Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId);
    }
}