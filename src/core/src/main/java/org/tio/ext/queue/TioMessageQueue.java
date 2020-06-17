package org.tio.ext.queue;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TioMessageQueue<T> implements MessageQueue<T>{

    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

    public TioMessageQueue() {
    }

    @Override
    public boolean add(T e) {
        return queue.add(e);
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public T peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
