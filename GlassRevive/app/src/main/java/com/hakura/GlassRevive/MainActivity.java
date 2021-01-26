package com.hakura.GlassRevive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sharedPreferences;
    private ServiceReceiver ServiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyNewGlass", MODE_PRIVATE);
        int expirationTime = sharedPreferences.getInt("expirationTime",60 * 5);
        boolean enableTTS = sharedPreferences.getBoolean("enableTTS",false);
        Switch sw = findViewById(R.id.Switch_EnableTTS);
        sw.setChecked(enableTTS);

        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("enableTTS", isChecked);
            editor.apply();
        });

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

        ServiceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hakura.GlassRevive.RECEIVER");
        registerReceiver(ServiceReceiver, intentFilter);

        startService(new Intent(this, GlassService.class));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(ServiceReceiver);
        super.onDestroy();
    }

    public void onTestButtonClick(View v) {
        try {
            EditText text = (EditText)findViewById(R.id.InputMessage);
            int expirationTime =  sharedPreferences.getInt("expirationTime",60 * 5);
            boolean enableTTS = sharedPreferences.getBoolean("enableTTS",false);
            Intent intent = new Intent("com.hakura.GlassRevive.timeline");
            intent.putExtra("title", "Hi");
            intent.putExtra("text", text.getText().toString());
            intent.putExtra("uuid", UUID.randomUUID().toString());
            intent.putExtra("expirationTime", expirationTime);
            intent.putExtra("enableTTS", enableTTS);
            sendBroadcast(intent);
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
        pm.setComponentEnabledSetting(new ComponentName(this, com.hakura.GlassRevive.NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, com.hakura.GlassRevive.NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainReceiver", intent.toString());
            TextView tv = null;
            switch (intent.getStringExtra("action")){
                case "Status":
                    tv = findViewById(R.id.TextView_Status);
                    tv.setText(intent.getStringExtra("envelope"));
                    break;
                case "Log":
                    tv = findViewById(R.id.TextView_Log);
                    tv.setText(intent.getStringExtra("envelope"));
                    break;
            }
        }

    }

}