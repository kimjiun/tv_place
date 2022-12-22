package com.mobitant.yesterdaytv.lib;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.regex.Pattern;

public class EtcLib {
    public final String TAG = EtcLib.class.getSimpleName();
    private volatile static EtcLib instance;

    public static EtcLib getInstance() {
        if (instance == null) {
            synchronized (EtcLib.class) {
                if (instance == null) {
                    instance = new EtcLib();
                }
            }
        }
        return instance;
    }

    public String getPhoneNumber(Context context) {

        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();

        if (number != null && !number.equals("") && number.length() >= 8) {
            if (Locale.getDefault().getCountry().equals("KR")) {
                if (number.startsWith("82")) {
                    number = "+" + number;
                }

                if (number.startsWith("0")) {
                    number = "+82" + number.substring(1, number.length());
                }
            }

            MyLog.d(TAG, "number " + number);

        } else {
            number = getDeviceId(context);
        }

        return number;
    }

    private String getDeviceId(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice = tm.getDeviceId();
        String androidId =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String serial = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) serial = Build.SERIAL;

        if (tmDevice != null) return "01" + tmDevice;
        if (androidId != null) return "02" + androidId;
        if (serial != null) return "03" + serial;

        return null;
    }

    public boolean isValidPhoneNumber(String number) {
        if (number == null) {
            return false;
        } else {
            if (Pattern.matches("\\d{2}-\\d{3}-\\d{4}", number)
                    || Pattern.matches("\\d{3}-\\d{3}-\\d{4}", number)
                    || Pattern.matches("\\d{3}-\\d{4}-\\d{4}", number)
                    || Pattern.matches("\\d{10}", number)
                    || Pattern.matches("\\d{11}", number) ) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getPhoneNumberText(String number) {
        String phoneText = "";

        if (StringLib.getInstance().isBlank(number)) {
            return phoneText;
        }

        number = number.replace("-", "");

        int length = number.length();

        if (number.length() >= 10) {
            phoneText = number.substring(0, 3) + "-"
                      + number.substring(3, length-4) + "-"
                      + number.substring(length-4, length);
        }

        return phoneText;
    }
}
