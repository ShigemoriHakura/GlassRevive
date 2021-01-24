package com.hakura.MyNewGlass.Glass;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.glass.companion.Glass;
import com.google.glass.companion.Glass.Envelope;
import com.google.glass.companion.Glass.CompanionInfo;
import com.google.glass.companion.Glass.GlassInfoResponse;
import com.google.glass.companion.Glass.GlassInfoRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlassService extends Service {

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    private OutputStream mOutStream;
    private InputStream mInStream;
    private GlassReaderThread mGlassReaderThread;
    private final ExecutorService mWriteThread = Executors.newSingleThreadExecutor();
    private List<GlassConnectionListener> mListeners = new ArrayList<GlassConnectionListener>();
    private final Object STREAM_WRITE_LOCK = new Object();

    public ServiceBinder mBinder = new ServiceBinder();
    private boolean isConnected;
    private int count = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("MyNewGlass|GlassService", "Started");
        this.registerListener(new GlassConnectionListener() {
            @Override
            public void onReceivedEnvelope(Envelope envelope) {
                //Log.d("GR", "=============onReceivedEnvelope");
                Log.d("MyNewGlass|Get", envelope.toString());

                /*if (envelope.hasGlassInfoResponseG2C()) {
                    final GlassInfoResponse response = envelope.getGlassInfoResponseG2C();
                    String info = "";
                    info += "Device name: " + response.getDeviceName() + "\n";
                    info += "Battery: " + response.getBatteryLevel() + "%" + "\n";
                    info += "Software: " + response.getSoftwareVersion() + "\n";
                    info += "Storage: " + response.getExternalStorageAvailableBytes() / 1000 / 1000 + "/" + response.getExternalStorageTotalBytes() / 1000 / 1000
                            + " MB available";
                    Log.d("MyNewGlass|GlassService", info);
                }
                if (envelope.getCompanionInfo() != null) {
                    CompanionInfo companionInfo = envelope.getCompanionInfo();
                    String log = companionInfo.getResponseLog();
                    System.out.println(log);
                }*/
            }

            @Override
            public void onConnectionOpened() {
                // TODO Auto-generated method stub

            }
        });
        getDeviceAndConnect();
    }

    public void getDeviceAndConnect() {
        mDevice = getDevice();
        if (mDevice == null) {
            Log.d("MyNewGlass|GlassService", "No Glass Paired");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getDeviceAndConnect();
                }
            }, 1000);
        } else {
            sendBroadcastStatus("Found " + mDevice.getName());
            try {
                connect(mDevice);
                sendBroadcastStatus("Connected");
            } catch (IOException e) {
                e.printStackTrace();
                //要处理错误
            }
        }
    }

    public interface GlassConnectionListener {
        public abstract void onReceivedEnvelope(Envelope envelope);

        public abstract void onConnectionOpened();
    }

    public void connect(BluetoothDevice d) throws IOException {
        mSocket = d.createRfcommSocketToServiceRecord(UUID.fromString("F15CC914-E4BC-45CE-9930-CB7695385850"));
        mSocket.connect();
        mOutStream = mSocket.getOutputStream();
        mInStream = mSocket.getInputStream();
        GlassReaderThread mGlassReaderThread = new GlassReaderThread();
        mGlassReaderThread.start();
        handshake();

        isConnected = true;
        //tell the people!
        synchronized (mListeners) {
            for (GlassConnectionListener listener : mListeners) {
                listener.onConnectionOpened();
            }
        }
    }

    public void handshake() {
        // handshaking
        Envelope envelope = GlassUtil.newEnvelope()
                .toBuilder()
                .setTimezoneC2G(TimeZone.getDefault().getID())
                .build();
        writeAsync(envelope);

        // handshaking
        GlassInfoRequest glassInfoRequest = GlassInfoRequest.newBuilder()
                .setRequestBatteryLevel(true)
                .setRequestStorageInfo(true)
                .setRequestDeviceName(true)
                .setRequestSoftwareVersion(true)
                .build();
        Envelope envelope2 = GlassUtil.newEnvelope()
                .toBuilder()
                .setGlassInfoRequestC2G(glassInfoRequest)
                .build();
        writeAsync(envelope2);

        Envelope envelope3 = GlassUtil.newEnvelope();
        writeAsync(envelope3);

        /*LocationManager pLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }else{
            Log.d("MyNewGlass|GlassService", "Sending Location");
            Location lastKnownLocation = pLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Glass.Location Location = Glass.Location.newBuilder()
                    .setAltitude(lastKnownLocation.getAltitude())
                    .setAccuracy(lastKnownLocation.getAccuracy())
                    .setLatitude(lastKnownLocation.getLatitude())
                    .setLongitude(lastKnownLocation.getLongitude())
                    .setTime(System.currentTimeMillis())
                    .build();

            Glass.LocationMessage LocationMessage = Glass.LocationMessage.newBuilder()
                    .setLocation(Location)
                    .setType(Glass.LocationMessage.MessageType.LOCATION_CHANGED)
                    .setProvider("gps")
                    .build();

            Envelope envelope3 = GlassUtil.newEnvelope()
                    .toBuilder()
                    .setLocationMessageC2G(LocationMessage)
                    .build();
            writeAsync(envelope3);
        }*/

        try {
            Thread.sleep(300);
            isConnected = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void write(Envelope envelope) {
        synchronized (STREAM_WRITE_LOCK) {
            try {
                //System.out.println("write:" + envelope);
                if (mOutStream != null) {
                    GlassProtocol.writeMessage(envelope, mOutStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeAndReconnect();
            }
        }
    }

    public void writeAsync(final Envelope envelope) {
        mWriteThread.execute(new Runnable() {
            @Override
            public void run() {
                write(envelope);
            }
        });
    }

    public void writeAsync(final List<Envelope> envelopes) {
        mWriteThread.execute(new Runnable() {
            @Override
            public void run() {
                for (Envelope envelope : envelopes) {
                    write(envelope);
                }
            }
        });
    }

    public void close() {
        if (mGlassReaderThread != null) {
            mGlassReaderThread.interrupt();
            try {
                mGlassReaderThread.join(10000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
                mOutStream = null;
                mInStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeAndReconnect(){
        isConnected = false;
        close();
        getDeviceAndConnect();
    }

    public void registerListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    return;
                }
            }
            this.mListeners.add(glassConnectionListener);
        }
    }

    public void unregisterListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    mListeners.remove(i);
                    break;
                }
            }
        }
    }

    private class GlassReaderThread extends Thread {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Envelope envelope = (Envelope) GlassProtocol.readMessage(Envelope.newBuilder().setVersion(Integer.valueOf(0)).build(), mInStream);
                        if (envelope != null) {
                            synchronized (mListeners) {
                                for (GlassConnectionListener listener : mListeners) {
                                    listener.onReceivedEnvelope(envelope);
                                }
                            }
                        }
                    } catch (InterruptedIOException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Reader thread finished.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    };

    private void sendBroadcastStatus(String Status) {
        Intent intent = new Intent("com.hakura.MyNewGlass.Status");
        intent.putExtra("status", Status);
        this.sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        super.onStartCommand(intent, flags, startid);
        return START_STICKY;
    }

    public BluetoothDevice getDevice(){
        BluetoothDevice device = null;
        Set<BluetoothDevice> devices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (final BluetoothDevice d : devices){
            if (d.getName().contains("Glass")){
                Log.d("MyNewGlass|GlassService", "Found " + d.getName());
                device = d;
                break;
            }
        }
        return device;
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
            if(isConnected){
                writeAsync(GlassUtil.returnText(subject, text, Bid, id, expirationTime));
            }else{
                Log.d("MyNewGlass|GlassService", "mSocket Not Connected");
            }
        }
    }
}
