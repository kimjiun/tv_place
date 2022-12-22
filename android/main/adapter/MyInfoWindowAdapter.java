package com.mobitant.yesterdaytv.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mobitant.yesterdaytv.Constant;
import com.mobitant.yesterdaytv.MyApp;
import com.mobitant.yesterdaytv.R;
import com.mobitant.yesterdaytv.TvInfoMapFragment;
import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private  Context context;
    private TvInfoItem item;

    ViewGroup infowindow;
    ImageView image;
    TextView name;
    TextView keyword;
    TextView distance;

    private HashMap<Marker, TvInfoItem> markerMap;
    private Marker marker;

    public MyInfoWindowAdapter(Context context, HashMap<Marker, TvInfoItem> markerMap){
        this.context = context;
        this.markerMap = markerMap;
        infowindow = (ViewGroup)((Activity)context).getLayoutInflater().inflate(R.layout.infowindow, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        this.marker = marker;
        this.item = markerMap.get(marker);

        initialize();

        int meter = (int) item.userDistanceMeter;
        if (meter == 0)
            distance.setText("");
        else if (meter < 1000)
            distance.setText(meter + context.getResources().getString(R.string.unit_m));
        else
            distance.setText( (meter / 1000) + context.getResources().getString(R.string.unit_km));

        name.setText(item.name);
        keyword.setText(StringLib.getInstance().getSubString(context, item.keyword, Constant.MAX_LENGTH_DESCRIPTION));

        setImage(image, item.imageFilename);

        return infowindow;
    }

    private void setImage(ImageView imageView, String fileName) {
        if (StringLib.getInstance().isBlank(fileName)) {
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView);
        } else {
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }


    public void initialize(){
        image = (ImageView) infowindow.findViewById(R.id.image);
        name = (TextView) infowindow.findViewById(R.id.name);
        keyword = (TextView) infowindow.findViewById(R.id.keyword);
        distance = (TextView) infowindow.findViewById(R.id.distance);
    }

    public Marker getMarker(){
        return marker;
    }
}
