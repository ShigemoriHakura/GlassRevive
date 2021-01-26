package com.hakura.GlassRevive.Glass;

import android.util.Log;

import com.google.glass.companion.Glass;
import com.google.glass.companion.Glass.Envelope;
import com.google.glass.companion.Glass.CompanionInfo;
import com.google.glass.companion.Glass.CompanionFeatureInfo;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;
import com.google.googlex.glass.common.proto.TimelineNano.MenuItem;
import com.google.googlex.glass.common.proto.TimelineNano.NotificationConfig;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem.SourceType;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class GlassUtil {

    private static final int PROTOCOL_VERSION = calculateVersion(6, 11);

    public static int calculateVersion(int paramInt1, int paramInt2) {
        return (paramInt2 << 16) + paramInt1;
    }

    public static Envelope newEnvelope() {
        CompanionFeatureInfo companionFeatureInfo = CompanionFeatureInfo.newBuilder()
                .setIsPhotoSyncEnabled(true)
                .setIsKeyboardTextEntrySupported(false)
                .build();

        CompanionInfo companionInfo = CompanionInfo.newBuilder()
                .setRequestLog(true)
                .setRequestNetwork(true)
                .setId(0)
                .build();

        return Envelope.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setUptimeMillis(System.currentTimeMillis())
                //.setCompanionInfo(companionInfo)
                //.setCompanionFeatureInfo(companionFeatureInfo)
                .build();
    }

    public static Envelope createTimelineMessage(String text, String rawText, String Bid, String id, int expirationTime, boolean enableTTS) {
        long now = System.currentTimeMillis();

        NotificationConfig notification = NotificationConfig
                .newBuilder()
                .setDeliveryTime(now)
                .setLevel(10)
                .build();

        MenuItem menuItemRead = MenuItem.newBuilder()
                .setAction(MenuItem.Action.READ_ALOUD)
                .setId(UUID.randomUUID().toString())
                .build();

        MenuItem menuItemVideo = MenuItem.newBuilder()
                .setAction(MenuItem.Action.PLAY_VIDEO)
                .setId(UUID.randomUUID().toString())
                .setPayload("https://tts.baidu.com/text2audio?lan=ZH&cuid=baike&pdt=301&ctp=1&spd=5&per=0&vol=15&&pit=6&tex=" + rawText)
                .build();

        MenuItem menuItemPin = MenuItem.newBuilder()
                .setAction(MenuItem.Action.TOGGLE_PINNED)
                .setId(UUID.randomUUID().toString())
                .build();

        MenuItem menuItemDelete = MenuItem.newBuilder()
                .setAction(MenuItem.Action.DELETE)
                .setId(UUID.randomUUID().toString())
                .build();


        TimelineItem timelineItem = TimelineItem.newBuilder()
                .setId(id)
                .setBundleId(Bid)
                .setCreationTime(now)
                .setModifiedTime(now)
                .setExpirationTime(now + expirationTime * 1000)
                .setHtml(text)
                .setTitle("By Shirosaki")
                .setSourceType(SourceType.GLASSWARE)
                .setSpeakableText(rawText)
                .setSpeakableType("Text message")
                .setSource("Shirosaki")
                .setIsDeleted(false)
                .setNotification(notification)
                .setCompanionSyncStatus(TimelineItem.SyncStatus.SYNCED)
                .addMenuItem(menuItemRead)
                .addMenuItem(menuItemPin)
                .addMenuItem(menuItemDelete)
                .build();

        if(enableTTS){
            Log.d("GlassRevive", "Added TTS Link");
            timelineItem = timelineItem.toBuilder().addMenuItem(menuItemVideo).build();
        }

        Envelope envelope = GlassUtil.newEnvelope().toBuilder()
                .addTimelineItem(timelineItem)
                .build();
        return envelope;
    }

    public static Envelope returnText(String subject, String text, String Bid, String id, int expirationTime, boolean enableTTS) throws Exception{
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String strDate= dateFormat.format(date);
        Envelope envelope =
                GlassUtil.createTimelineMessage(
                        "<article> <section><p>" + subject + "</p> <p class=\"text-auto-size\">" + text + "</p> </section><footer>" + strDate + "</footer></article>",
                        text,
                        Bid,
                        id,
                        expirationTime,
                        enableTTS);
        return envelope;
    }
}