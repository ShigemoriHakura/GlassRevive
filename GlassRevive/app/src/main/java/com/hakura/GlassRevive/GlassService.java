package com.hakura.GlassRevive;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.glass.companion.Glass;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hakura.GlassRevive.Glass.GlassProtocol;

import com.google.glass.companion.Glass;
import com.google.glass.companion.Glass.Envelope;
import com.google.glass.companion.Glass.CompanionInfo;
import com.google.glass.companion.Glass.GlassInfoResponse;
import com.google.glass.companion.Glass.GlassInfoRequest;
import com.hakura.GlassRevive.Glass.GlassUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GlassService extends Service {

    private BluetoothAdapter btAdapter = null;

    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    private boolean stopThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("F15CC914-E4BC-45CE-9930-CB7695385850");
    private Intent intent = new Intent("com.hakura.GlassRevive.RECEIVER");

    private TimelineReceiver TimelineReceiver;

    public class TimelineReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d("TimelineReceiver", intent.toString());
                mConnectedThread.writeAsync(GlassUtil.returnText(
                        intent.getStringExtra("title"),
                        intent.getStringExtra("text"),
                        intent.getStringExtra("bid"),
                        intent.getStringExtra("uuid"),
                        intent.getIntExtra("expirationTime", 300),
                        intent.getBooleanExtra("enableTTS", false)
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");
        stopThread = false;

        TimelineReceiver = new TimelineReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hakura.GlassRevive.timeline");
        registerReceiver(TimelineReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(TimelineReceiver);
        stopThread = true;
        if (mConnectedThread != null) {
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null) {
            mConnectingThread.closeSocket();
        }
        Log.d("SERVICE", "onDestroy");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startService(new Intent(this,GlassService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendIntent(String action, String text){
        intent.putExtra("action", action);
        intent.putExtra("envelope", text);
        sendBroadcast(intent);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            sendIntent("Status", "BLUETOOTH NOT SUPPORTED");
            Log.d("BT SERVICE", "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
            stopSelf();
        } else {
            if (btAdapter.isEnabled()) {
                sendIntent("Status", "Searching");
                Log.d("DEBUG BT", "BT ENABLED! BT ADDRESS : " + btAdapter.getAddress() + " , BT NAME : " + btAdapter.getName());
                try {
                    BluetoothDevice device = null;
                    Set<BluetoothDevice> devices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

                    for (final BluetoothDevice d : devices){
                        if (d.getName().contains("Glass")){
                            sendIntent("Status", "Found " + d.getName());
                            Log.d("DEBUG BT","Talking to " + d.getName());
                            device = d;
                            break;
                        }
                    }
                    if(device != null){
                        mConnectingThread = new ConnectingThread(device);
                        mConnectingThread.start();
                    }else{
                        sendIntent("Status", "No Devices");
                        Log.d("BT SEVICE", "No Devices");
                        stopSelf();
                    }
                } catch (IllegalArgumentException e) {
                    Log.d("DEBUG BT", "PROBLEM WITH MAC ADDRESS : " + e.toString());
                    Log.d("BT SEVICE", "ILLEGAL MAC ADDRESS, STOPPING SERVICE");
                    stopSelf();
                }
            } else {
                Log.d("BT SERVICE", "BLUETOOTH NOT ON, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connecting Thread
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device) {
            sendIntent("Status", "Connecting");
            Log.d("DEBUG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket temp = null;
            Log.d("DEBUG BT", "BT UUID : " + BTMODULEUUID);
            try {
                temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                sendIntent("Status", "SOCKET CREATED");
                Log.d("DEBUG BT", "SOCKET CREATED : " + temp.toString());
            } catch (IOException e) {
                Log.d("DEBUG BT", "SOCKET CREATION FAILED :" + e.toString());
                Log.d("BT SERVICE", "SOCKET CREATION FAILED, STOPPING SERVICE");
                stopSelf();
            }
            mmSocket = temp;
        }

        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                handshake();
            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                    mmSocket.close();
                    stopSelf();
                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void handshake() {
            Envelope envelope = GlassUtil.newEnvelope()
                    .toBuilder()
                    .setTimezoneC2G(TimeZone.getDefault().getID())
                    .build();
            mConnectedThread.writeAsync(envelope);

            // handshaking
            GlassInfoRequest glassInfoRequest = GlassInfoRequest.newBuilder()
                    .setRequestBatteryLevel(true)
                    .setRequestStorageInfo(true)
                    .setRequestDeviceName(true)
                    .setRequestSoftwareVersion(true)
                    .build();
            Glass.Envelope envelope2 = GlassUtil.newEnvelope()
                    .toBuilder()
                    .setGlassInfoRequestC2G(glassInfoRequest)
                    .build();
            mConnectedThread.writeAsync(envelope2);

        }

        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
                sendIntent("Status", "SOCKET CLOSED");
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connected Thread
    private class ConnectedThread extends Thread {
        private final ExecutorService mWriteThread = Executors.newSingleThreadExecutor();
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            Log.d("DEBUG BT", "IN CONNECTED THREAD");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("DEBUG BT", e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                stopSelf();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d("DEBUG BT", "IN CONNECTED THREAD RUN");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true && !stopThread) {
                try {
                    Glass.Envelope envelope = (Glass.Envelope) GlassProtocol.readMessage(Glass.Envelope.newBuilder().setVersion(Integer.valueOf(0)).build(), mmInStream);
                    Log.d("GlassRevive|Get", envelope.toString());
                    sendIntent("Status", "Get Envelope");
                    //sendIntent("Log", String.valueOf(envelope));
                    if(envelope.hasCompanionInfo()){
                        Log.d("GlassRevive|Get", "Not Null CompanionInfo");
                        if(envelope.getCompanionInfo().hasRequestLocaleInfo() && envelope.getCompanionInfo().getRequestLocaleInfo()){
                            Glass.LocaleInfo LocaleInfo = Glass.LocaleInfo.newBuilder()
                                    .setNetworkBasedCountryIso("CN")
                                    .setSimBasedCountryIso("CN")
                                    .build();
                            CompanionInfo companionInfo = CompanionInfo.newBuilder()
                                    .setResponseLocaleInfo(LocaleInfo)
                                    .setId(envelope.getCompanionInfo().getId())
                                    .build();
                            Glass.Envelope env = GlassUtil.newEnvelope()
                                    .toBuilder()
                                    .setCompanionInfo(companionInfo)
                                    .build();
                            Log.d("GlassRevive|Sent", "Locale info");
                            mConnectedThread.write(env);
                        }
                    }
                    if(envelope.hasGlassInfoResponseG2C()){
                        final Glass.GlassInfoResponse response = envelope.getGlassInfoResponseG2C();
                        String info = "Device name: " + response.getDeviceName() + "\n" +
                                "Battery: " + response.getBatteryLevel() + "%" + "\n" +
                                "Software: " + response.getSoftwareVersion() + "\n" +
                                "Storage: " + response.getExternalStorageAvailableBytes()/1000/1000 + "/" + response.getExternalStorageTotalBytes()/1000/1000
                                + " MB available";
                        sendIntent("Log", info);
                    }
                    if(envelope.hasPhotoG2C()){
                        Log.d("GlassRevive|Get", "PhotoSync");
                        Glass.Photo PhotoG2C = envelope.getPhotoG2C();
                        SharedPreferences sharedPreferences = getSharedPreferences("GlassRevive", MODE_PRIVATE);
                        boolean enablePhotoSync = sharedPreferences.getBoolean("enablePhotoSync",false);
                        if(enablePhotoSync && PhotoG2C.hasThumbnailBytes()){
                            Log.d("GlassRevive|Get", "Saving");
                            File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile();
                            if (!appDir.exists()) {
                                Log.d("GlassRevive|Get", "No Dir");
                                appDir.mkdir();
                            }
                            String fileName = PhotoG2C.getPhotoId() + ".jpg";
                            File file = new File(appDir, fileName);
                            try {
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                BufferedOutputStream bStream = new BufferedOutputStream(fileOutputStream);
                                bStream.write(PhotoG2C.getThumbnailBytes().toByteArray());
                                //MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), file.getAbsolutePath(), fileName, null);
                                getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "")));
                                Log.d("GlassRevive|Get", "Done");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d("DEBUG BT", e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    stopSelf();
                    break;
                }
            }
        }

        public void writeAsync(final Envelope envelope) {
            mWriteThread.execute(new Runnable() {
                @Override
                public void run() {
                    write(envelope);
                    sendIntent("Status", "Sent Envelope");
                }
            });
        }

        public void write(Glass.Envelope envelope) {
            try {
                if (mmOutStream != null) {
                    GlassProtocol.writeMessage(envelope, mmOutStream);
                    sendIntent("Status", "Sent Envelope");
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.d("DEBUG BT", "UNABLE TO READ/WRITE " + e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void closeStreams() {
            try {
                sendIntent("Status", "Stream Closed");
                //Don't leave Bluetooth sockets open when leaving activity
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }
}