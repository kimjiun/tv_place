package com.mobitant.yesterdaytv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.lib.EtcLib;
import com.mobitant.yesterdaytv.lib.GeoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.RemoteLib;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if(!RemoteLib.getInstance().isConnected(context)){
            showNoService();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTask();
            }
        }, 1200);
    }

    private void showNoService(){
        TextView messageText = (TextView) findViewById(R.id.message);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }

    public void startTask(){
        String phone = EtcLib.getInstance().getPhoneNumber(this);
        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    public void selectMemberInfo(String phone){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);
        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        call.enqueue(new Callback<MemberInfoItem>() {
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem> response) {
                MemberInfoItem item = response.body();
                if(response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)) {
                    MyLog.d(TAG, "success " + response.body().toString());
                    setMemberInfoItem(item);
                }else{
                    MyLog.d(TAG, "not success");
                    goProfileActivity(item);
                }
            }

            @Override
            public void onFailure(Call<MemberInfoItem> call, Throwable t) {
               MyLog.d(TAG, "no internet connectivity");
               MyLog.d(TAG, t.toString());
            }
        });
    }

    private void setMemberInfoItem(MemberInfoItem item){
        ((MyApp) getApplicationContext()).setMemberInfoItem(item);
        startMain();
    }

    public void startMain(){
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goProfileActivity(MemberInfoItem item){
        if(item == null || item.seq <=0){
            insertMemberPhone();
        }
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        Intent intent2 = new Intent(this, ProfileActivity.class);
        startActivity(intent2);

        finish();
    }

    private void insertMemberPhone(){
        String phone = EtcLib.getInstance().getPhoneNumber(context);
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);
        Call<String> call = remoteService.insertMemberPhone(phone);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    MyLog.d(TAG, "success insert id " + response.body());
                }
                else{
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG, "fail" + statusCode + errorBody.toString());
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");

            }
        });

    }

}
