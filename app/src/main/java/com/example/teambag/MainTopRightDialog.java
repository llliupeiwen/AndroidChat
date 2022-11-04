package com.example.teambag;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class MainTopRightDialog extends Activity {
    private LinearLayout layout;
    private Animation animation1;
    private Animation animation2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_top_right_dialog);
        //设置对话框activity的宽度等于屏幕宽度，一定要设置，不然对话框会显示不全
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//需要添加的语句
        layout=(LinearLayout)findViewById(R.id.main_dialog_layout);
        animation1=AnimationUtils.loadAnimation(this,R.anim.push_top_in2);
        //layout.startAnimation(animation1);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        animation2=AnimationUtils.loadAnimation(this,R.anim.push_top_out2);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        layout.startAnimation(animation2);
        return true;
    }

    @Override
    public void finish(){

        super.finish();
    }
}
