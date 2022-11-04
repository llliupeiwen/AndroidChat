package com.example.teambag;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Welcome extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置布局
        setContentView(R.layout.welcome);
    }

    //登录按钮点击事件处理
    public void welcome_login(View view){
        Intent intent = new Intent();
        //页面跳转到登录界面
        intent.setClass(com.example.teambag.Welcome.this,com.example.teambag.LoginUser.class);
        startActivity(intent);
        //结束当前activity
        this.finish();
    }

    //注册按钮点击事件处理
    public void welcome_register(View view){
        Intent intent = new Intent();
        //页面跳转到注册界面
        intent.setClass(com.example.teambag.Welcome.this,com.example.teambag.Register.class);
        startActivity(intent);
        //结束当前activity
        this.finish();
    }
}
