package com.mobitant.yesterdaytv;

import android.app.Application;
import android.os.StrictMode;

import com.mobitant.yesterdaytv.item.ReviewItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.MemberInfoItem;


public class MyApp extends Application {
    private MemberInfoItem memberInfoItem;
    private TvInfoItem tvInfoItem;

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public MemberInfoItem getMemberInfoItem() {
        if (memberInfoItem == null) memberInfoItem = new MemberInfoItem();

        return memberInfoItem;
    }

    public void setMemberInfoItem(MemberInfoItem item) {
        this.memberInfoItem = item;
    }

    public int getMemberSeq() {
        return memberInfoItem.seq;
    }

    public void setTvInfoItem(TvInfoItem tvInfoItem) {
        this.tvInfoItem = tvInfoItem;
    }

    public TvInfoItem getTvInfoItem() {
        return tvInfoItem;
    }
}
