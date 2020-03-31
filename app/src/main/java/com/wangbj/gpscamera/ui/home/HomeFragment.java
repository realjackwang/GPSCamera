package com.wangbj.gpscamera.ui.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.Projection;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.autonavi.amap.mapcore.interfaces.IProjection;
import com.eminayar.panter.DialogType;
import com.eminayar.panter.PanterDialog;
import com.eminayar.panter.interfaces.OnSingleCallbackConfirmListener;
import com.wangbj.gpscamera.LocationForegroundService;
import com.wangbj.gpscamera.NoticeService;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.httpservice.JsonService;
import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.DataUtils;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import static com.wangbj.gpscamera.utils.TimeFormat.formatUTC;

public class HomeFragment extends Fragment implements LocationSource, GeocodeSearch.OnGeocodeSearchListener {

    private Context context;
    private GeocodeSearch.OnGeocodeSearchListener content;

    private boolean isbind;

    private Button mBtnSave;
    private Button mBtnDel;
    private ImageButton mIbMyLocation;
    private ImageButton mIbSwitchMarker;
    private FrameLayout startRecord;
    private ImageView recordImg;
    private ImageView targetImg;
    private ImageView gpsImg;
    private TextView btnInterval; //按钮间的间隔
    private FrameLayout choicePlace;
    private TextView ui_current_place;


    private boolean isLocation = false; //是否已经开始定位
    private boolean isPath = false; //是否开始记录轨迹
    private Location lastLocation = null; //上一次的位置，用于画线

    private boolean isShowCamera = false; //是否为查看摄像头marker模式

    private PromptDialog promptDialog;

    private Path path; //轨迹

    private ServiceConnection connection;
    private LocationForegroundService locationForegroundService;
    private NoticeService noticeService;

    private ArrayList<String[]> arrayList;

    private ArrayAdapter arrayAdapter;
    private ACache aCache;
    private SharedPreferences sp;

    private TextureMapView mMapView = null;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    protected static CameraPosition cameraPosition;
    private Marker placeMarker;
    private ArrayList<Marker> cameraMarker = new ArrayList<>();
    private ArrayList<Circle> cameraCircle = new ArrayList<>();

    private LocationSource.OnLocationChangedListener mListener;

    private Intent serviceIntent = null;
    private boolean isStartLocation = false;

    private String curPlaceName = null;
    private int curPlaceId;
    private String[] curPlaceGps;
    private JSONArray curPlaceCameraGps;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initView(root);
        initLocation();
        startLocation();

        context = getContext();
        content = this;

        serviceIntent = new Intent();
        serviceIntent.setClass(context, NoticeService.class);

