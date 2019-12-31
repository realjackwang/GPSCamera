package com.wangbj.gpscamera.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import okhttp3.Response;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.wangbj.gpscamera.LocationForegroundService;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.httpservice.DownloadService;
import com.wangbj.gpscamera.httpservice.TestService;
import com.wangbj.gpscamera.utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.wangbj.gpscamera.utils.TimeFormat.formatUTC;

public class HomeFragment extends Fragment implements LocationSource {

    protected WeakReference<View> mRootView;
    private Context context;

    private TextView txt;
    private ListView listView;
    private TextView statelite;
    private Button button;
    private Button button1;
    private Button button2;
    private Button button3;
    private ProgressBar progressBar;

    private boolean isbind;
    boolean isLocation = false; //是否已经开始定位

    private ServiceConnection connection;
    private LocationForegroundService locationForegroundService;

    private ArrayList<String> arrayList;
    private ArrayAdapter arrayAdapter;


    private MapView mMapView = null;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象


    public AMapLocationClient mlocationClient;
    public AMapLocationClientOption mLocationOption = null;
    private LocationSource.OnLocationChangedListener mListener;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

//        View root = inflater.inflate(R.layout.fragment_home, container, false);
        /**
         * 缓存页面
         */
        if (mRootView == null || mRootView.get() == null) {
            View view = inflater.inflate(R.layout.fragment_home, null);
            mRootView = new WeakReference<View>(view);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.get().getParent();
            if (parent != null) {
                parent.removeView(mRootView.get());
            }
        }


        mMapView = mRootView.get().findViewById(R.id.map);

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        aMap = mMapView.getMap();
        aMap.setLocationSource(this);//通过aMap对象设置定位数据源的监听
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(36.3616386300, 120.6922173500));
        latLngs.add(new LatLng(36.3616688600, 120.6927752500));
        latLngs.add(new LatLng(36.3602130400, 120.6928718100));
        latLngs.add(new LatLng(36.3602908000, 120.6919491300));
        latLngs.add(new LatLng(36.3616386300, 120.6922173500));
        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);

        CameraUpdate mCameraUpdate= CameraUpdateFactory.zoomTo(17);
        aMap.moveCamera(mCameraUpdate);

        context = getContext();

        initView();
        startService();

        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);


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

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocation){
                    isLocation = true;
                    button2.setText("停止记录轨迹");
                    getLocation();
                }
                else{
                    isLocation = false;
                    button2.setText("开始记录轨迹");
                    locationForegroundService.stopGps();
                    context.unbindService(connection);
                }

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "待完善", Toast.LENGTH_SHORT).show();
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


    private void initView() {
//        button = mRootView.get().findViewById(R.id.button);
//        button1 = mRootView.get().findViewById(R.id.button2);
        button2 = mRootView.get().findViewById(R.id.button3);
        button3 = mRootView.get().findViewById(R.id.button4);
        txt = mRootView.get().findViewById(R.id.tv_show);
        statelite = mRootView.get().findViewById(R.id.textView2);
        listView = mRootView.get().findViewById(R.id.listview);
        progressBar = mRootView.get().findViewById(R.id.progressBar);
    }


    private void startService() {

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "--->onServiceConnected");
                locationForegroundService = ((LocationForegroundService.LocalBinder) service).getLocationForegroundService();

                Location location = locationForegroundService.startGps();
                if (location != null) {
                    arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
                    arrayAdapter.notifyDataSetChanged();

                    AMapLocation amapLocation = fromGpsToAmap(location);
                    mListener.onLocationChanged(amapLocation);

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
                            arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
                            arrayAdapter.notifyDataSetChanged();
                            AMapLocation amapLocation = fromGpsToAmap(location);
                            mListener.onLocationChanged(amapLocation);

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
                    public void onGpsChanged(int count,int used) {
                        statelite.setText("卫星数：" + used+"/"+count);
                    }


                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }


    public void getLocation() {



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

    private boolean GPSOpen() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位,定位级别可以精确到街(通过24颗卫星定位,在室外和空旷的地方定位准确、速度快)
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置(也称作AGPS,辅助GPS定位。主要用于在室内或遮盖物(建筑群或茂密的深林等)密集的地方定位)
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    @Override
    public void onDestroy() {
//        if (isbind) {
//            context.unbindService(connection);
//            isbind = false;
//        }
        super.onDestroy();
    }


    //定义一个更新显示的方法
    private void updateShow(Location location) {
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

    public AMapLocation fromGpsToAmap(Location location) {
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


}