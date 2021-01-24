package com.hakura.MyNewGlass;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ServiceUtil {
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        //获取所有的服务
        List<ActivityManager.RunningServiceInfo> services= activityManager.getRunningServices(Integer.MAX_VALUE);
        if(services!=null&&services.size()>0){
            for(ActivityManager.RunningServiceInfo service : services){
                if(className.equals(service.service.getClassName())){
                    isRunning=true;
                    break;
                }
            }
        }

        return isRunning;
    }
}
