package com.example.teambag;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class AppStart extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置布局
        setContentView(R.layout.app_start);
        //延迟跳转，显示欢迎图片背景
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //页面跳转到启动界面
                Intent intent = new Intent(com.example.teambag.AppStart.this,com.example.teambag.Welcome.class);
                startActivity(intent);
                //结束当前activity
                com.example.teambag.AppStart.this.finish();
            }
        },1000);
    }
}
