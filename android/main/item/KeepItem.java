package com.mobitant.yesterdaytv.item;

import com.google.gson.annotations.SerializedName;

public class KeepItem extends TvInfoItem {
    @SerializedName("keep_seq") public String keepSeq;
    @SerializedName("keep_member_seq") public String keepMemberSeq;
    @SerializedName("keep_date") public String keepDate;

    @Override
    public String toString() {
        return "KeepItem{" +
                "keepSeq='" + keepSeq + '\'' +
                ", keepMemberSeq='" + keepMemberSeq + '\'' +
                ", keepDate='" + keepDate + '\'' +
                '}';
    }
}
