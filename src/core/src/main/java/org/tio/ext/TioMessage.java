package org.tio.ext;


import org.tio.ext.model.MsgKey;
import org.tio.ext.model.MsgType;
import org.tio.ext.queue.TioMessageQueue;

import java.util.HashMap;

public class TioMessage {

    private static TioMessageQueue<HashMap<MsgKey, String>> MESSAGE_QUEUE = new TioMessageQueue<>();

    public static Object threadPoolLock = new Object();
    public static Object messagePushLock = new Object();

    public static void push(MsgType type, String channel, String body){
        HashMap<MsgKey, String> msgMap = MESSAGE_QUEUE.peek();
        if(msgMap == null){
            synchronized (MESSAGE_QUEUE){
                if(msgMap == null){
                    msgMap = new HashMap<>();
                    MESSAGE_QUEUE.add(msgMap);
                }
            }
        }
        MsgKey msgKey = new MsgKey();
        msgKey.setType(type);
        msgKey.setChannel(channel);
        //log.info("接收到推送消息: {}, {}", channel, body);
        msgMap.put(msgKey, body);
    }

    public static void pushAll(String channcel, String body){
        push(MsgType.GLOBAL, channcel, body);
    }

    public static void pushUser(String channcel, String body){
        push(MsgType.USER, channcel, body);
    }

    public static void pushGroup(String channcel, String body){
        push(MsgType.GROUP, channcel, body);
    }

    public static void pushDirect(String channelId, String body){
        push(MsgType.DIRECT, channelId, body);
    }

    public static boolean hasMsg(){
        return MESSAGE_QUEUE.size() > 0;
    }

    public static TioMessageQueue<HashMap<MsgKey, String>> getQueue(){
        return MESSAGE_QUEUE;
    }

    public static HashMap<MsgKey, String> poll(){
        return MESSAGE_QUEUE.poll();
    }
}
