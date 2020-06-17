package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.ext.core.ChannelContextCounter;
import org.tio.ext.core.WsSendUtils;
import org.tio.ext.model.MsgKey;
import org.tio.ext.model.MsgType;
import org.tio.utils.lock.SetWithLock;

import java.util.HashMap;
import java.util.Iterator;

public class SendRunnable implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(SendRunnable.class);

    private ChannelContext[] contexts;

    private HashMap<MsgKey, String> msgMap;

    private TioConfig tioConfig;

    public SendRunnable(TioConfig tioConfig, ChannelContext[] contexts, HashMap<MsgKey, String> msgMap){
        this.tioConfig = tioConfig;
        this.contexts = contexts;
        this.msgMap = msgMap;
    }

    @Override
    public void run() {
        if(contexts == null || contexts.length <= 0){
            return;
        }
        if(msgMap == null || msgMap.size() <= 0){
            return;
        }
        while (true) {
            int index = ChannelContextCounter.get();
            if (ChannelContextCounter.isOver()) {
                break;
            }
            ChannelContext context = contexts[index];
            //log.info(JSONUtil.objectToJsonString(msgMap) + "," + contexts.length +"发送消息成功=" + System.currentTimeMillis()+"，执行任务编号=" + index + ", ThreadName=" + Thread.currentThread().getName());
            msgMap.forEach((k, v) ->{
                if(k.getType().equals(MsgType.GLOBAL)){
                    //如果是全局发送则直接发
                    WsSendUtils.send(context, v);
                } else if(k.getType().equals(MsgType.GROUP)){
                    //如果是组发送则需要判断当前ChannelContext是否存在组里面
                    String channel = k.getChannel();
                    boolean inGroup = Tio.isInGroup(channel, context);
                    if(inGroup){
                        WsSendUtils.send(context, v);
                    }
                } else if(k.getType().equals(MsgType.USER)){
                    //如果是用户定向发送,则需要通过用户ID找出对应用户ChanncelContext,然后判断是不是当前ChannelContext,如果是则发送
                    SetWithLock<ChannelContext> setWithLock = Tio.getByUserid(tioConfig, k.getChannel());
                    if(setWithLock != null && setWithLock.getObj() != null && setWithLock.size() > 0){
                        Iterator<ChannelContext> iterator = setWithLock.getObj().iterator();
                        while (iterator.hasNext()){
                            if(context.getId().equals(k.getChannel())){
                                WsSendUtils.send(context, v);
                            }
                        }
                    }
                } else if(k.getType().equals(MsgType.DIRECT)){
                    //如果是ChannelContext定向发送,则只需要判断是否与当前ChannelContext是否相等
                    if(context.getId().equals(k.getChannel())){
                        WsSendUtils.send(context, v);
                    }
                }else {
                    log.error("无法找到消息类型,不发送");
                }

            });

        }

        log.info("线程执行结束=" + Thread.currentThread().getName());

    }
}
