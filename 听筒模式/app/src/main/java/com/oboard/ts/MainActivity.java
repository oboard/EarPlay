package com.oboard.ts;

import android.app.Activity;
import android.os.Bundle;
import android.media.AudioManager;
import android.widget.Button;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;
import android.animation.ValueAnimator;
import android.widget.Toast;
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import java.lang.reflect.Field;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.graphics.PixelFormat;
import android.widget.CheckBox;
import android.content.Intent;

public class MainActivity extends Activity {
    AudioManager audioManager;
    
    public static Boolean canp = false,canj = false;
    
    Timer mTimer;TimerTask mTimerTask;
    int mMode;View ii;//Button i2,i3,i4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ii = findViewById(R.id.i);

        Intent i = new Intent(this,HideService.class);
        startService(i);
        
        /*
         i2 = (Button)findViewById(R.id.l1);
         i3 = (Button)findViewById(R.id.l2);
         i4 = (Button)findViewById(R.id.l3);
         */
        audioManager = (AudioManager)this.getSystemService("audio");
        
        this.mTimer = new Timer();
        this.mTimerTask = new TimerTask(){

            public void run() {
                audioManager.setMode(mMode);
                /*
                 if (mMode==0) {
                 audioManager.setMode(2);
                 }
                 */
                audioManager.setSpeakerphoneOn(false);
                // 设置为通话状态
                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            }
        };
        this.mTimer.schedule(this.mTimerTask, 0, 1000);
    }
    public void tt(View v) {
        mMode = 3;
        mbar(v.getY());
    } public void lb(View v) {
        mMode = 0;
        mbar(v.getY());
    } public void jt(View v) {
        mMode = 2;
        mbar(v.getY());
    } public void ck(View v) {
        canj = !canj;
        
    }
    /*
     @Override
     protected void onResume() {
     //shortcuts
     if (getIntent().getAction().equals("c")) {
     lb(i2);
     } else if (getIntent().getAction().equals("d")) {
     mMode = 3;
     mbar(i3.getY());
     } else if (getIntent().getAction().equals("e")) {
     mMode = 2;
     mbar(i4.getY());
     }
     super.onResume();
     } 

     */

    public boolean onCreateOptionsMenu(Menu menu) { 
        super.onCreateOptionsMenu(menu); 
        //  getActionBar().setNavigationMode(getActionBar()..);  
        // MenuItem about = menu.add(0, 1, 0, "关于"); 
        //  MenuItem open = menu.add(0, 2, 1, "Open"); 
        //  MenuItem close = menu.add(0, 3, 2, "Close"); 
        //   about.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM); 
        getMenuInflater().inflate(R.menu.main, menu);
        return true; 
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gy:
                AlertDialog.Builder normalDia=new AlertDialog.Builder(this); 
                normalDia.setTitle("关于"); 
                normalDia.setMessage("谢谢您对本软件的支持 \n\n开发者\n 一块小板子 2232442466 \n鸣谢\n 小青光 1664147500"); 

                normalDia.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
                        @Override 
                        public void onClick(DialogInterface dialog, int which) {  

                        } 
                    }); /*
                 normalDia.setNegativeButton("取消", new DialogInterface.OnClickListener() { 
                 @Override 
                 public void onClick(DialogInterface dialog, int which) { 

                 } 
                 }); */
                normalDia.create().show(); 
                break;
                //case R.id.jz:

                //  break;
        }
        return super.onMenuItemSelected(featureId, item);
    } 

    public void mbar(float y) {
        //1.设置属性的初始值和结束值
        ValueAnimator mAnimator = ValueAnimator.ofFloat(ii.getY(), y);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ii.setY(animation.getAnimatedValue());
                }
            });
        //3.设置动画的持续时间
        mAnimator.setDuration(250);
		mAnimator.start();
    }
    
    @Override
    protected void onResume() {
        //shortcuts
        if (getIntent().getAction().equals("c")) {
            
        } else if (getIntent().getAction().equals("d")) {
            mMode = 3;
        } else if (getIntent().getAction().equals("e")) {
            mMode = 2;
        }
        
        super.onResume();
    }

}
