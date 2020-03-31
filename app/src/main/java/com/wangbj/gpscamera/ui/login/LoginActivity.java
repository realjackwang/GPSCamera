package com.wangbj.gpscamera.ui.login;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.alibaba.fastjson.JSONArray;
import com.wangbj.gpscamera.MainActivity;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.httpservice.DownloadService;
import com.wangbj.gpscamera.httpservice.JsonService;
import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.FileUtils;
import com.wangbj.gpscamera.utils.LoadUtils;
import com.wangbj.gpscamera.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Headers;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private Dialog mLoad;
    private int clickTime = 0;
    private int times = -1;
    private java.util.Timer timer = null;
    private static final int REQUESTCODE = 101;
    private ACache aCache;
    private PromptDialog promptDialog;


    private LoadUtils.LoadHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_login);


        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        Button btnLogin = findViewById(R.id.btn_login);
        final EditText etPhone = findViewById(R.id.et_phone);
        final EditText etPwd = findViewById(R.id.et_pwd);
        final CheckBox cbPassword = findViewById(R.id.cb_remember_pwd);
        final CheckBox cbLogin = findViewById(R.id.cb_auto_login);
        Button btnSignUp = findViewById(R.id.btn_sign_up);

        aCache = ACache.get(this);
        promptDialog = new PromptDialog(LoginActivity.this);

        if (aCache.getAsString("CACHE_SERVER") == null) {
            aCache.put("CACHE_SERVER", "http://192.168.123.41:5000");
        }
        mHandler = new LoadUtils.LoadHandler(mLoad);

        btnSignUp.setOnClickListener(new Button.OnClickListener() {  //注册按钮功能
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {    //登录按钮功能
            @Override
            public void onClick(View v) {
                final String username = etPhone.getText().toString();
                final String pwd = etPwd.getText().toString();
                if (username.equals("") || pwd.equals("")) {
                    Toast.makeText(LoginActivity.this, "手机号或密码为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (StringUtils.isPhone(username)) {


                    promptDialog.showLoading("正在登录");

                    if (cbPassword.isChecked()) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("USERNAME", username);
                        editor.putString("PASSWORD", pwd);
                        editor.apply();
                    }

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(username);
                    jsonArray.add(pwd);

                    JsonService.Send(aCache, "/login/", jsonArray, new JsonService.OnHttpListener() {
                        @Override
                        public void onHttpSuccess(final Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    String str = null;
                                    try {
                                        str = response.body().string();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(str);
                                    if ((int) jsonObject.get("code") == 200) {
                                        promptDialog.showSuccess("登陆成功");
                                        Headers headers = response.headers();//response为okhttp请求后的响应
                                        List cookies = headers.values("Set-Cookie");
                                        String session = (String) cookies.get(0);
                                        String sessionId = session.substring(0, session.indexOf(";"));
                                        aCache.put("sessionid", sessionId);
                                        sp.edit().putString("ACCESS", jsonObject.get("access").toString()).apply();

                                        Toast.makeText(LoginActivity.this, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        promptDialog.showSuccess(jsonObject.get("msg").toString());
                                        Toast.makeText(LoginActivity.this, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    promptDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onHttpFailed(Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptDialog.showError("登录失败");
                                    Toast.makeText(LoginActivity.this, "服务器异常，请稍后重试，或更换服务器地址", Toast.LENGTH_SHORT).show();
                                    promptDialog.dismissImmediately();
                                }
                            });
                        }
                    });

                } else {

                    Toast.makeText(LoginActivity.this, "手机号格式错误", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cbLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {      //自动登录按钮
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbLogin.isChecked()) {
                    System.out.println("自动登录已选中");
                    sp.edit().putBoolean("ACHECK", true).commit();

                } else {
                    System.out.println("自动登录没有选中");
                    sp.edit().putBoolean("ACHECK", false).commit();
                }
            }
        });

        cbPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {   //记住密码按钮
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbPassword.isChecked()) {
                    System.out.println("记住密码已选中");
                    sp.edit().putBoolean("CHECK", true).commit();
                } else {
                    System.out.println("记住密码没有选中");
                    sp.edit().putBoolean("CHECK", false).commit();
                }
            }
        });

        if (sp.getBoolean("CHECK", false)) {     //记住密码和自动登录功能
            cbPassword.setChecked(true);
            etPhone.setText(sp.getString("USERNAME", ""));
            etPwd.setText(sp.getString("PASSWORD", ""));

            if (sp.getBoolean("ACHECK", false)) {
                cbLogin.setChecked(true);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }
        requestPermission();
        checkMapData();
    }


    public void onBackPressed() {    //按两次返回退出程序
        clickTime = clickTime + 1;
        if (clickTime == 1 && timer == null) {
            Toast.makeText(LoginActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            timer = new java.util.Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    times = times + 1;
                    if (times == 2) {
                        clickTime = 0;
                        times = -1;
                        timer.cancel();
                        timer = null;
                    }
                }
            }, 0, 1000);
        } else if (clickTime == 2) {
            if (timer != null) {
                timer.cancel();
                timer = null;
                super.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }

    }   //按两次返回退出程序

    public static void Hidden(EditText v, ImageButton s) {         //设置密码可见
        if (v.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            v.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            s.setImageResource(R.drawable.ic_lock_outline);


        } else {
            v.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            s.setImageResource(R.drawable.ic_lock_open);
        }

    }   //设置密码可见

    public void passwordHideAndShow(View view) {
        EditText et_pwd = findViewById(R.id.et_pwd);
        ImageButton ib = findViewById(R.id.imageButton);
        Hidden(et_pwd, ib);
    }  //设置密码可见

    private void checkMapData(){
        String appFilesDir = getExternalFilesDir(null).toString();

        FileUtils.createFile(  appFilesDir+"/aMap");
        if(!FileUtils.checkFile(appFilesDir+"/aMap"+"/style.data")){

            promptDialog.showLoading("正在下载地图数据包1");

            DownloadService.download(aCache, "/file/style.data","style.data",appFilesDir+"/aMap" ,new DownloadService.OnDownloadListener() {
                @Override
                public void onDownloadSuccess(File file) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            promptDialog.showSuccess("下载地图数据包完成");
                        }
                    });
                }

                @Override
                public void onDownloading(int progress) {

                }

                @Override
                public void onDownloadFailed(Exception e) {

                }
            });
        }

        if(!FileUtils.checkFile(appFilesDir+"/aMap"+"/style_extra.data")){
            promptDialog.showLoading("正在下载地图数据包2");
            DownloadService.download(aCache, "/file/style_extra.data","style_extra.data",appFilesDir+"/aMap" ,new DownloadService.OnDownloadListener() {
                @Override
                public void onDownloadSuccess(File file) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            promptDialog.showSuccess("下载地图数据包完成");
                        }
                    });

                }

                @Override
                public void onDownloading(int progress) {

                }

                @Override
                public void onDownloadFailed(Exception e) {

                }
            });
        }

    }

    /**
     * request for the GPS and storage permission
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                & ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 201);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }


    /**
     * Callback for the gps permission
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    case 200:
                        Toast.makeText(LoginActivity.this, "未开启定位权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                        break;
                    case 201:
                        Toast.makeText(LoginActivity.this, "未开启网络定位权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                        break;
                    case 0:
                        Toast.makeText(LoginActivity.this, "未开启存储权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("LoginActivity", e.getMessage());
        }

    }


}