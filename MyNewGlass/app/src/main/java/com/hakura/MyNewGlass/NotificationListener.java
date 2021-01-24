package com.hakura.MyNewGlass;

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

import com.hakura.MyNewGlass.Glass.GlassService;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NotificationListener extends NotificationListenerService {

    private GlassService.ServiceBinder mBinderService;
    private boolean isConnected = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderService = (GlassService.ServiceBinder) service;
            isConnected = true;
        }
    };

    @Override
    public void onCreate() {
        Log.i("MyNewGlass|NListener", "Service started-----");
        Intent bindIntent = new Intent(NotificationListener.this, GlassService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            //有些通知不能解析出TEXT内容，这里做个信息能判断
            //思考要不要把sdk版本提上去
            if (sbn.getNotification().tickerText != null) {
                String nTitle = sbn.getNotification().extras.getString("android.title");
                String nMessage = sbn.getNotification().extras.getString("android.text");
                Log.d("MyNewGlass|NListener", "Get Message" + "-----" + nMessage);
                SharedPreferences sharedPreferences = getSharedPreferences("MyNewGlass", MODE_PRIVATE);
                int expirationTime = sharedPreferences.getInt("expirationTime",60 * 5);
                if(isConnected){
                    mBinderService.sendText(nTitle, nMessage, sbn.getPackageName(), UUID.randomUUID().toString(), expirationTime);
                }else{
                    startService(new Intent(this, GlassService.class));
                }

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
                //MyGlassUtils.sendText(nTitle, nMessage, sbn.getPackageName(), UUID.randomUUID().toString(), expirationTime);
            }
        } catch (Exception e) {
            Log.e("GR_Notify_Error", e.getLocalizedMessage());
            //Toast.makeText(MainActivity.context, "不可解析的通知", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("MyNewGlass|NListener", "shut down-----" + sbn.toString());
    }

}