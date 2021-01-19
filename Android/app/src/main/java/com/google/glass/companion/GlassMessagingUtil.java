package com.google.glass.companion;

import com.google.googlex.glass.common.proto.TimelineNano;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem.SourceType;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;
import com.google.googlex.glass.common.proto.TimelineNano.NotificationConfig;
import com.google.googlex.glass.common.proto.TimelineNano.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class GlassMessagingUtil {

    private static float normalize(float f) {
        if (f < 0.0F) {
            return 0.001F;
        } else if (f > 100.0F) {
            return 99.999F;
        } else {
            return f;
        }
    }

    public static Glass.Envelope createTimelineMessage(String text, String rawText, String id) {
        long now = System.currentTimeMillis();

        NotificationConfig notification = NotificationConfig
        .newBuilder()
        .setDeliveryTime(now)
        .setLevel(10)
        .build();

        MenuItem menuItem = MenuItem.newBuilder()
                .setAction(MenuItem.Action.READ_ALOUD)
                .setId("233333")
                .build();

        TimelineItem timelineItem = TimelineItem.newBuilder()
                .setId(id)
                .setCreationTime(now)
                .setModifiedTime(now)
                .setHtml(text)
                .setTitle("By Shirosaki")
                .setSourceType(SourceType.GLASSWARE)
                .setSpeakableText(rawText)
                .setSpeakableType("Text message")
                .setSource("Shirosaki")
                .setIsDeleted(false)
                .setNotification(notification)
                .addMenuItem(menuItem)
                .build();


        Glass.Envelope envelope = CompanionMessagingUtil.newEnvelope().toBuilder()
                .addTimelineItem(timelineItem)
                .build();

        return envelope;
    }
}