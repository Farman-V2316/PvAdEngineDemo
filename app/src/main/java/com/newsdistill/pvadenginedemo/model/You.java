package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class You implements Parcelable {

    @SerializedName("options")
    @Expose
    private String options;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("vote")
    @Expose
    private String vote;
    @SerializedName("save")
    @Expose
    private boolean save;
    @SerializedName("reaction")
    @Expose
    private String reaction;

    public You() {
    }

    public You(String options, String rating, String vote, boolean save, String reaction) {
        this.options = options;
        this.rating = rating;
        this.vote = vote;
        this.save = save;
        this.reaction = reaction;
    }

    public final static Creator<You> CREATOR = new Creator<You>() {

        @SuppressWarnings({
                "unchecked"
        })
        public You createFromParcel(Parcel in) {
            You instance = new You();
            instance.options = ((String) in.readValue((String.class.getClassLoader())));
            instance.rating = ((String) in.readValue((String.class.getClassLoader())));
            instance.vote = ((String) in.readValue((String.class.getClassLoader())));
            instance.save = ((boolean) in.readValue((boolean.class.getClassLoader())));
            instance.reaction = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public You[] newArray(int size) {
            return (new You[size]);
        }

    };

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(options);
        dest.writeValue(rating);
        dest.writeValue(vote);
        dest.writeValue(save);
        dest.writeValue(reaction);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "You{" +
                "options=" + options +
                ", rating=" + rating +
                ", vote=" + vote +
                ", save=" + save +
                ", reaction=" + reaction +
                '}';
    }
}