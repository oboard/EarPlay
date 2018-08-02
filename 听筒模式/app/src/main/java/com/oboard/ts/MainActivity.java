package com.oboard.ts;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    AudioManager audioManager;
    SensorManager sensorManager;
    PowerManager.WakeLock mWakeLock;
    PowerManager mPowerManager;

    CheckBox cb;//传感开关
    Timer mTimer;//timer
    TimerTask mTimerTask;//timertask
    int mMode;
    View ii;
    Sensor mSensor;//传感器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //息屏设置
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "");


        ii = findViewById(R.id.i);
        cb = (CheckBox)findViewById(R.id.mainCheckBox1);

        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton view, boolean state) {
                    if (state) {
                        //注册传感器,先判断有没有传感器
                        if (mSensor != null)
                            sensorManager.registerListener(MainActivity.this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    } else {
                        //传感器取消监听
                        sensorManager.unregisterListener(MainActivity.this);
                        //释放息屏
                        if (mWakeLock.isHeld())
                            mWakeLock.release();
                        mWakeLock = null;
                        mPowerManager = null;
                    }
                }
            });



        audioManager = (AudioManager)this.getSystemService("audio");

        mTimer = new Timer();
        mTimerTask = new TimerTask(){
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
        mTimer.schedule(this.mTimerTask, 0, 1000);
    }

    /**
     * 传感器变化
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0.0) {
            //贴近手机
            //设置免提
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //关闭屏幕
            if (!mWakeLock.isHeld())
                mWakeLock.acquire();

        } else {
            //离开手机
            audioManager.setMode(AudioManager.MODE_NORMAL);
            //设置免提
            audioManager.setSpeakerphoneOn(true);

            //唤醒设备
            if (mWakeLock.isHeld())
                mWakeLock.release();
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor p1, int p2) {

    }


    public void onModeChange(View v) {
        mMode = Integer.parseInt(v.getTag().toString());
        mbar(v.getY());
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
                new AlertDialog.Builder(this)
                    .setTitle("关于")
                    .setMessage("谢谢您对本软件的支持 \n\n开发者\n 一块小板子 2232442466 \n鸣谢\n 小青光 1664147500")
                    .setPositiveButton("确定", null)
                    .setNegativeButton("不确定", new DialogInterface.OnClickListener() { 
                        @Override 
                        public void onClick(DialogInterface dialog, int which) { 
                            finish();
                        } 
                    })
                    .create().show(); 
                break;
                //case R.id.jz:

                //  break;
        }
        return super.onMenuItemSelected(featureId, item);
    } 

    public void mbar(float y) {
        //1.设置属性的初始值和结束值
        final ValueAnimator mAnimator = ValueAnimator.ofFloat(ii.getY(), y);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ii.setY(animation.getAnimatedValue());
                }
            });
        //3.设置动画的持续时间
        mAnimator.setDuration(250)
            .start();
    }
    /*
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
     */
}
