package com.wangbj.gpscamera.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * @author JackWang
 * @fileName GPSUtils
 * @date on 2019-12-30 下午 3:02
 * @email 544907049@qq.com
 **/

public class GPSUtils {

    private AMapLocation fromGpsToAmap(Location location,Context context){
        AMapLocation aMapLocation = new AMapLocation(location);
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        try {
            converter.coord(new DPoint(location.getLatitude(), location.getLongitude()));
            DPoint desLatLng = converter.convert();
            aMapLocation.setLatitude(desLatLng.getLatitude());
            aMapLocation.setLongitude(desLatLng.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aMapLocation;
    }

//        private Location fromAmaptoGps(AMapLocation aMapLocation){
//        re
//        }

}
