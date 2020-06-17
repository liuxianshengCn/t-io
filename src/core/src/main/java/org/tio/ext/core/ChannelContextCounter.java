package org.tio.ext.core;

import java.util.concurrent.atomic.AtomicInteger;

public class ChannelContextCounter {

    private static AtomicInteger version = new AtomicInteger(0);

    private static AtomicInteger count = new AtomicInteger(0);

    private static Integer maxValue;

    private static int increment;

    public static void reset() {
        count.set(0);
        version.set(0);
        maxValue = 0;
    }

    public static int get() {
        return increment = count.getAndIncrement();
    }

    public static void set(int maxValue){
        count.set(0);
        version.set(0);
        ChannelContextCounter.maxValue = maxValue;
    }

    public static boolean isOver() {
        return maxValue <= increment;
    }

    public static int getVersion() {
        return version.getAndIncrement();
    }
}
