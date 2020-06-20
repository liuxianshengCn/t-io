package org.tio.ext.core;

import org.tio.core.ChannelContext;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author liuxi
 * @date 2020/6/20 13:41
 */
public class ChannelContextPool {

    ArrayBlockingQueue<ChannelContext> queue;

    public ChannelContextPool(Set<ChannelContext> channelContexts) {
        queue = new ArrayBlockingQueue(channelContexts.size());
    }

    public ChannelContext pool() {
        return queue.poll();
    }
}
