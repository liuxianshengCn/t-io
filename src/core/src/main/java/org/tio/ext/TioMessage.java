package org.tio.ext;


import org.tio.core.intf.Packet;
import org.tio.ext.core.ChannelExchange;
import org.tio.ext.model.MPartition;
import org.tio.ext.queue.RepeatMessagePool;
import org.tio.ext.queue.UniqeMessagePool;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TioMessage {

    //去重消息队列
    private final static Queue<UniqeMessagePool> uniqeMessagePoolQueue = new ConcurrentLinkedQueue<>();
    //可重复消息队列
    private final static Queue<RepeatMessagePool> repeatMessagePoolQueue = new ConcurrentLinkedQueue<>();

    public static Object threadPoolLock = new Object();
    public static Object messagePushLock = new Object();

    private final static ChannelExchange<String> exchange = new ChannelExchange();

    /**
     * 绑定交换区
     * @param from
     * @param to
     */
    public static void addExchange(String from, String to) {
        exchange.addExchange(from, to);
    }

    /**
     * 获取交换地址
     * @param from
     * @return
     */
    public static String getTo(String from) {
        return exchange.getTo(from);
    }

    /**
     * 推送全局消息，去重
     * @param packet
     */
    public static void pushToAllUniqe(String channel, Packet packet) {
        UniqeMessagePool messagePool = getUnieqPool();
        messagePool.addMessage(MPartition.ALL_CONNECT, channel, packet);
        uniqeMessagePoolQueue.add(messagePool);
    }

    /**
     * 推送组消息，去重
     * @param groupId
     * @param packet
     */
    public static void pushToGroupUniqe(String groupId, Packet packet) {
        UniqeMessagePool messagePool = getUnieqPool();
        messagePool.addMessage(MPartition.GROUP_CONNECT, groupId, packet);
        uniqeMessagePoolQueue.add(messagePool);
    }

    /**
     * 推送组消息，去重
     * @param groupId
     * @param packet
     */
    public static void pushToGroupUniqe(String groupId, String mGroupId, Packet packet) {
        UniqeMessagePool messagePool = getUnieqPool();
        messagePool.addMessage(MPartition.GROUP_CONNECT, groupId, packet);
        uniqeMessagePoolQueue.add(messagePool);
    }

    /**
     * 推送可重复消息
     * @param MPartition
     * @param userId
     * @param packet
     */
    public static void pushToRepeat(MPartition MPartition, String userId, Packet packet) {
        RepeatMessagePool messagePool = getRepeatPool();
        messagePool.addMessage(MPartition, userId, packet);
        repeatMessagePoolQueue.add(messagePool);
    }

    /**
     * 发送到指定客户端
     * @param clientId
     * @param packet
     */
    public static void pushToClient(String clientId, Packet packet) {
        RepeatMessagePool messagePool = getRepeatPool();
        messagePool.addMessage(MPartition.ID_CONNECT, clientId, packet);
        repeatMessagePoolQueue.add(messagePool);
    }

    /**
     * 获取去重复消息池
     * @return
     */
    private static UniqeMessagePool getUnieqPool() {
        UniqeMessagePool pool = uniqeMessagePoolQueue.poll();
        if (null == pool) {
            synchronized (uniqeMessagePoolQueue) {
                pool = uniqeMessagePoolQueue.poll();
                if (null == pool) {
                    pool = new UniqeMessagePool<String, Packet>();
                }
            }
        }
        return pool;
    }

    /**
     * 获取重复消息池
     * @return
     */
    private static RepeatMessagePool getRepeatPool() {
        RepeatMessagePool pool = repeatMessagePoolQueue.poll();
        if (null == pool) {
            synchronized (repeatMessagePoolQueue) {
                pool = repeatMessagePoolQueue.poll();
                if (null == pool) {
                    pool = new RepeatMessagePool<String, Packet>();
                }
            }
        }
        return pool;
    }

    public static Map<String, Object> pollAll() {
        UniqeMessagePool uniqeMessagePool = uniqeMessagePoolQueue.poll();
        RepeatMessagePool repeatMessagePool = repeatMessagePoolQueue.poll();

        Map<String, Object> data = new HashMap<>();
        if (null != data) {
            data.put("unique", uniqeMessagePool);
        }
        if (null != repeatMessagePool) {
            data.put("repeat", repeatMessagePool);
        }

        return data;
    }
}
