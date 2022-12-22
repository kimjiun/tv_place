package com.mobitant.yesterdaytv.item;

import com.google.gson.annotations.SerializedName;

public class ReviewItem extends MemberInfoItem{
    public int rate;
    public String review;
    @SerializedName("review_seq") public String reviewSeq;
    @SerializedName("review_member_seq") public int reviewMemberSeq;
    @SerializedName("review_item_seq") public int reviewItemSeq;
    @SerializedName("review_date") public String reviewDate;

    @Override
    public String toString() {
        return "ReviewItem{" +
                "reviewSeq='" + reviewSeq + '\'' +
                ", reviewMemberSeq='" + reviewMemberSeq + '\'' +
                ", reviewItemSeq='" + reviewItemSeq + '\'' +
                ", rate='" + rate +'\'' +
                ", reviewContent='" + review +'\'' +
                ", reviewDate='" + reviewDate + '\'' +
                '}';
    }
}
