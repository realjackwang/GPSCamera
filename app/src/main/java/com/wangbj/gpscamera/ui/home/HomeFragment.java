package com.wangbj.gpscamera.ui.home;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.wangbj.gpscamera.FakeLocationService;
import com.wangbj.gpscamera.LocationForegroundService;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.DataUtils;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import static android.content.ContentValues.TAG;
import static com.wangbj.gpscamera.utils.TimeFormat.formatUTC;

public class HomeFragment extends Fragment implements LocationSource, GeocodeSearch.OnGeocodeSearchListener {

    private WeakReference<View> mRootView;
    private Context context;
    private GeocodeSearch.OnGeocodeSearchListener content;

    private boolean isbind;

    private TextView txt;
    private ListView listView;
    private TextView statelite;
    private Button button;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;

    private ProgressBar progressBar;


    private boolean isLocation = false; //是否已经开始定位
    private boolean isPath = false; //是否开始记录轨迹
    private Location lostLocation = null; //上一次的位置，用于画线

    private Path path; //轨迹

    private ServiceConnection connection;
    private LocationForegroundService locationForegroundService;

    private ArrayList<String[]> arrayList;

    private ArrayAdapter arrayAdapter;
    private ACache aCache;

    private MapView mMapView = null;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象


    private LocationSource.OnLocationChangedListener mListener;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

//        View root = inflater.inflate(R.layout.fragment_home, container, false);
        /**
         * 缓存页面
         */
        if (mRootView == null || mRootView.get() == null) {
            View view = inflater.inflate(R.layout.fragment_home, null);
            mRootView = new WeakReference<>(view);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.get().getParent();
            if (parent != null) {
                parent.removeView(mRootView.get());
            }
        }

        initView();
        startService();

        mMapView = mRootView.get().findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        aMap = mMapView.getMap();
        aMap.setLocationSource(this);//通过aMap对象设置定位数据源的监听
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);

        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(16);
        aMap.moveCamera(mCameraUpdate);

        context = getContext();
        content = this;

