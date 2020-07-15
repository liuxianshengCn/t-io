package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.ext.TioMessage;
import org.tio.ext.core.MessageExecutor;
import org.tio.utils.Threads;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class AssignRunnable implements Runnable {

    static Logger log							= LoggerFactory.getLogger(AssignRunnable.class);

    private TioConfig tioConfig;

    public AssignRunnable(TioConfig tioConfig){
        this.tioConfig = tioConfig;
    }

    @Override
    public void run() {
        while (true){
            try {
                Map message = TioMessage.pollAll();
                if (null != message) {
                    SetWithLock<ChannelContext> setWithLock = tioConfig.connections;
                    if(setWithLock != null){
                        Set<ChannelContext> connections = setWithLock.getObj();
                        if(connections != null && connections.size() > 0){
                            Queue<ChannelContext> queue = new ArrayBlockingQueue(connections.size());
                            queue.addAll(connections);
                            MessageExecutor.execute(tioConfig, message, queue);
                        }
                    }
                }
                this.waitTask();
            } catch (Exception e) {
                log.error("AssignRunnable Thread Exception: {}", e);
            }
        }
    }

    private void waitTask(){

        SynThreadPoolExecutor tioExecutor = Threads.getTioExecutor();

        try {
            synchronized (TioMessage.messagePushLock){
                TioMessage.messagePushLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (TioMessage.threadPoolLock) {
            try {
                if (tioExecutor.getActiveCount() > 0) {
                    TioMessage.threadPoolLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
