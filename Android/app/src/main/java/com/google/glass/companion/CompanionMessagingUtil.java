package com.google.glass.companion;

import java.lang.reflect.Method;

public class CompanionMessagingUtil {

    private static final int PROTOCOL_VERSION = 131078;

    private static final Method androidUptimeMills;
    static {
        Method method = null;
        try {
            method = Class.forName("android.os.SystemClock").getMethod("uptimeMillis");
        } catch (Exception e) {
        }
        androidUptimeMills = method;
    }

    public static Glass.Envelope newEnvelope() {
        long uptimeMillis;
        if (androidUptimeMills != null) {
            try {
                uptimeMillis = (Long) androidUptimeMills.invoke(null, null);
            } catch (Exception e) {
                uptimeMillis = Long.valueOf(System.currentTimeMillis());
            }
        } else {
            uptimeMillis = Long.valueOf(System.currentTimeMillis());
        }
        Glass.Envelope localEnvelope =  Glass.Envelope.newBuilder()
                .setVersion(Integer.valueOf(PROTOCOL_VERSION))
                .setUptimeMillis(uptimeMillis)
                .build();
        return localEnvelope;
    }
}
