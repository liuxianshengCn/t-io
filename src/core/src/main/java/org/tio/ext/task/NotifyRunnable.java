package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.ext.TioMessage;
import org.tio.utils.Threads;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

public class NotifyRunnable implements Runnable {
    static Logger log							= LoggerFactory.getLogger(NotifyRunnable.class);
    private SynThreadPoolExecutor tioExecutor = null;

    @Override
    public void run() {

        tioExecutor = Threads.getTioExecutor();

        while (true){

            try {
                Thread.sleep(200);

                if (tioExecutor.getActiveCount() <= 0) {
                    synchronized (TioMessage.messagePushLock){
                        TioMessage.messagePushLock.notifyAll();
                    }
                    synchronized (TioMessage.threadPoolLock) {
                        TioMessage.threadPoolLock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                log.error("AssignRunnable Thread InterruptedException: {}", e);
            } catch (Exception e) {
                log.error("AssignRunnable Thread Exception: {}", e);
            }

        }
    }

}
