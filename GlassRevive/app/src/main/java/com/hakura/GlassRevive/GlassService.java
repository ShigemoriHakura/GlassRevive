package com.hakura.GlassRevive;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

public class GlassService extends Service {
    public ServiceBinder mBinder = new ServiceBinder();

    public void onCreate() {
        super.onCreate();
        Log.v("GlassService", "Started");
        searchAndConnect();
    }

    public void searchAndConnect(){
        BluetoothClient mClient = new BluetoothClient(this);
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                //Beacon beacon = new Beacon(device.scanRecord);
                BluetoothLog.v(String.format("get %s\n%s", device.getAddress(), device.getName()));
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        public void sendText(String subject, String text, String Bid, String id, int expirationTime) throws Exception {

        }
    }

}
