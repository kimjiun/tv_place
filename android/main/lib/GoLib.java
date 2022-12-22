package com.mobitant.yesterdaytv.lib;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mobitant.yesterdaytv.ItemReviewActivity;
import com.mobitant.yesterdaytv.MyReviewActivity;
import com.mobitant.yesterdaytv.TvInfoInfoActivity;
import com.mobitant.yesterdaytv.ProfileActivity;


public class GoLib {
    public final String TAG = GoLib.class.getSimpleName();
    private volatile static GoLib instance;

    public static GoLib getInstance() {
        if (instance == null) {
            synchronized (GoLib.class) {
                if (instance == null) {
                    instance = new GoLib();
                }
            }
        }
        return instance;
    }

    public void goFragment(FragmentManager fragmentManager, int containerViewId,
                           Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    public void goFragmentBack(FragmentManager fragmentManager, int containerViewId,
                               Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void goBackFragment(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
    }

    public void goProfileActivity(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void goTvInfoActivity(Context context, int infoSeq) {
        Intent intent = new Intent(context, TvInfoInfoActivity.class);
        intent.putExtra(TvInfoInfoActivity.INFO_SEQ, infoSeq);
        context.startActivity(intent);
    }

    public void goItemReviewActivity(Context context, int infoSeq, String itemName) {
        Intent intent = new Intent(context, ItemReviewActivity.class);
        intent.putExtra(ItemReviewActivity.INFO_SEQ, infoSeq);
        intent.putExtra(ItemReviewActivity.ITEM_NAME, itemName);
        context.startActivity(intent);
    }

    public void goMyReviewActivity(Context context) {
        Intent intent = new Intent(context, MyReviewActivity.class);
        context.startActivity(intent);
    }
}