//        arrayList = new ArrayList<>();
//        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayList);
//        listView.setAdapter(arrayAdapter);

        aCache = ACache.get(context);


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

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocation) {
                    isLocation = true;
                    button5.setText("关闭GPS");
                    getLocation();
                } else {
                    isLocation = false;
                    button5.setText("开启GPS");
                    locationForegroundService.stopGps();
                    if (isbind){
                        context.unbindService(connection);
                        isbind = false;
                    }


                }

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "待完善", Toast.LENGTH_SHORT).show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPath) {
                    arrayList = new ArrayList<>();
                    path = new Path();

                    path.put("starttime",DataUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                    isPath = true;
                    button2.setText("停止记录轨迹");
                    button3.setVisibility(View.GONE);
                    button4.setVisibility(View.GONE);
                } else {
                    isPath = false;
                    button2.setText("开始记录轨迹");
                    button2.setVisibility(View.GONE);
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                }
            }
        });


        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputServer = new EditText(context);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("请输入这次轨迹的标题").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                        .setNegativeButton("取消", null);
                builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        ArrayList<Path> arrayList2 = (ArrayList<Path>) aCache.getAsObject("PathHistory");
                        path.put("id",System.currentTimeMillis());
                        path.put("title",inputServer.getText().toString());
                        path.put("endtime", DataUtils.getCurrentTime("HH:mm:ss"));
                        path.put("path",arrayList);

                        if (arrayList2 != null) {
                            arrayList2.add(path);
                            aCache.put("PathHistory", arrayList2);
                        } else {
                            ArrayList<Path> arrayList3 = new ArrayList<>();
                            arrayList3.add(path);
                            aCache.put("PathHistory", arrayList3);
                        }
                    }
                });
                builder.show();
                button2.setVisibility(View.VISIBLE);
                button3.setVisibility(View.GONE);
                button4.setVisibility(View.GONE);
            }

        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayList = new ArrayList<>();
                path = new Path();
                aMap.clear();

                MyLocationStyle myLocationStyle = new MyLocationStyle();
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
                aMap.setMyLocationStyle(myLocationStyle);

                button2.setVisibility(View.VISIBLE);
                button3.setVisibility(View.GONE);
                button4.setVisibility(View.GONE);

            }
        });


        /**
         * 测试页面刷新是否正常
         final Handler TestHandler = new Handler();
         Runnable TestTimerRun = new Runnable() {
        @Override public void run() {
        arrayList.add("测试"+test);
        test++;
        arrayAdapter.notifyDataSetChanged();
        TestHandler.postDelayed(this,1000);
        }
        };
         TestTimerRun.run();*/

        return mRootView.get();
    }

    private void initPath(){

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }





    private void initView() {
//        button = mRootView.get().findViewById(R.id.button);
//        button1 = mRootView.get().findViewById(R.id.button2);
        button2 = mRootView.get().findViewById(R.id.button3);
        button3 = mRootView.get().findViewById(R.id.button4);
        button4 = mRootView.get().findViewById(R.id.button5);
        button5 = mRootView.get().findViewById(R.id.button6);

        txt = mRootView.get().findViewById(R.id.tv_show);
        statelite = mRootView.get().findViewById(R.id.textView2);
        listView = mRootView.get().findViewById(R.id.listview);
//        progressBar = mRootView.get().findViewById(R.id.progressBar);
    }


    private void startService() {

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "--->onServiceConnected");
                locationForegroundService = ((LocationForegroundService.LocalBinder) service).getLocationForegroundService();

                Location location = locationForegroundService.startGps();
                if (location != null) {

//                    arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
//                    arrayAdapter.notifyDataSetChanged();

                    AMapLocation amapLocation = fromGpsToAmap(location);
                    mListener.onLocationChanged(amapLocation);

                    lostLocation = amapLocation;

                    txt.setText(String.format("经度%s  纬度:%s", location.getLatitude(), location.getLongitude()));
                    String time = formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss");
                    System.out.println(time);

                } else {
                    txt.setText("正在寻找GPS卫星");
                }

                locationForegroundService.setLocationCallback(new LocationForegroundService.LocationCallback() {
                    @Override
                    public void onLocation(Location location) {
                        if (location != null) {

                            AMapLocation amapLocation = fromGpsToAmap(location);
                            mListener.onLocationChanged(amapLocation);

                            if (lostLocation != null) {

                                if (isPath) {
                                    String time = formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss");
                                    String[] strings = {"" + location.getLatitude(), "" + location.getLongitude(), time};

                                    arrayList.add(strings);

                                    List<LatLng> latLngs = new ArrayList<LatLng>();
                                    latLngs.add(new LatLng(lostLocation.getLatitude(), lostLocation.getLongitude()));
                                    latLngs.add(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
                                    aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));

                                    if (path.get("info") == null){
                                        GeocodeSearch geocoderSearch = new GeocodeSearch(context);
                                        geocoderSearch.setOnGeocodeSearchListener(content);

                                        LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
                                        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                                        geocoderSearch.getFromLocationAsyn(regeocodeQuery);
                                    }


                                }

                            }

                            lostLocation = amapLocation;

                            txt.setText(String.format("经度%s  纬度:%s", location.getLatitude(), location.getLongitude()));
                        } else {
                            txt.setText("GPS卫星寻找失败");
                        }
                    }

                    @Override
                    public void onGpsStatue(int status) {

                        switch (status) {
                            //GPS状态为可见时
                            case LocationProvider.AVAILABLE:
                                Toast.makeText(context, "当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                                break;
                            //GPS状态为服务区外时
                            case LocationProvider.OUT_OF_SERVICE:
                                Toast.makeText(context, "当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                                break;
                            //GPS状态为暂停服务时
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                Toast.makeText(context, "当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                                break;
                        }


                    }

                    @Override
                    public void onGpsChanged(int count, int used) {
                        statelite.setText("卫星数：" + used + "/" + count);
                    }


                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

    }


        private void getLocation() {

            if (android.os.Build.VERSION.SDK_INT >= 26) {
                if (!GPSOpen()) {
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                }
            }

            Intent intent = new Intent(context, LocationForegroundService.class);
            isbind = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

        private boolean GPSOpen () {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // 通过GPS卫星定位,定位级别可以精确到街(通过24颗卫星定位,在室外和空旷的地方定位准确、速度快)
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // 通过WLAN或移动网络(3G/2G)确定的位置(也称作AGPS,辅助GPS定位。主要用于在室内或遮盖物(建筑群或茂密的深林等)密集的地方定位)
            boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return gps || network;
        }



        //定义一个更新显示的方法
        private void updateShow (Location location){
            if (location != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("当前的位置信息：\n");
                sb.append("精度：" + location.getLongitude() + "\n");
                sb.append("纬度：" + location.getLatitude() + "\n");
                sb.append("高度：" + location.getAltitude() + "\n");
                sb.append("速度：" + location.getSpeed() + "\n");
                sb.append("方向：" + location.getBearing() + "\n");
                sb.append("定位精度：" + location.getAccuracy() + "\n");
                txt.setText(sb.toString());
            } else txt.setText("");
        }

        public static HomeFragment newInstance (String content){
            Bundle args = new Bundle();
            args.putString("ARGS", content);
            HomeFragment fragment = new HomeFragment();
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public void activate (OnLocationChangedListener onLocationChangedListener){
            mListener = onLocationChangedListener;
        }

        @Override
        public void deactivate () {

        }

        private AMapLocation fromGpsToAmap(Location location){
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


        @Override
        public void onRegeocodeSearched (RegeocodeResult regeocodeResult,int i){
            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
            Toast.makeText(context, address.getProvince()+address.getCity()+address.getDistrict(), Toast.LENGTH_SHORT).show();
            path.put("info",address.getProvince()+address.getCity()+address.getDistrict());
        }

        @Override
        public void onGeocodeSearched (GeocodeResult geocodeResult,int i){

        }
    }