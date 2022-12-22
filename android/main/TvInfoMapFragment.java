package com.mobitant.yesterdaytv;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobitant.yesterdaytv.adapter.MyInfoWindowAdapter;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.lib.GeoLib;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TvInfoMapFragment extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;
    GoogleMap map;

    LatLng currentLatLng;
    int distanceMeter = 640;
    int currentZoomLevel = Constant.MAP_ZOOM_LEVEL_DETAIL;

    Toast zoomGuideToast;
    Marker nowMarked;

    private HashMap<Marker, TvInfoItem> markerMap = new HashMap<>();

    ArrayList<TvInfoItem> infoList = new ArrayList<>();

    MyInfoWindowAdapter myInfoWindowAdapter;

    public static TvInfoMapFragment newInstance() {
        TvInfoMapFragment f = new TvInfoMapFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();

        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();

        View v = inflater.inflate(R.layout.fragment_tvinfo_map, container, false);

        myInfoWindowAdapter = new MyInfoWindowAdapter(context, markerMap);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).changeTitle("지도");

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
        fragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        map.setInfoWindowAdapter(myInfoWindowAdapter);

        String fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;

        if (ActivityCompat.checkSelfPermission(context, fineLocationPermission)
                != PackageManager.PERMISSION_GRANTED) return;

        map.setMyLocationEnabled(true);

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                GoLib.getInstance().goTvInfoActivity(context, markerMap.get(marker).seq);
            }
        });

        map.setOnMapClickListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnMarkerClickListener(this);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(false);

        MyLog.d(TAG, GeoItem.getKnownLocation().toString());

        if (GeoItem.getKnownLocation() != null) {
            movePosition(GeoItem.getKnownLocation(), Constant.MAP_ZOOM_LEVEL_DETAIL);
        }
        showList();
    }

    private void movePosition(LatLng latlng, float zoomLevel) {
        CameraPosition cp = new CameraPosition.Builder().target((latlng)).zoom(zoomLevel).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        nowMarked = marker;
        if(nowMarked != null)
            MyLog.d(TAG, "Now clicked marker: "+ nowMarked.getTitle());
        marker.showInfoWindow();
        return true;
    }

    private void listMap(int memberSeq, LatLng latLng, int distance, LatLng userLatLng) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<TvInfoItem>> call = remoteService.listMap(memberSeq, latLng.latitude,
                latLng.longitude, distance, userLatLng.latitude, userLatLng.longitude);
        call.enqueue(new Callback<ArrayList<TvInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<TvInfoItem>> call,
                                   Response<ArrayList<TvInfoItem>> response) {
                ArrayList<TvInfoItem> list = response.body();

                if (list == null) {
                    list = new ArrayList<>();
                }

                if (response.isSuccessful()) {
                    setMap(list);
                    infoList = list;
                } else {
                    MyLog.d(TAG, "not success");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<TvInfoItem>> call, Throwable t) {
            }
        });
    }

    private void setMap(ArrayList<TvInfoItem> list) {

        if (map != null && list != null) {
            map.clear();
            addMarker(list);
        }

        drawCircle(currentLatLng);
    }

    private void addMarker(ArrayList<TvInfoItem> list) {
        MyLog.d(TAG, "addMarker list.size() " + list.size());

        if (list == null || list.size() == 0) return;

        MyLog.d(TAG, "list size = " + list.size());

        for (TvInfoItem item : list) {
            MyLog.d(TAG, "addMarker " + item);
            if (item.latitude != 0 && item.longitude != 0) {
                Marker marker = map.addMarker(getMarker(item));
                markerMap.put(marker, item);
            }
        }
    }

    private MarkerOptions getMarker(TvInfoItem item) {
        final MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(item.latitude, item.longitude));
        marker.title(item.name);
        marker.snippet(item.tel);
        marker.draggable(false);

        switch (item.program){
            case "생활의달인" :
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dalin_logo));
                break;
            case "생생정보" :
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.sangsang_logo));
                break;
            case "생방송투데이" :
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.today_logo));
                break;
            case "생방송오늘저녁" :
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dinner_logo));
                break;
        }
        return marker;
    }

    private void drawCircle(LatLng position) {
        double radiusInMeters = distanceMeter;
        int strokeColor = 0x440000ff;
        int shadeColor = 0x110000ff;

        CircleOptions circleOptions
                = new CircleOptions().center(position).radius(radiusInMeters)
                      .fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(4);
        map.addCircle(circleOptions);

    }

    @Override
    public void onCameraMove() {
        showList();
    }

    private void showList() {
        currentZoomLevel = (int) map.getCameraPosition().zoom;
        currentLatLng = map.getCameraPosition().target;

        if (currentZoomLevel < Constant.MAP_MAX_ZOOM_LEVEL) {

            map.clear();

            if (zoomGuideToast != null) {
                zoomGuideToast.cancel();
            }
            zoomGuideToast = Toast.makeText(context
                                , getResources().getString(R.string.message_zoom_level_max_over)
                                , Toast.LENGTH_SHORT);
            zoomGuideToast.show();

            return;
        }

        distanceMeter = GeoLib.getInstance().getDistanceMeterFromScreenCenter(map);

        listMap(memberSeq, currentLatLng, distanceMeter, GeoItem.getKnownLocation());
    }
}