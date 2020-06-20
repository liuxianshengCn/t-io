package org.tio.ext.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 去重消息池
 *
 * @author liuxi
 * @date 2020/6/19 11:56
 */
public class UniqeMessagePool<T, P> {
    Logger log = LoggerFactory.getLogger(UniqeMessagePool.class);
    private final Map<T, Map<T, P>> pool = new ConcurrentHashMap<>();

    /**
     * 将消息放置消息池
     *
     * @param key
     * @param packet
     */
    public void addMessage(T key, P packet) {
        this.addMessage(key, key, packet);
    }

    /**
     * 将消息根据通道放入消息池
     *
     * @param key
     * @param packet
     */
    public void addMessage(T key, T channel, P packet) {
        Map<T, P> pGroup = pool.get(key);
        if (null == pGroup) {
            synchronized (pool) {
                if (null == pGroup) {
                    pGroup = new ConcurrentHashMap<>();
                    pool.put(key, pGroup);
                }
            }
        }
        pGroup.put(channel, packet);
    }

    /**
     * 根据类型获取消息
     * @param key
     * @return
     */
    public Map<T, P> get(T key) {
        return pool.get(key);
    }

    /**
     * 获取消息
     * @param key
     * @param channel
     * @return
     */
    public P get(T key, T channel) {
        if (pool.containsKey(key)) {
            Map<T, P> pGroup = pool.get(key);
            if (pGroup.containsKey(channel)) {
                return pGroup.get(channel);
            }
        }
        return null;
    }

}
