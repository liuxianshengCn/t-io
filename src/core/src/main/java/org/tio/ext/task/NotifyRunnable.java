package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.TioConfig;
import org.tio.ext.TioMessage;

public class NotifyRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NotifyRunnable.class);
    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (TioConfig.PUSH_EXECUTOR.getActiveCount() <= 0 && TioMessage.hasMsg()) {
                System.out.println("线程池所有线程执行完成，需要重新唤醒线程池");
                synchronized (TioMessage.messagePushLock){
                    TioMessage.messagePushLock.notify();
                }
                synchronized (TioMessage.threadPoolLock) {
                    TioMessage.threadPoolLock.notify();
                }
            }

        }
    }

}
