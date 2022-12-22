package com.mobitant.yesterdaytv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobitant.yesterdaytv.custom.WorkaroundMapFragment;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.lib.DialogLib;
import com.mobitant.yesterdaytv.lib.EtcLib;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvInfoInfoActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";

    Context context;

    int memberSeq;
    int foodInfoSeq;
    String phoneNum="";

    TvInfoItem item;
    GoogleMap map;

    Button showReview;

    View loadingText;
    ScrollView scrollView;
    ImageView keepImage;
    TextView toolbartitle;
    RatingBar rateAvg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvinfo_info);

        context = this;

        loadingText = findViewById(R.id.loading_layout);

        memberSeq = ((MyApp)getApplication()).getMemberSeq();
        foodInfoSeq = getIntent().getIntExtra(INFO_SEQ, 0);
        rateAvg = findViewById(R.id.rate_Average);

        showReview = findViewById(R.id.btn_show_review);
        showReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoLib.getInstance().goItemReviewActivity(context, item.seq, item.name);
            }
        });

        selectFoodInfo(foodInfoSeq, GeoItem.getKnownLocation(),memberSeq);
        setToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectFoodInfo(foodInfoSeq, GeoItem.getKnownLocation(), memberSeq);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbartitle = (TextView)findViewById(R.id.toolbar_title);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectFoodInfo(int foodInfoSeq, LatLng userLatLng, int memberSeq) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);
        Call<TvInfoItem> call = remoteService.selectTvInfo(foodInfoSeq, userLatLng.latitude,
                userLatLng.longitude, memberSeq);

        call.enqueue(new Callback<TvInfoItem>() {
            @Override
            public void onResponse(Call<TvInfoItem> call, Response<TvInfoItem> response) {
                TvInfoItem infoItem = response.body();

                if (response.isSuccessful() && infoItem != null && infoItem.seq > 0) {
                    item = infoItem;
                    setView();
                    loadingText.setVisibility(View.GONE);
                } else {
                    loadingText.setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.loading_text)).setText(R.string.loading_not);
                }
            }

            @Override
            public void onFailure(Call<TvInfoItem> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    private void setView() {
        toolbartitle.setText(item.name);

        ImageView infoImage = (ImageView) findViewById(R.id.info_image);
        setImage(infoImage, item.imageFilename);

        TextView location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(this);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        rateAvg.setRating((int)item.rateAvg);
        FragmentManager fm = getSupportFragmentManager();
        WorkaroundMapFragment fragment = (WorkaroundMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = (WorkaroundMapFragment) SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        fragment.getMapAsync(this);

        fragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        TextView nameText = (TextView) findViewById(R.id.name);
        if (!StringLib.getInstance().isBlank(item.name)) {
            nameText.setText(item.name);
        }

        keepImage = (ImageView) findViewById(R.id.keep);
        keepImage.setOnClickListener(this);
        if (item.isKeep) {
            keepImage.setImageResource(R.drawable.ic_keep_on);
        } else {
            keepImage.setImageResource(R.drawable.ic_keep_off);
        }


        TextView distance = findViewById(R.id.distance);
        int distance_from_user = (int)item.userDistanceMeter / 1000;
        distance.setText(distance_from_user+"km");

        TextView address = (TextView) findViewById(R.id.address);
        if (!StringLib.getInstance().isBlank(item.address)) {
            address.setText(item.address);
        } else {
            address.setVisibility(View.GONE);
        }

        TextView tel = (TextView) findViewById(R.id.tel);
        if (!StringLib.getInstance().isBlank(item.tel)) {
            MyLog.d(TAG, "tel called" + item.tel);
            tel.setText(item.tel);
            tel.setOnClickListener(this);
        } else {
            tel.setVisibility(View.GONE);
        }

        TextView showdate = (TextView) findViewById(R.id.showdate);
        if (!StringLib.getInstance().isBlank(""+item.showdate)) {
            showdate.setText(item.showdate + " 방영");
        } else {
            showdate.setText(R.string.no_text);
        }

        TextView program = (TextView) findViewById(R.id.program);
        if (!StringLib.getInstance().isBlank(item.program)) {
            program.setText("["+ item.program + "]");
        } else {
            program.setText(R.string.no_text);
        }


        TextView keyword = (TextView) findViewById(R.id.keyword);
        if (!StringLib.getInstance().isBlank(item.keyword)) {
            keyword.setText(item.keyword);
        } else {
            keyword.setText(R.string.no_text);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        map.setMyLocationEnabled(true);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(true);

        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(item.latitude, item.longitude));
        marker.draggable(false);
        map.addMarker(marker);

        movePosition(new LatLng(item.latitude, item.longitude), Constant.MAP_ZOOM_LEVEL_DETAIL);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.keep) {
            if (item.isKeep) {
                DialogLib.getInstance()
                        .showKeepDeleteDialog(context, keepHandler, memberSeq, item.seq);
                keepImage.setImageResource(R.drawable.ic_keep_off);
            } else {
                DialogLib.getInstance()
                        .showKeepInsertDialog(context, keepHandler, memberSeq, item.seq);
                keepImage.setImageResource(R.drawable.ic_keep_on);
            }
        } else if (v.getId() == R.id.location) {
            movePosition(new LatLng(item.latitude, item.longitude),
                    Constant.MAP_ZOOM_LEVEL_DETAIL);
        } else if (v.getId() == R.id.tel){
            phoneNum = "tel:" + item.tel;
            startActivity(new Intent("android.intent.action.DIAL", Uri.parse(phoneNum)));
        }
    }

    Handler keepHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            item.isKeep = !item.isKeep;

            if (item.isKeep) {
                keepImage.setImageResource(R.drawable.ic_keep_on);
            } else {
                keepImage.setImageResource(R.drawable.ic_keep_off);
            }
        }
    };

    private void movePosition(LatLng latlng, float zoomLevel) {
        CameraPosition cp = new CameraPosition.Builder().target((latlng)).zoom(zoomLevel).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    private void setImage(ImageView imageView, String fileName) {
        if (StringLib.getInstance().isBlank(fileName)) {
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView);
        } else {
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((MyApp) getApplication()).setTvInfoItem(item);
    }
}
