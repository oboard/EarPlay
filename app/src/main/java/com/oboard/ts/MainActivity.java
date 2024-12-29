package com.oboard.ts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    AudioManager audioManager;
    SensorManager sensorManager;
    PowerManager.WakeLock mWakeLock;
    PowerManager mPowerManager;

    Switch cb;//传感开关
    Timer mTimer;//timer
    TimerTask mTimerTask;//timertask
    int mMode;
    String mName = "";
    View ii;
    SeekBar mVolumeBar;
    Sensor mSensor;//传感器
    LinearLayout ll;

    int maxVolume, systemVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.side_down_in, R.anim.side_down_out);

        setContentView(R.layout.main);

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        ii = findViewById(R.id.i);
        cb = findViewById(R.id.mainCheckBox1);
        mVolumeBar = findViewById(R.id.mainSeekBar1);
        ll = findViewById(R.id.mainLinearLayout);

        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @SuppressLint("InvalidWakeLockTag")
            public void onCheckedChanged(CompoundButton view, boolean state) {
                if (state) {
                    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                    mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                    //息屏设置
                    mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "EarPlay:proximity");

                    //注册传感器,先判断有没有传感器
                    if (mSensor != null) {
                        boolean registered = sensorManager.registerListener(MainActivity.this, 
                            mSensor, 
                            SensorManager.SENSOR_DELAY_NORMAL);
                        if (!registered) {
                            cb.setChecked(false);
                            Log.e("MainActivity", "Failed to register sensor listener");
                        }
                    } else {
                        cb.setChecked(false);
                        Log.e("MainActivity", "No proximity sensor found");
                    }
                } else {
                    if (sensorManager != null) {
                        //传感器取消监听
                        sensorManager.unregisterListener(MainActivity.this);
                        Log.d("MainActivity", "Unregistered sensor listener");
                    }
                    //释放息屏
                    if (mWakeLock != null && mWakeLock.isHeld())
                        mWakeLock.release();
                    mWakeLock = null;
                    mPowerManager = null;
                }
            }
        });

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        refreshVolume();
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean b) {
                systemVolume = p;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0);
            }

            public void onStartTrackingTouch(SeekBar sb) {
            }

            public void onStopTrackingTouch(SeekBar sb) {
            }
        });

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            public void run() {
                audioManager.setMode(mMode);
                audioManager.setSpeakerphoneOn(false);
                // 设置为通话状态
                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            }
        };
        mTimer.schedule(this.mTimerTask, 0, 1000);

        mName = getResources().getString(R.string.a);
        setNotification();
    }

    public void refreshVolume() {
        //获取系统的最大声音
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //获取系统当前的声音
        systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //设置最大值
        mVolumeBar.setMax(maxVolume);
        //设置为系统现在的音量
        mVolumeBar.setProgress(systemVolume);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.side_down_in, R.anim.side_down_out);
    }

    @Override
    public void onAccuracyChanged(Sensor p1, int p2) {
        //不能删，删了报错！
    }

    /**
     * 传感器变化
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
            return;
        }

        float distance = event.values[0];
        float maxRange = event.sensor.getMaximumRange();
        
        Log.d("MainActivity", "Proximity changed: " + distance + " (max: " + maxRange + ")");

        if (distance < maxRange) {
            //贴近手机
            //关闭屏幕
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                Log.d("MainActivity", "Screen off - proximity near");
            }
        } else {
            //离开手机
            //点亮屏幕
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                Log.d("MainActivity", "Screen on - proximity far");
            }
        }
    }

    /*
     int num = -1;
     @Override
     protected void onNewIntent(Intent intent) {
     if (intent.getStringExtra("i") != null) {
     ((View)mVolumeBar.getParent()).setVisibility(View.GONE);
     String[] n = new String[] {
     getResources().getString(R.string.a),
     getResources().getString(R.string.b),
     getResources().getString(R.string.c)
     };

     new AlertDialog.Builder(this)
     .setTitle("EarPlay")
     .setIcon(R.mipmap.i)
     .setPositiveButton("确定", new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int which) {
     if (num >= 0)
     onModeChange(new int[] {0, 3, 2, 1}[num]);
     onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
     }
     })
     .setNegativeButton("取消", new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int which) {
     onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
     }
     })
     .setSingleChoiceItems(n, new int[] {0, 3, 2, 1}[mMode], new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int which) {
     num = which;
     }
     })
     .create().show();

     } else {
     ((View)mVolumeBar.getParent()).setVisibility(View.VISIBLE);
     }

     super.onNewIntent(intent);
     }
     */

    @Override
    protected void onDestroy() {

        cancelNotification();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                refreshVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                refreshVolume();
                return true;
            default:
                break;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void finish(View v) {
        overridePendingTransition(R.anim.side_down_in, R.anim.side_down_out);
        moveTaskToBack(true);
    }

    public void onModeChange(View v) {
        onModeChange(Integer.parseInt(v.getTag().toString()));
    }

    public void onModeChange(int i) {
        if (mMode == i)
            return;
        mMode = i;//储存模式
        int[] y = new int[]{0, 3, 2, 1};
        mName = new String[]{
                getResources().getString(R.string.a),
                getResources().getString(R.string.b),
                getResources().getString(R.string.c)
        }[y[i]];
        mbar(ii.getHeight() * y[i]);//指示条
        cancelNotification();//删除通知
        setNotification();//重新设置通知
    }

    public void mbar(float y) {
        //1.设置属性的初始值和结束值
        final ValueAnimator mAnimator = ValueAnimator.ofFloat(ii.getY(), y);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(animation -> ii.setY((float) animation.getAnimatedValue()));
        //3.设置动画的持续时间
        mAnimator.setDuration(250)
                .start();
    }

    // 添加常驻通知
    public void setNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "earplay_channel",
                "EarPlay Notification",
                NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("EarPlay status notification");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("i", "i");
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = new Notification.Builder(this, "earplay_channel");
        } else {
            nb = new Notification.Builder(this);
        }

        nb.setSmallIcon(R.mipmap.i)
                .setOngoing(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.i))
                .setContentIntent(contextIntent)
                .setContentTitle("切换声音输出")
                .setContentText("当前" + mName)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mName.equals(getResources().getString(R.string.a))) {
                nb.setSmallIcon(Icon.createWithResource(this, R.drawable.b));
            } else {
                nb.setSmallIcon(Icon.createWithResource(this, R.drawable.a));
            }
        }

        try {
            notificationManager.notify(0, nb.build());
            Log.d("MainActivity", "Notification posted successfully");
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to post notification: " + e.getMessage());
        }
    }


    public void about(View view) {
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
    }

    // 取消通知
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.weight = 1.0f;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.weight = 0.0f;
        }
        ll.setLayoutParams(lp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
                setNotification();
            }
        }
    }
}
