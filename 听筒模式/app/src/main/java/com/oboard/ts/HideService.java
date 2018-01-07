package com.oboard.ts;
import android.app.Service;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.widget.RelativeLayout;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.content.Context;
import android.hardware.SensorEvent;
import android.view.LayoutInflater;
import android.os.IBinder;
import android.content.Intent;
import android.graphics.PixelFormat;

public class HideService extends Service implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensor;
    RelativeLayout mDesktopLayout;
    WindowManager mWindowManager;
    LayoutParams mLayoutParams;

    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mDesktopLayout = new RelativeLayout(this);
        // 取得系统窗体
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
    
        
        super.onCreate();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
	}

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if(MainActivity.canj) {
            float range = event.values[0];
            if (range == sensor.getMaximumRange()) {
                if(MainActivity.canp) {
                    mWindowManager.removeView(mDesktopLayout);
                    MainActivity.canp = false;
                }
            } else {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                MainActivity.canp = true;
                // 窗体的布局样式
                mLayoutParams = new WindowManager.LayoutParams();
                // 设置窗体显示类型――TYPE_SYSTEM_ALERT(系统提示)
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                // 设置窗体焦点及触摸：
                // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
                mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                // 设置显示的模式
                mLayoutParams.format = PixelFormat.RGBA_8888;
                // 设置窗体宽度和高度
                mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                // 设置窗体显示的位置，否则在屏幕中心显示
                mLayoutParams.x = 0;
                mLayoutParams.y = 0;
                mWindowManager.addView(mDesktopLayout, mLayoutParams);
            }
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onStart(intent,startId);
    }
    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
       
    }
    
    
}
