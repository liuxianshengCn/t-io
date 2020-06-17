package org.tio.ext.queue;

public interface MessageQueue<T> {
    public boolean add(T t);

    public T poll();

    public T peek();

    public void clear();

    public int size();

    public boolean isEmpty();
}