        aCache = ACache.get(context);
        sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        promptDialog = new PromptDialog(getActivity());

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TestService.test(new TestService.OnHttpListener() {
//                    @Override
//                    public void onHttpSuccess(Response response) {
//
//                    }
//
//                    @Override
//                    public void onHttpFailed(Exception e) {
//                        Looper.prepare();
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                        Looper.loop();
//                    }
//                });
//            }
//        });

//
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = "http://192.168.137.1:5000/file/test.zip";
//                DownloadService.download(url, new DownloadService.OnDownloadListener() {
//                    @Override
//                    public void onDownloadSuccess(final File file) {
//
//                        final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(context);
//
//                        alterDiaglog.setTitle("下载完成");
//                        alterDiaglog.setMessage("是否打开文件？");
//
//                        alterDiaglog.setPositiveButton("打开", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                FileUtils.openFile(file);
//
//                            }
//                        });
//
//                        alterDiaglog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        });
//
//                        Looper.prepare();
//                        alterDiaglog.show();
//                        Looper.loop();
//
//
//                    }
//
//                    @Override
//                    public void onDownloading(int progress) {
//                        progressBar.setProgress(progress);
//
//
//                    }
//
//                    @Override
//                    public void onDownloadFailed(Exception e) {
//                        Looper.prepare();
//                        Toast.makeText(context, "服务器无法连接", Toast.LENGTH_SHORT).show();
//                        Looper.loop();
//
//                    }
//                });
//            }
//        });

//        button5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isLocation) {
//                    isLocation = true;
//                    button5.setText("关闭GPS");
//                    startLocation();
//                } else {
//                    isLocation = false;
//                    button5.setText("开启GPS");
//                    stopLocation();
//                }
//
//            }
//        });

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curPlaceName != null) {
                    if (!isPath) {
                        arrayList = new ArrayList<>();
                        path = new Path();
                        path.put("starttime", DataUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                        isPath = true;
                        recordImg.setImageResource(R.drawable.ic_stop);
                        mBtnSave.setVisibility(View.GONE);
                        mBtnDel.setVisibility(View.GONE);
                        btnInterval.setVisibility(View.GONE);
                        if (isStartLocation) {
                            showNotify();
                        }
                    } else {
                        isPath = false;
                        recordImg.setImageResource(R.drawable.ic_start);
                        startRecord.setVisibility(View.GONE);
                        mBtnSave.setVisibility(View.VISIBLE);
                        mBtnDel.setVisibility(View.VISIBLE);
                        btnInterval.setVisibility(View.VISIBLE);

                        if (isStartLocation) {
                            stopNotify();
                        }
                    }
                } else {
                    Toast.makeText(context, "请先点击右上角选择所在区域", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<Path> cachePathArr = (ArrayList<Path>) aCache.getAsObject("PathHistory");
                path.put("title", curPlaceName);
                path.put("endtime", DataUtils.getCurrentTime("HH:mm:ss"));
                path.put("path", arrayList);
                path.put("place", curPlaceId);
                path.put("phone", sp.getString("USERNAME", ""));
                path.put("date", DataUtils.getCurrentTime("YY-MM-DD"));
                path.put("id", sp.getString("USERNAME", "") + DataUtils.getTimestamp());
                path.put("cloud", false);

                if (cachePathArr != null) {
                    cachePathArr.add(path);
                    aCache.put("PathHistory", cachePathArr);
                } else {
                    ArrayList<Path> newPathArr = new ArrayList<>();
                    newPathArr.add(path);
                    aCache.put("PathHistory", newPathArr);
                }

                startRecord.setVisibility(View.VISIBLE);
                mBtnSave.setVisibility(View.GONE);
                mBtnDel.setVisibility(View.GONE);
                btnInterval.setVisibility(View.GONE);
            }

        });

        mBtnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayList = new ArrayList<>();
                path = new Path();
                aMap.clear();

                MyLocationStyle myLocationStyle = getDefaultStyle(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
                aMap.setMyLocationStyle(myLocationStyle);

                startRecord.setVisibility(View.VISIBLE);
                mBtnSave.setVisibility(View.GONE);
                mBtnDel.setVisibility(View.GONE);
                btnInterval.setVisibility(View.GONE);

            }
        });

        mIbMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("position", "Zoom:" + aMap.getCameraPosition().zoom);

                if (lastLocation != null) {
                    if (aMap.getCameraPosition().zoom < 12) {
                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 16, 0, 0)));
                    } else {
                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), aMap.getCameraPosition().zoom, 0, 0)));
                    }
                }

                MyLocationStyle myLocationStyle = getDefaultStyle(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
                aMap.setMyLocationStyle(myLocationStyle);

                targetImg.setImageResource(R.drawable.ic_target_center);
            }
        });

        mIbSwitchMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowCamera) {
                    isShowCamera = false;
                    mIbSwitchMarker.setImageDrawable(context.getDrawable(R.drawable.ic_map_marker_camera));

                    for (int i = 0; i < cameraMarker.size(); i++) {
                        cameraMarker.get(i).remove();
                        cameraCircle.get(i).remove();
                    }

                    showPlaceMarker();
                } else {
                    isShowCamera = true;
                    mIbSwitchMarker.setImageDrawable(context.getDrawable(R.drawable.ic_marker_place));

                    placeMarker.remove();

                    showCameraMarker();
                }
            }
        });

        choicePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDialog.showLoading("正在加载", false);

                JSONArray jsonArray = new JSONArray();
                jsonArray.add(lastLocation.getLatitude() + "," + lastLocation.getLongitude());
                JsonService.Send(aCache, "/get_place/", jsonArray, new JsonService.OnHttpListener() {
                    @Override
                    public void onHttpSuccess(Response response) throws IOException {
                        String str = response.body().string();
                        JSONObject jsonObject = JSONObject.parseObject(str);
                        JSONArray jsonArray1 = (JSONArray) jsonObject.get("data");
                        String[] place_name = new String[jsonArray1.size()];
                        String[] place_id = new String[jsonArray1.size()];
                        final String[] place_camera = new String[jsonArray1.size()];
                        final String[] place_gps = new String[jsonArray1.size()];
                        for (int i = 0; i < place_name.length; i++) {
                            JSONArray jsonArray2 = (JSONArray) jsonArray1.get(i);
                            place_name[i] = jsonArray2.get(1).toString();
                            place_id[i] = jsonArray2.get(0).toString();
                            place_gps[i] = jsonArray2.get(2).toString();
                            place_camera[i] = jsonArray2.get(3).toString();
                        }
                        Looper.prepare();
                        new PanterDialog(context)
                                .setHeaderBackground(R.drawable.pattern_bg_blue)
                                .setHeaderLogo(R.drawable.ic_logo)
                                .setDialogType(DialogType.SINGLECHOICE)
                                .isCancelable(false)
                                .items(place_name, new OnSingleCallbackConfirmListener() {
                                    @Override
                                    public void onSingleCallbackConfirmed(PanterDialog dialog, final int pos, final String text) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ui_current_place.setText(text);
                                                curPlaceName = text;
                                                curPlaceId = pos;
                                                curPlaceCameraGps = (JSONArray) JSONObject.parseObject(place_camera[pos]).get("data");
                                                curPlaceGps = place_gps[pos].split(",");

                                                showPlaceMarker();
                                                mIbSwitchMarker.setVisibility(View.VISIBLE);

                                            }
                                        });
                                    }
                                })
                                .show();
                        promptDialog.dismiss();
                        Looper.loop();

                    }

                    @Override
                    public void onHttpFailed(Exception e) {
                        Looper.prepare();
                        new PanterDialog(context)
                                .setHeaderBackground(R.drawable.pattern_bg_blue)
                                .setHeaderLogo(R.drawable.ic_logo)
                                .setPositive("确定")// You can pass also View.OnClickListener as second param
                                .setMessage("无法正常获取区域数据，请确保已连接网络。")
                                .isCancelable(false)
                                .show();
                        promptDialog.dismiss();
                        Looper.loop();
                    }
                });
            }
        });


        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapView = getView().findViewById(R.id.map);

        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);

            aMap = mMapView.getMap();
            if (getCameraPosition() == null) {
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(39.90403, 116.407525), 10, 0, 0)));
            } else {
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(getCameraPosition()));
            }
            aMap.setLocationSource(this); //通过aMap对象设置定位数据源的监听
            aMap.setMyLocationEnabled(true); //可触发定位并显示当前位置

            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
            mUiSettings.setCompassEnabled(false); //指南针按钮
            mUiSettings.setMyLocationButtonEnabled(false); //定位按钮
            mUiSettings.setZoomControlsEnabled(false); //放大缩小按钮
            mUiSettings.setTiltGesturesEnabled(false); //倾斜手势

            String mapFilesDir = getActivity().getExternalFilesDir(null).toString() + "/aMap/";


            CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
            customMapStyleOptions.setStyleDataPath(mapFilesDir + "style.data");
            customMapStyleOptions.setStyleExtraPath(mapFilesDir + "style_extra.data");

            aMap.setCustomMapStyle(customMapStyleOptions);

            MyLocationStyle myLocationStyle = getDefaultStyle(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
            aMap.setMyLocationStyle(myLocationStyle);

            CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(16);
            aMap.moveCamera(mCameraUpdate);

            aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
                @Override
                public void onTouch(MotionEvent motionEvent) {
                    Log.e("aMap", "onTouch");
                    MyLocationStyle myLocationStyle = getDefaultStyle(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                    aMap.setMyLocationStyle(myLocationStyle);
                    targetImg.setImageResource(R.drawable.ic_target);
                    placeMarker.hideInfoWindow();

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        setCameraPosition(aMap.getCameraPosition());
        super.onDestroy();
        Log.e("HomeFragment", "onDestroy");
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        Log.e("HomeFragment", "onResume");
        if (!isPath) {
            startLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("HomeFragment", "onPause");
        mMapView.onPause();
        if (!isPath & isStartLocation) {
            stopLocation();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    private void initView(View root) {
        mBtnSave = root.findViewById(R.id.btn_save_path);
        mBtnDel = root.findViewById(R.id.btn_del_path);
        mIbMyLocation = root.findViewById(R.id.frameLayout);
        startRecord = root.findViewById(R.id.frameLayout3);
        recordImg = root.findViewById(R.id.recordImg);
        btnInterval = root.findViewById(R.id.text1);
        targetImg = root.findViewById(R.id.frameLayout);
        gpsImg = root.findViewById(R.id.gpsImg);
        choicePlace = root.findViewById(R.id.place_list);
        ui_current_place = root.findViewById(R.id.current_place);
        mIbSwitchMarker = root.findViewById(R.id.ib_switch_camera_place);

    }

    private void showPlaceMarker() {

        if (placeMarker != null) {
            placeMarker.remove();
        }

        LatLng latLng = new LatLng(Double.parseDouble(curPlaceGps[0]), Double.parseDouble(curPlaceGps[1]));
        MarkerOptions markerOption = new MarkerOptions().position(latLng).title(curPlaceName);
        View markerView = LayoutInflater.from(context).inflate(R.layout.fragment_home_map_marker, mMapView, false);
        TextView textView = markerView.findViewById(R.id.marker_name);
        textView.setText(curPlaceName);
        markerOption.draggable(true);//设置Marker可拖动
//                                                markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                                                        .decodeResource(getResources(),R.drawable.ic_map_camera)));
        markerOption.icon(BitmapDescriptorFactory.fromView(markerView));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//                                                markerOption.setFlat(true);//设置marker平贴地图效果

        placeMarker = aMap.addMarker(markerOption);
    }

    private void showCameraMarker() {
        for (int i = 0; i < curPlaceCameraGps.size(); i++) {
            double lat = ((BigDecimal) ((JSONArray) curPlaceCameraGps.get(i)).get(0)).doubleValue();
            double lng = ((BigDecimal) ((JSONArray) curPlaceCameraGps.get(i)).get(1)).doubleValue();
//            double angle = (double) (int) ((JSONArray) curPlaceCameraGps.get(i)).get(2);

            LatLng latLng = new LatLng(lat, lng);
            MarkerOptions markerOption = new MarkerOptions().position(latLng).title(curPlaceName);
            View markerView = LayoutInflater.from(context).inflate(R.layout.marker_home, mMapView, false);
            ImageView imageView = markerView.findViewById(R.id.iv_marker);
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_map_marker_camera));
            markerOption.icon(BitmapDescriptorFactory.fromView(markerView));
            cameraMarker.add(aMap.addMarker(markerOption));


//            ArrayList<LatLng> arrayList = getCirclePosition(lat, lng, angle);

//            PolygonOptions polygonOptions = new PolygonOptions();
//            polygonOptions.add(latLng);
//
//            for (int j = 0; j < arrayList.size(); j++) {
//                polygonOptions.add(arrayList.get(j));
//            }
//
//            polygonOptions.strokeWidth(3) // 多边形的边框
//                    .strokeColor(Color.argb(50, 1, 1, 1)) // 边框颜色
//                    .fillColor(Color.argb(50, 1, 1, 1));   // 多边形的填充色
//

            CircleOptions circleOptions = new CircleOptions().center(latLng).radius(20).
                    fillColor(Color.argb(50, 1, 1, 1)).
                    strokeColor(Color.argb(50, 1, 1, 1)).
                    strokeWidth(3);


            cameraCircle.add(aMap.addCircle(circleOptions));

        }
    }

    private ArrayList<LatLng> getCirclePosition(double lat, double lng, double angle) {

        ArrayList<LatLng> arrayList = new ArrayList<>();

        for (double i = -30; i < 30; i++) {
            double r = 0.001;
            double curAngle = i * 6 + angle;
            double Y = lat;

//            if (curAngle < 0) {
//                curAngle += 360;
//            }

            double X = lng + Math.cos(Math.toRadians(curAngle)) * r;
            if (curAngle != 0) {
                Y = lat + Math.sin(Math.toRadians(curAngle)) * r;
            }

            arrayList.add(new LatLng(Y, X));
        }

        return arrayList;
    }


    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getActivity());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }


    private void startLocation() {
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
        isStartLocation = true;
    }

    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
        isStartLocation = false;
    }


    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(500);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }


    private MyLocationStyle getDefaultStyle(int type) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(type);
        myLocationStyle.strokeColor(Color.argb(100, 0, 0, 220));
        myLocationStyle.radiusFillColor(Color.argb(50, 0, 0, 50));
        myLocationStyle.strokeWidth(0.8f);
        return myLocationStyle;
    }

    private AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                if (isPath) {
                    if (lastLocation != null) {
                        List<LatLng> latLngs = new ArrayList<>();
                        latLngs.add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 53, 146, 196)));
                    }
                    String time = formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss");
                    String[] strings = {"" + location.getLatitude(), "" + location.getLongitude(), time};
                    arrayList.add(strings);

                    if (path.get("info") == null) {
                        path.put("info", location.getAddress());
                    }


                }
                lastLocation = location;

                mListener.onLocationChanged(location);
                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();

                Log.e("GPS", result);
                changeGpsImg(location);
