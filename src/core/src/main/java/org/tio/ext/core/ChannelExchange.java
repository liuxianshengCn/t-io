package org.tio.ext.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuxi
 * @date 2020/6/20 17:06
 */
public class ChannelExchange<T> {

    private final Map<T, T> exchange = new ConcurrentHashMap<>();

    public void addExchange(T f, T t) {
        exchange.put(f, t);
    }

    public T getTo(T f) {
        return exchange.getOrDefault(f, f);
    }
}
