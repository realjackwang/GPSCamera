package com.wangbj.gpscamera.ui.login;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.httpservice.JsonService;
import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.StringUtils;
import com.wangbj.gpscamera.utils.TimerUtils;


import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import okhttp3.Response;


public class RegisterActivity extends AppCompatActivity {

    private Button btn, send_code;
    private EditText euser, ephone, epwd, eagain, ecode;
    private Context context;
    private ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        btn = (Button) findViewById(R.id.register5);
//        euser = (EditText) findViewById(R.id.register1);
        ephone = (EditText) findViewById(R.id.register2);
        epwd = (EditText) findViewById(R.id.register3);
        eagain = (EditText) findViewById(R.id.register4);
        send_code = findViewById(R.id.send_code);
        ecode = findViewById(R.id.verify_code);
        aCache = ACache.get(this);
        context = this;


        EventHandler eh = new EventHandler() {
            @Override
            public void afterEvent(int event, final int result, final Object data) {

                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE || event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //提交验证成功，跳转成功页面，否则toast提示
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                Toast.makeText(RegisterActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                                Log.e("event", "获取验证码成功");
                            } else {
                                processError(data);
                            }
                        }
                    });
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //提交验证成功，跳转成功页面，否则toast提示
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                Toast.makeText(RegisterActivity.this, "验证成功", Toast.LENGTH_SHORT).show();

                                JSONArray jsonArray = new JSONArray();
                                jsonArray.add(ephone.getText().toString());
                                jsonArray.add(epwd.getText().toString());

                                JsonService.Send(aCache, "/register/", jsonArray, new JsonService.OnHttpListener() {
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
                                                    Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                                    overridePendingTransition(R.anim.activity_sub_enter, R.anim.activity_sub_exit);
                                                    RegisterActivity.this.finish();

                                                } else {
                                                    Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onHttpFailed(Exception e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "服务器异常，请稍后重试，或更换服务器地址", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });


                                Log.e("event", "提交验证码成功");
                            } else {
                                processError(data);
                            }
                        }
                    });
                }
            }
        };

        SMSSDK.registerEventHandler(eh); //注册短信回调


    }

    public void sign_up(View v) {

        if (!isNetworkConnected()) {
            Toast.makeText(this, getString(R.string.smssdk_network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = ephone.getText().toString();
        String pwd = epwd.getText().toString();
        String again = eagain.getText().toString();
        String code = ecode.getText().toString();


        if ((phone.equals("")) || (pwd.equals(""))) {
            Toast.makeText(RegisterActivity.this, "手机号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!StringUtils.isPhone(phone)) {
            Toast.makeText(RegisterActivity.this, "手机号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pwd.equals(again)) {
            Toast.makeText(RegisterActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((pwd.length() < 6) || (pwd.length() > 16)) {
            Toast.makeText(RegisterActivity.this, "密码必须大于等于6位，小于等于16位", Toast.LENGTH_SHORT).show();
            return;
        }


        SMSSDK.submitVerificationCode("86", phone, code);

    }


    public void send(View v) {

        if (!isNetworkConnected()) {
            Toast.makeText(this, getString(R.string.smssdk_network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        final String phone = ephone.getText().toString();
        SMSSDK.getVerificationCode("86", phone);
        TimerUtils timer = new TimerUtils(send_code, 60000, 1000);
        timer.start();
    }

    public void show_user_note(View v) {
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }

    public void show_privacy_note(View v) {
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }


    private void showErrorToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void processError(Object data) {
        int status = 0;
        // 根据服务器返回的网络错误，给toast提示
        try {
            ((Throwable) data).printStackTrace();
            Throwable throwable = (Throwable) data;

            JSONObject object = new JSONObject(
                    throwable.getMessage());
            String des = object.optString("detail");
            status = object.optInt("status");
            if (!TextUtils.isEmpty(des)) {
                showErrorToast(des);
                return;
            }
        } catch (Exception e) {
            Log.w("register", "", e);
        }

    }

    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_sub_enter, R.anim.activity_sub_exit);
    }

}

