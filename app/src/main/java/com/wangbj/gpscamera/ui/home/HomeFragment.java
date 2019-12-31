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

import com.wangbj.gpscamera.LocationForegroundService;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.httpservice.DownloadService;
import com.wangbj.gpscamera.httpservice.TestService;
import com.wangbj.gpscamera.utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    protected WeakReference<View> mRootView;

    private HomeViewModel homeViewModel;
    private Context context;
    private LocationManager lm;
    private TextView txt;
    private ListView listView;
    private TextView statelite;
    private boolean isbind;
    private ServiceConnection connection;


    private ArrayList<String> arrayList;
    private ArrayAdapter arrayAdapter;
//    private int test = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

//        View root = inflater.inflate(R.layout.fragment_home, container, false);
        if (mRootView == null || mRootView.get() == null) {
            View view=inflater.inflate(R.layout.fragment_home, null);
            mRootView = new WeakReference<View>(view);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.get().getParent();
            if (parent != null) {
                parent.removeView(mRootView.get());
            }
        }



        context = getContext();
        Button button =  mRootView.get().findViewById(R.id.button);
        Button button1 =  mRootView.get().findViewById(R.id.button2);
        Button button2 =  mRootView.get().findViewById(R.id.button3);
        Button button3 =  mRootView.get().findViewById(R.id.button4);

        txt =  mRootView.get().findViewById(R.id.tv_show);
        statelite =  mRootView.get().findViewById(R.id.textView2);
        listView =  mRootView.get().findViewById(R.id.listview);
        final ProgressBar progressBar =  mRootView.get().findViewById(R.id.progressBar);


        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "--->onServiceConnected");
                LocationForegroundService locationForegroundService = ((LocationForegroundService.LocalBinder) service).getLocationForegroundService();
                Location location = locationForegroundService.gps();
                if (location != null) {
                    arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
                    arrayAdapter.notifyDataSetChanged();
                    updateShow(location);
//                    txt.setText("经度" + location.getLatitude() + "  纬度:" + location.getLongitude());
                } else {
                    txt.setText("正在寻找GPS卫星");
                }

                locationForegroundService.setLocationCallback(new LocationForegroundService.LocationCallback() {
                    @Override
                    public void onLocation(Location location) {
                        if (location != null) {
                            arrayList.add("经度" + location.getLatitude() + "纬度:" + location.getLongitude());
                            arrayAdapter.notifyDataSetChanged();
                            updateShow(location);
//                            txt.setText("经度" + location.getLatitude() + "  纬度:" + location.getLongitude());
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
                    public void onGpsChanged(int count) {
                        statelite.setText("卫星数："+count);
                    }


                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestService.test(new TestService.OnHttpListener() {
                    @Override
                    public void onHttpSuccess(Response response) {

                    }

                    @Override
                    public void onHttpFailed(Exception e) {
                        Looper.prepare();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
            }
        });


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://192.168.137.1:5000/file/test.zip";
                DownloadService.download(url, new DownloadService.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(final File file) {

                        final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(context);

                        alterDiaglog.setTitle("下载完成");
                        alterDiaglog.setMessage("是否打开文件？");

                        alterDiaglog.setPositiveButton("打开", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FileUtils.openFile(file);

                            }
                        });

                        alterDiaglog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        Looper.prepare();
                        alterDiaglog.show();
                        Looper.loop();


                    }

                    @Override
                    public void onDownloading(int progress) {
                        progressBar.setProgress(progress);


                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Looper.prepare();
                        Toast.makeText(context, "服务器无法连接", Toast.LENGTH_SHORT).show();
                        Looper.loop();

                    }
                });
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(v);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "待完善", Toast.LENGTH_SHORT).show();
            }
        });


//        final Handler TestHandler = new Handler();
//        Runnable TestTimerRun = new Runnable() {
//            @Override
//            public void run() {
//                arrayList.add("测试"+test);
//                test++;
//                arrayAdapter.notifyDataSetChanged();
//                TestHandler.postDelayed(this,1000);
//            }
//        };
//        TestTimerRun.run();

        return  mRootView.get();
    }


    public void getLocation(View view) {

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


}