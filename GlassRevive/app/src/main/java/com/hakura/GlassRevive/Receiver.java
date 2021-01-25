package com.hakura.GlassRevive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("MyNewGlass|Receive", "handle action: "+intent.getAction());
        Intent i = new Intent(context, GlassService.class);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            context.startService(i);
        }
    }
}
