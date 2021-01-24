package com.hakura.MyNewGlass;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hakura.MyNewGlass.Glass.GlassService;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private GlassService.ServiceBinder mBinderService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderService = (GlassService.ServiceBinder) service;
        }
    };

    public Activity context;
    public static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        sharedPreferences = getSharedPreferences("MyNewGlass", MODE_PRIVATE);
        int expirationTime = sharedPreferences.getInt("expirationTime",60 * 5);
        TextView tv = findViewById(R.id.TextView_ExpTime);
        tv.setText(expirationTime + " S");
        SeekBar exp = findViewById(R.id.SeekBar_Exp);
        exp.setProgress(expirationTime);

        exp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("expirationTime", progress);
                editor.apply();
                TextView tv = findViewById(R.id.TextView_ExpTime);
                tv.setText(progress + " S");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
            }
        });

        //startService(new Intent(this, GlassService.class));
        Intent bindIntent = new Intent(MainActivity.this, GlassService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束服务
        if(connection != null){
            unbindService(connection);
        }

    }

    public void onTestButtonClick(View v) {
        try {
            EditText text = (EditText)findViewById(R.id.InputMessage);
            int expirationTime =  sharedPreferences.getInt("expirationTime",60 * 5);
            mBinderService.sendText("Hi", text.getText().toString(), "com.hakura.MyNewGlass",  UUID.randomUUID().toString(),expirationTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGivePermissionButtonClick(View v) {
        Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent_s);
    }

    public void onReGivePermissionButtonClick(View v) {
        toggleNotificationListenerService();
        Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
    }

    public void toggleNotificationListenerService(){
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, com.hakura.MyNewGlass.NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, com.hakura.MyNewGlass.NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}