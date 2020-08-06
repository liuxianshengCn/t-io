package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.core.WriteCompletionHandler;
import org.tio.core.intf.Packet;
import org.tio.ext.TioMessage;
import org.tio.ext.model.MPartition;
import org.tio.ext.queue.RepeatMessagePool;
import org.tio.ext.queue.UniqeMessagePool;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SendRunnable implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(SendRunnable.class);

    Queue<ChannelContext> queue;

    private Map msgMap;

    private TioConfig tioConfig;

    public SendRunnable(TioConfig tioConfig, Map msgMap, Queue<ChannelContext> queue){
        this.tioConfig = tioConfig;
        this.queue = queue;
        this.msgMap = msgMap;
    }

    @Override
    public void run() {

        while (true) {
            ChannelContext context = queue.poll();

            if (null == context) {
                break;
            }

            if (msgMap.containsKey("repeat")) {
                RepeatMessagePool repeatMessagePool = (RepeatMessagePool) msgMap.get("repeat");
                if (null != repeatMessagePool) {
                    sendToClient(repeatMessagePool, context);
                    sendToUser(repeatMessagePool, context);
                    sendGroup(repeatMessagePool, context);
                }
            }

            if (msgMap.containsKey("unique")) {
                UniqeMessagePool uniqeMessagePool = (UniqeMessagePool) msgMap.get("unique");
                if (null != uniqeMessagePool) {
                    sendGlobal(uniqeMessagePool, context);
                    sendGroup(uniqeMessagePool, context);
                }
            }
        }
    }

    private void sendGlobal(UniqeMessagePool pool, ChannelContext channelContext) {
        Map<String, Packet> pGroup = pool.get(MPartition.ALL_CONNECT);
        if (null != pGroup && !pGroup.isEmpty()) {
            pGroup.forEach((k, v) -> {
                send(channelContext, v);
            });
        }
    }

    private void sendGroup(UniqeMessagePool pool, ChannelContext channelContext) {
        Map<String, Packet> pGroup = pool.get(MPartition.GROUP_CONNECT);
        if (null != pGroup && !pGroup.isEmpty()) {
            pGroup.forEach((k, v) -> {
                //是否存在交换地址
                k = TioMessage.getTo(k);

                boolean isInGroup = Tio.isInGroup(k, channelContext);

                if (isInGroup) {
                    send(channelContext, v);
                }
            });
        }
    }

    private void sendGroup(RepeatMessagePool pool, ChannelContext channelContext) {
        Map<String, List<Packet>> pGroup = pool.get(MPartition.GROUP_CONNECT);
        if (null != pGroup && !pGroup.isEmpty()) {

            pGroup.forEach((k, v) -> {
                //是否存在交换地址
                k = TioMessage.getTo(k);

                boolean isInGroup = Tio.isInGroup(k, channelContext);

                if (isInGroup) {
                    v.forEach(t ->{
                        send(channelContext, t);
                    });
                }
            });
        }
    }

    private void sendToClient(RepeatMessagePool pool, ChannelContext channelContext) {
        List<Packet> packets = pool.get(MPartition.ID_CONNECT, channelContext.getId());
        if (null != packets && packets.size() > 0) {
            for (Packet packet : packets) {
                send(channelContext, packet);
            }
        }
    }

    private void sendToUser(RepeatMessagePool pool, ChannelContext channelContext) {

        List<Packet> packets = pool.get(MPartition.BSID_CONNECT, channelContext.getBsId());
        if (null != packets && packets.size() > 0) {
            for (Packet packet : packets) {
                log.info("发送用户消息：bsId = {}", channelContext.getBsId());
                send(channelContext, packet);
            }
        }
    }

    private void send(ChannelContext channelContext, Packet packet) {
        ByteBuffer byteBuffer = packet.getPreEncodedByteBuffer();
        if (null == byteBuffer) {
            byteBuffer = tioConfig.getAioHandler().encode(packet, tioConfig, channelContext);
        }

        if (!byteBuffer.hasRemaining()) {
            byteBuffer.flip();
        }

        ReentrantLock lock = channelContext.writeCompletionHandler.lock;
        lock.lock();
        try {
            WriteCompletionHandler.WriteCompletionVo writeCompletionVo = new WriteCompletionHandler.WriteCompletionVo(byteBuffer, packet);
            channelContext.asynchronousSocketChannel.write(byteBuffer, 100, TimeUnit.MILLISECONDS, writeCompletionVo, channelContext.writeCompletionHandler);
            channelContext.writeCompletionHandler.condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
