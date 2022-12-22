package com.mobitant.yesterdaytv.item;

import com.google.gson.annotations.SerializedName;

@org.parceler.Parcel
public class TvInfoItem {
    public int seq;
    @SerializedName("rate_avg") public int rateAvg;
    @SerializedName("show_date") public int showdate;
    public String name;
    public String program;
    public String keyword;
    public String tel;
    public String address;
    public double latitude;
    public double longitude;
    @SerializedName("reg_date") public String regDate;
    @SerializedName("mod_date") public String modDate;
    @SerializedName("user_distance_meter") public double userDistanceMeter;
    @SerializedName("is_keep") public boolean isKeep;
    @SerializedName("image_filename") public String imageFilename;

    @Override
    public String toString() {
        return "TvInfoItem{" +
                "seq=" + seq +
                ", showdate=" + showdate +
                ", name='" + name + '\'' +
                ", program='" + program + '\''+
                ", keyword='" + keyword + '\'' +
                ", tel='" + tel + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", regDate='" + regDate + '\'' +
                ", modDate='" + modDate + '\'' +
                ", userDistanceMeter=" + userDistanceMeter +
                ", rate=" + rateAvg +
                ", isKeep=" + isKeep +
                ", imageFilename='" + imageFilename + '\'' +
                '}';
    }
}
