package org.tio.ext.queue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可重复消息池
 *
 * @author liuxi
 * @date 2020/6/19 11:57
 */
public class RepeatMessagePool<T, P> implements Serializable {
    private final Map<T, Map<T, List<P>>> pool = new ConcurrentHashMap<>();

    /**
     * 将消息根据通道放入消息池
     *
     * @param key   消息类型，全局消息，组消息，user消息，clientId消息
     * @param packet
     */
    public void addMessage(T key, T uuid, P packet) {

        Map<T, List<P>> pMap = pool.get(key);
        List<P> pArray;
        if (null == pMap) {
            synchronized (pool) {
                if (null == pMap) {
                    pMap = new ConcurrentHashMap<>();
                    pArray = new ArrayList<>();
                    pMap.put(uuid, pArray);
                    pool.put(key, pMap);
                }
            }
        }
        pArray = pMap.get(uuid);
        if (null == pArray) {
            synchronized (pool) {
                if (!pMap.containsKey(uuid)) {
                    pArray = new ArrayList<>();
                    pMap.put(uuid, pArray);
                }
            }
        }
        pArray.add(packet);
    }

    /**
     * 根据类型获取消息
     * @param key
     * @return
     */
    public Map<T, List<P>> get(T key) {
        return pool.get(key);
    }

    /**
     * 获取消息列表
     *
     * @param key
     * @return
     */
    public List<P> get(T key, T uuid) {
        Map<T, List<P>> pMap = pool.get(key);
        if (null != pMap) {
            List<P> pArray = pMap.get(uuid);
            return pArray;
        }
        return null;
    }

    /**
     * 获取所有消息
     * @param key
     * @return
     */
    public Map<T, List<P>> getAll(T key) {
        return pool.get(key);
    }

    /**
     * 获取一条消息
     *
     * @param key
     * @param index
     * @return
     */
    public P get(T key, T uuid, int index) {
        List<P> pArray = this.get(key, uuid);
        if (null != pArray) {
            return pArray.get(index);
        }
        return null;
    }
}
