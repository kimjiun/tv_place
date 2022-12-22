package com.mobitant.yesterdaytv.lib;

import android.content.Context;

import com.mobitant.yesterdaytv.R;

public class StringLib {
    public final String TAG = StringLib.class.getSimpleName();
    private volatile static StringLib instance;

    protected StringLib() {
    }

    public static StringLib getInstance() {
        if (instance == null) {
            synchronized (StringLib.class) {
                if (instance == null) {
                    instance = new StringLib();
                }
            }
        }
        return instance;
    }

    public boolean isBlank(String str) {
        if (str == null || str.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public String getSubString(Context context, String str, int max) {
        if (str != null && str.length() > max) {
            return str.substring(0, max) + context.getResources().getString(R.string.skip_string);
        } else {
            return str;
        }
    }
}
