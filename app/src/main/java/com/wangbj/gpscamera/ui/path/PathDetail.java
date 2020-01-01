package com.wangbj.gpscamera.ui.path;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.utils.ACache;

import java.util.ArrayList;
import java.util.List;

public class PathDetail extends AppCompatActivity {

    MapView mMapView;
    AMap aMap;
    ACache aCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_detail);

        aCache = ACache.get(this);
        //接收intent传来的notice的id的值
        Intent intent = getIntent();
        Long Id = intent.getLongExtra("id",-1);

        System.out.println(Id);

        ArrayList<Path> pathlist =  (ArrayList<Path>) aCache.getAsObject("PathHistory");
        Path path = new Path();
        for(int i=0;i<pathlist.size();i++){
            if(pathlist.get(i).get("id").equals(Id)) {
                path = pathlist.get(i);
                break;
            }
        }
        List<LatLng> latLngs = new ArrayList<LatLng>();

        ArrayList paths = (ArrayList) path.get("path");

        for(int i=0;i<paths.size();i++){
            String[] singlePath = (String[]) paths.get(i);
            latLngs.add(new LatLng(Double.parseDouble(singlePath[0]), Double.parseDouble(singlePath[1])));
        }

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        aMap = mMapView.getMap();

        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(16);


        aMap.moveCamera(mCameraUpdate);


        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));


        String[] singlePath1 = (String[]) paths.get(0);
        LatLng start = new LatLng(Double.parseDouble(singlePath1[0]), Double.parseDouble(singlePath1[1]));
        final Marker marker1 = aMap.addMarker(new MarkerOptions().position(start).title("起点").snippet("DefaultMarker"));

        aMap.moveCamera(CameraUpdateFactory.changeLatLng(start));

        String[] singlePath2 = (String[]) paths.get(paths.size()-1);
        LatLng end = new LatLng(Double.parseDouble(singlePath2[0]), Double.parseDouble(singlePath2[1]));
        final Marker marker2 = aMap.addMarker(new MarkerOptions().position(end).title("终点").snippet("DefaultMarker"));

    }
}
