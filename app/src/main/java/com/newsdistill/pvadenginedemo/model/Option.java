package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Option implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;

    @SerializedName("result")
    @Expose
    private int result;

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("totalCount")
    @Expose
    private int totalCount;

    @SerializedName("answer")
    @Expose
    private boolean answer;

    public final static Creator<Option> CREATOR = new Creator<Option>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Option createFromParcel(Parcel in) {
            Option instance = new Option();
            instance.id = ((String) in.readValue((String.class.getClassLoader())));
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.imageUrl = ((String) in.readValue((Object.class.getClassLoader())));
            instance.result = ((int) in.readValue((int.class.getClassLoader())));
            instance.count = ((int) in.readValue((int.class.getClassLoader())));
            instance.totalCount = ((int) in.readValue((int.class.getClassLoader())));
            instance.answer = ((boolean) in.readValue((int.class.getClassLoader())));
            return instance;
        }

        public Option[] newArray(int size) {
            return (new Option[size]);
        }

    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeValue(imageUrl);
        dest.writeValue(result);
        dest.writeValue(count);
        dest.writeValue(totalCount);
        dest.writeValue(answer);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl=" + imageUrl +
                ", result=" + result +
                ", count=" + count +
                ", totalCount=" + totalCount +
                ", answer=" + answer +
                '}';
    }
}