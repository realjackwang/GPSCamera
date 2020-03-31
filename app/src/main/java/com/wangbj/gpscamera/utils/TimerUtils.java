package com.wangbj.gpscamera.utils;

/**
 * Created by Jack on 2017/8/29.
 */

import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Jackie on 2015/11/30.
 */
public class TimerUtils extends CountDownTimer {
    private TextView mTextView;

    public TimerUtils(Button textView, long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.mTextView = textView;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mTextView.setClickable(false); //设置不可点击
        mTextView.setText(millisUntilFinished / 1000 + "S后重试");  //设置倒计时时间
    }

    @Override
    public void onFinish() {
        mTextView.setText("发送验证码");
        mTextView.setClickable(true);//重新获得点击
    }


}