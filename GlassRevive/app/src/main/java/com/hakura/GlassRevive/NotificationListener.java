package com.hakura.GlassRevive;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NotificationListener extends NotificationListenerService {

    @Override
    public void onCreate() {
        Log.i("NListener", "Service started-----");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn.getNotification().tickerText != null) {
                String nTitle = sbn.getNotification().extras.getString("android.title");
                String nMessage = sbn.getNotification().extras.getString("android.text");
                Log.d("NListener", "Get Message" + "-----" + nMessage);
                SharedPreferences sharedPreferences = getSharedPreferences("MyNewGlass", MODE_PRIVATE);
                int expirationTime = sharedPreferences.getInt("expirationTime",60 * 5);
                boolean enableTTS = sharedPreferences.getBoolean("enableTTS",false);
                Intent intent = new Intent("com.hakura.GlassRevive.timeline");
                intent.putExtra("title", nTitle);
                intent.putExtra("text", nMessage);
                intent.putExtra("uuid", UUID.randomUUID().toString());
                intent.putExtra("bid", sbn.getPackageName());
                intent.putExtra("expirationTime", expirationTime);
                intent.putExtra("enableTTS", enableTTS);
                sendBroadcast(intent);
            }
        } catch (Exception e) {
            Log.e("NListener|Error", e.getLocalizedMessage());
            //Toast.makeText(MainActivity.context, "不可解析的通知", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("GlassRevive|NListener", "shut down-----" + sbn.toString());
    }

}