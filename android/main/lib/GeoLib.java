package com.mobitant.yesterdaytv.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.mobitant.yesterdaytv.item.GeoItem;

import java.util.List;
import java.util.Locale;

public class GeoLib {
    public final String TAG = GeoLib.class.getSimpleName();
    private volatile static GeoLib instance;

    public static GeoLib getInstance() {
        if (instance == null) {
            synchronized (GeoLib.class) {
                if (instance == null) {
                    instance = new GeoLib();
                }
            }
        }
        return instance;
    }

    public void setLastKnownLocation(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        int result = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (location != null) {
            GeoItem.knownLatitude = location.getLatitude();
            GeoItem.knownLongitude = location.getLongitude();
        } else {
            //서울 설정
            GeoItem.knownLatitude = 37.566229;
            GeoItem.knownLongitude = 126.977689;
        }
    }

    public Address getAddressString(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public String getAddressString(Address address) {
        String address2 = "";
        if (address.getAddressLine(1) != null) {
            address2 = " " + address.getAddressLine(1);
        }
        return address.getAddressLine(0) + address2;
    }

    public int getDistanceMeterFromScreenCenter(GoogleMap map) {
        VisibleRegion vr = map.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;

        Location leftLocation = new Location("left");
        leftLocation.setLatitude(vr.latLngBounds.getCenter().latitude);
        leftLocation.setLongitude(left);

        Location center=new Location("center");
        center.setLatitude( vr.latLngBounds.getCenter().latitude);
        center.setLongitude( vr.latLngBounds.getCenter().longitude);
        return  (int) center.distanceTo(leftLocation);
    }
}
