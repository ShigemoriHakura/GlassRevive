package com.google.glass.companion;

import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem.SourceType;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;
import com.google.googlex.glass.common.proto.TimelineNano.NotificationConfig;
import com.google.googlex.glass.common.proto.TimelineNano.MenuItem;

import java.util.UUID;

public class GlassMessagingUtil {

    public static Glass.Envelope createTimelineMessage(String text, String rawText, String Bid, String id, int expirationTime) {
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
                .addMenuItem(menuItemVideo)
                .addMenuItem(menuItemPin)
                .addMenuItem(menuItemDelete)
                .build();


        Glass.Envelope envelope = CompanionMessagingUtil.newEnvelope().toBuilder()
                .addTimelineItem(timelineItem)
                .build();

        return envelope;
    }

    public static Glass.Envelope deleteTimelineMessage(String id) {

        TimelineItem timelineItem = TimelineItem.newBuilder()
                .setId(id)
                .build();

        Glass.ApiRequest request = Glass.ApiRequest.newBuilder()
                .setProjectId("com.hakura.GlassRevive")
                .setToken("123456")
                .setType(Glass.ApiRequest.RequestType.DELETE_TIMELINE_ITEM)
                .build();

        Glass.Envelope envelope = CompanionMessagingUtil.newEnvelope().toBuilder()
                .setApiRequestC2G(request)
                .addTimelineItem(timelineItem)
                .build();

        return envelope;
    }

}