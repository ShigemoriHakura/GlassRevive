package com.hakura.GlassRevive;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.Glass;
import com.google.glass.companion.GlassConnection;
import com.google.glass.companion.GlassConnection.GlassConnectionListener;
import com.google.glass.companion.GlassMessagingUtil;
import com.google.glass.companion.Glass.Envelope;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MyGlassUtils {
    private static GlassConnection c = null;

    public static void sendInfoRequest() throws Exception{
        Glass.GlassInfoRequest glassInfoRequest = Glass.GlassInfoRequest
                .newBuilder()
                .setRequestBatteryLevel(true)
                .setRequestStorageInfo(true)
                .setRequestDeviceName(true)
                .setRequestSoftwareVersion(true)
                .setRequestNeedSetup(false)
                .build();
        Glass.Envelope envelope2 = CompanionMessagingUtil.newEnvelope()
                .toBuilder()
                .setGlassInfoRequestC2G(glassInfoRequest)
                .build();
        send(envelope2);
    }

    public static void sendText(String subject, String text, String Bid, String id, int expirationTime) throws Exception{
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String strDate= dateFormat.format(date);
        //String.valueOf(Math.random()*10000.0)
        Glass.Envelope envelope =
                GlassMessagingUtil.createTimelineMessage(
                        "<article> <section><p>" + subject + "</p> <p class=\"text-auto-size\">" + text + "</p> </section><footer>" + strDate + "</footer></article>",
                        text,
                        Bid,
                        id,
                        expirationTime);
        send(envelope);
    }

    public static void testText() throws Exception{
        String id = "";
        Glass.Envelope envelope = GlassMessagingUtil.deleteTimelineMessage(id);
        send(envelope);
    }

    public static void send(Glass.Envelope envelope) throws Exception{
        Log.d("GR","Sending Envelope");

        if (c != null){
            c.close();
        }

        Log.d("GR","Finding Glass");
        MainActivity.context.runOnUiThread(new Runnable(){
            public void run(){
                TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status);
                tv.setText("Finding Glass");
                tv.invalidate();
            }
        });

        BluetoothDevice device = null;

        Set<BluetoothDevice> devices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        for (final BluetoothDevice d : devices){
            if (d.getName().contains("Glass")){

                Log.d("GR","Talking to " + d.getName());

                device = d;

                MainActivity.context.runOnUiThread(new Runnable(){
                    public void run(){
                        TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status);
                        tv.setText("Found " + d.getName());
                        tv.invalidate();
                    }
                });

            }
        }




        c = new GlassConnection();

        c.connect(device);


        c.registerListener(new GlassConnectionListener() {

            @Override
            public void onServiceSearchError() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onServiceNotFound() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onReceivedEnvelope(Envelope envelope) {

                Log.d("GR", "=============onReceivedEnvelope");
                Log.d("GR", envelope.toString());

                if (envelope.getGlassInfoRequestC2G() != null) {
                    final Glass.GlassInfoResponse response = envelope.getGlassInfoResponseG2C();

                    MainActivity.context.runOnUiThread(new Runnable(){
                        public void run(){
                            String info = "";
                            info += "Device name: " + response.getDeviceName() + "\n";
                            info += "Battery: " + response.getBatteryLevel() + "%" + "\n";
                            info += "Software: " + response.getSoftwareVersion() + "\n";
                            info += "Storage: " + response.getExternalStorageAvailableBytes()/1000/1000 + "/" + response.getExternalStorageTotalBytes()/1000/1000
                                    + " MB available";
                            TextView tv = (TextView) MainActivity.context.findViewById(R.id.mainGlassText);
                            tv.setText(info);
                            tv.invalidate();
                        }
                    });
                }
                if (envelope.getCompanionInfo() != null) {
                    Glass.CompanionInfo companionInfo = envelope.getCompanionInfo();
                    String log = companionInfo.getResponseLog();
                    System.out.println(log);
                }
                if(envelope.getApiResponseG2C() != null){
                    List TimelineItemList = envelope.getTimelineItemList();
                    System.out.println(TimelineItemList.toString());
                }
                Log.d("GR-T", envelope.toString());
            }

            @Override
            public void onDeviceScanCompleted() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDeviceDiscovered(Device device) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onConnectionOpened() {
                // TODO Auto-generated method stub

            }
        });


        MainActivity.context.runOnUiThread(new Runnable(){
            public void run(){
                TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status);
                tv.setText("Writing Envlope");
                tv.invalidate();
            }
        });

        c.write(envelope);

        c.close();

        TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status);
        tv.setText("Done");
        tv.invalidate();

        Log.d("GR","Wrote Message");
    }

}
