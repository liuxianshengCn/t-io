package org.tio.ext.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.core.WriteCompletionHandler;
import org.tio.core.intf.Packet;
import org.tio.core.ssl.SslUtils;
import org.tio.core.ssl.SslVo;
import org.tio.core.utils.TioUtils;
import org.tio.ext.model.MsgResponse;

import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public class WsSendUtils {
    private static final Logger log = LoggerFactory.getLogger(WsSendUtils.class);
    public static void send(ChannelContext channelContext, String message) {
        byte[] mes = GzipUtils.compress(message);
        sendPacket(channelContext, MsgResponse.fromBytes(mes));
    }

    public static boolean sendPacket(ChannelContext channelContext, Packet packet) {
        TioConfig tioConfig = channelContext.tioConfig;
        boolean isSsl = SslUtils.isSsl(tioConfig);
        ByteBuffer byteBuffer = getByteBuffer(packet, channelContext);
        if (isSsl) {
            if (!packet.isSslEncrypted()) {
                SslVo sslVo = new SslVo(byteBuffer, packet);
                try {
                    channelContext.sslFacadeContext.getSslFacade().encrypt(sslVo);
                    byteBuffer = sslVo.getByteBuffer();
                } catch (SSLException e) {
                    log.error(channelContext.toString() + ", 进行SSL加密时发生了异常", e);
                    Tio.close(channelContext, "进行SSL加密时发生了异常", ChannelContext.CloseCode.SSL_ENCRYPTION_ERROR);
                    return false;
                }
            }
        }

        sendByteBuffer(byteBuffer, packet, channelContext);
        return true;
    }

    private static ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        MsgResponse wsResponse = (MsgResponse)packet;
        ByteBuffer byteBuffer = MsgServerEncoder.encode(wsResponse, tioConfig, channelContext);
        return byteBuffer;
    }

    private static ByteBuffer getByteBuffer(Packet packet, ChannelContext channelContext) {
        try {
            ByteBuffer byteBuffer = packet.getPreEncodedByteBuffer();
            if (byteBuffer == null) {
                TioConfig tioConfig = channelContext.tioConfig;
                byteBuffer = encode(packet, tioConfig, channelContext);
            }

            if (!byteBuffer.hasRemaining()) {
                byteBuffer.flip();
            }
            return byteBuffer;
        } catch (Exception e) {
            log.error(packet.logstr(), e);
            throw new RuntimeException(e);
        }
    }

    public static void sendByteBuffer(ByteBuffer byteBuffer, Object packets, ChannelContext channelContext) {
        if (byteBuffer == null) {
            log.error("{},byteBuffer is null", channelContext);
            return;
        }

        if (!TioUtils.checkBeforeIO(channelContext)) {
            return;
        }

       /* WriteCompletionHandler.WriteCompletionVo writeCompletionVo = new WriteCompletionHandler.WriteCompletionVo(byteBuffer, packets);
        channelContext.asynchronousSocketChannel.write(byteBuffer, writeCompletionVo, channelContext.writeCompletionHandler);*/
        ReentrantLock lock = channelContext.writeCompletionHandler.lock;
        lock.lock();
        try {
            WriteCompletionHandler.WriteCompletionVo writeCompletionVo = new WriteCompletionHandler.WriteCompletionVo(byteBuffer, packets);
            channelContext.asynchronousSocketChannel.write(byteBuffer, writeCompletionVo, channelContext.writeCompletionHandler);
            channelContext.writeCompletionHandler.condition.await();
        } catch (InterruptedException e) {
            log.error(e.toString(), e);
        } finally {
            lock.unlock();
        }
    }
}
