package com.hakura.GlassRevive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NotificationListener extends NotificationListenerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GR_Notify", "Service is started" + "-----");
        String data = intent.getStringExtra("data");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            //有些通知不能解析出TEXT内容，这里做个信息能判断
            if (sbn.getNotification().tickerText != null) {
                //Log.i("GR_Notify", "get"+"-----"+sbn.getNotification().toString());
                SharedPreferences sp = getSharedPreferences("msg", MODE_PRIVATE);
                String nTitle = sbn.getNotification().extras.getString("android.title");
                String nMessage = sbn.getNotification().extras.getString("android.text");

                /*Log.d("GR_Notify","CharSequence - Title: "+sbn.getNotification().extras.getCharSequence("android.title")+
                        " CharSequence - textLines: "+sbn.getNotification().extras.getCharSequence("android.textLines")+
                        " CharSequence - subText: "+sbn.getNotification().extras.getCharSequence("android.subText")+
                        " CharSequence - text: "+sbn.getNotification().extras.getCharSequence("android.text")+
                        " CharSequence - infoText: "+sbn.getNotification().extras.getCharSequence("android.infoText")+
                        " CharSequence - summaryText: "+sbn.getNotification().extras.getCharSequence("android.summaryText"));

                Log.d("GR_Notify","String - Title: "+sbn.getNotification().extras.getString("android.title")+
                        " String - textLines: "+sbn.getNotification().extras.getString("android.textLines")+
                        " String - subText: "+sbn.getNotification().extras.getString("android.subText")+
                        " String - text: "+sbn.getNotification().extras.getString("android.text")+
                        " String - infoText: "+sbn.getNotification().extras.getString("android.infoText")+
                        " String - summaryText: "+sbn.getNotification().extras.getString("android.summaryText"));*/
                //Log.e("GR_Notify", "Get Message" + "-----" + nMessage);
                MyGlassUtils.sendText(nTitle, nMessage, sbn.getPackageName());
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.context, "不可解析的通知", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("GR_Notify", "shut"+"-----"+sbn.toString());
    }

}

