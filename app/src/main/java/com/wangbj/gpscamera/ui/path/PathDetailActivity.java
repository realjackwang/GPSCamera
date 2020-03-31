package com.wangbj.gpscamera.ui.path;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.utils.ACache;

import java.util.ArrayList;
import java.util.List;

public class PathDetailActivity extends AppCompatActivity {

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
        String  Id = intent.getStringExtra("id");

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

        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(18);
        aMap.moveCamera(mCameraUpdate);

        UiSettings mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setCompassEnabled(false); //指南针按钮
        mUiSettings.setMyLocationButtonEnabled(false); //定位按钮
        mUiSettings.setZoomControlsEnabled(false); //放大缩小按钮


        String mapFilesDir = getExternalFilesDir(null).toString() + "/aMap/";
        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setStyleDataPath(mapFilesDir + "style.data");
        customMapStyleOptions.setStyleExtraPath(mapFilesDir + "style_extra.data");
        aMap.setCustomMapStyle(customMapStyleOptions);


        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));

        String[] singlePath1 = (String[]) paths.get(0);
        String[] singlePath2 = (String[]) paths.get(paths.size()-1);

        LatLng start = new LatLng(Double.parseDouble(singlePath1[0]), Double.parseDouble(singlePath1[1]));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(start));

        addMarker("起点",singlePath1,R.drawable.ic_map_marker_start);
        addMarker("终点",singlePath2,R.drawable.ic_map_marker_end);


    }

    private void addMarker(String name,String[] gps, int img_id){

        LatLng latLng = new LatLng(Double.parseDouble(gps[0]), Double.parseDouble(gps[1]));
        MarkerOptions markerOption = new MarkerOptions().position(latLng).title(name);
        View markerView = LayoutInflater.from(PathDetailActivity.this).inflate(R.layout.marker_home, mMapView, false);
//        TextView textView = markerView.findViewById(R.id.marker_name);
//        textView.setText(name);

        ImageView imageView = markerView.findViewById(R.id.iv_marker);
        imageView.setImageDrawable(getDrawable(img_id));
        markerOption.draggable(true);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromView(markerView));

        aMap.addMarker(markerOption);

    }


    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_stay, R.anim.activity_bottom_exit);
    }
}