//                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "定位失败，loc is null", Toast.LENGTH_SHORT).show();
            }
        }
    };


    /**
     * 获取GPS状态字符串
     */
    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }


    /**
     * 通过获取的GPS信息提取GPS状态并修改GPS状态图
     */
    private void changeGpsImg(AMapLocation location) {
        if (location.getLocationQualityReport().getGPSSatellites() < 3) {
            gpsImg.setImageResource(R.drawable.ic_gps_bad);
        } else {
            gpsImg.setImageResource(R.drawable.ic_gps_good);
        }

        if (location.getLocationQualityReport().getGPSStatus() != 0) {
            gpsImg.setImageResource(R.drawable.ic_gps_none);
        }

    }

    /**
     * 显示记录轨迹通知
     */
    private void showNotify() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                noticeService = ((NoticeService.LocalBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(context, NoticeService.class);
        isbind = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    /**
     * 关闭记录轨迹通知
     */
    private void stopNotify() {
        if (isbind) {
            context.unbindService(connection);
            isbind = false;
        }
    }


    private CameraPosition getCameraPosition() {
        return cameraPosition;
    }

    private void setCameraPosition(CameraPosition Position) {
        cameraPosition = Position;
    }


//    private void startService() {
//
//        connection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                Log.e(TAG, "--->onServiceConnected");
//                locationForegroundService = ((LocationForegroundService.LocalBinder) service).getLocationForegroundService();
//
//                Location location = locationForegroundService.startGps();
//                if (location != null) {
//
////                    arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
////                    arrayAdapter.notifyDataSetChanged();
//
//                    AMapLocation amapLocation = fromGpsToAmap(location);
//                    mListener.onLocationChanged(amapLocation);
//
//                    lastLocation = amapLocation;
//
//                    txt.setText(String.format("经度%s  纬度:%s", location.getLatitude(), location.getLongitude()));
//                    String time = formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss");
//                    System.out.println(time);
//
//                } else {
//                    txt.setText("正在寻找GPS卫星");
//                }
//
//                locationForegroundService.setLocationCallback(new LocationForegroundService.LocationCallback() {
//                    @Override
//                    public void onLocation(Location location) {
//                        if (location != null) {
//
//                            AMapLocation amapLocation = fromGpsToAmap(location);
//                            mListener.onLocationChanged(amapLocation);
//
//                            if (lastLocation != null) {
//
//                                if (isPath) {
//                                    String time = formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss");
//                                    String[] strings = {"" + location.getLatitude(), "" + location.getLongitude(), time};
//
//                                    arrayList.add(strings);
//
//                                    List<LatLng> latLngs = new ArrayList<LatLng>();
//                                    latLngs.add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
//                                    latLngs.add(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
//                                    aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));
//
//                                    if (path.get("info") == null){
//                                        GeocodeSearch geocoderSearch = new GeocodeSearch(context);
//                                        geocoderSearch.setOnGeocodeSearchListener(content);
//
//                                        LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
//                                        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
//                                        geocoderSearch.getFromLocationAsyn(regeocodeQuery);
//                                    }
//
//
//                                }
//
//                            }
//
//                            lastLocation = amapLocation;
//
//                            txt.setText(String.format("经度%s  纬度:%s", location.getLatitude(), location.getLongitude()));
//                        } else {
//                            txt.setText("GPS卫星寻找失败");
//                        }
//                    }
//
//                    @Override
//                    public void onGpsStatue(int status) {
//
//                        switch (status) {
//                            //GPS状态为可见时
//                            case LocationProvider.AVAILABLE:
//                                Toast.makeText(context, "当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
//                                break;
//                            //GPS状态为服务区外时
//                            case LocationProvider.OUT_OF_SERVICE:
//                                Toast.makeText(context, "当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
//                                break;
//                            //GPS状态为暂停服务时
//                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                                Toast.makeText(context, "当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
//                                break;
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onGpsChanged(int count, int used) {
//                        statelite.setText("卫星数：" + used + "/" + count);
//                    }
//
//
//                });
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        };
//
//    }
//
//
//        private void getLocation() {
//
//            if (android.os.Build.VERSION.SDK_INT >= 26) {
//                if (!GPSOpen()) {
//                    Intent i = new Intent();
//                    i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(i);
//                }
//            }
//
//            Intent intent = new Intent(context, LocationForegroundService.class);
//            isbind = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        }

//    private boolean GPSOpen() {
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        // 通过GPS卫星定位,定位级别可以精确到街(通过24颗卫星定位,在室外和空旷的地方定位准确、速度快)
//        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        // 通过WLAN或移动网络(3G/2G)确定的位置(也称作AGPS,辅助GPS定位。主要用于在室内或遮盖物(建筑群或茂密的深林等)密集的地方定位)
//        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        return gps || network;
//    }


    public static HomeFragment newInstance(String content) {
        Bundle args = new Bundle();
        args.putString("ARGS", content);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
        Toast.makeText(context, address.getProvince() + address.getCity() + address.getDistrict(), Toast.LENGTH_SHORT).show();
        path.put("info", address.getProvince() + address.getCity() + address.getDistrict());
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

}