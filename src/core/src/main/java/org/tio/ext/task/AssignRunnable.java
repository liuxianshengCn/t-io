package org.tio.ext.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.ext.TioMessage;
import org.tio.ext.core.ChannelContextCounter;
import org.tio.ext.core.MessageExecutor;
import org.tio.ext.model.MsgKey;
import org.tio.utils.lock.SetWithLock;

import java.util.HashMap;
import java.util.Set;

public class AssignRunnable implements Runnable {

    static Logger log							= LoggerFactory.getLogger(AssignRunnable.class);

    private TioConfig tioConfig;

    public AssignRunnable(TioConfig tioConfig){
        this.tioConfig = tioConfig;
    }

    @Override
    public void run() {
        while (true){
            if(TioMessage.hasMsg()){
                HashMap<MsgKey, String> msg = TioMessage.poll();
                SetWithLock<ChannelContext> setWithLock = tioConfig.connections;
                if(setWithLock != null){
                    Set<ChannelContext> connections = setWithLock.getObj();
                    if(connections != null && connections.size() > 0){
                        ChannelContextCounter.set(connections.size());
                        ChannelContext[] contexts = connections.toArray(new ChannelContext[connections.size()]);
                        MessageExecutor.execute(tioConfig, contexts, msg);
                    }
                }
            }
            this.waitTask();

        }
    }

    private void waitTask(){
        try {
            synchronized (TioMessage.messagePushLock){
                log.info("暂无新消息，线程进行等待，counter版本号="+ ChannelContextCounter.getVersion());
                TioMessage.messagePushLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("收到新消息，消息线程被唤醒，counter版本号="+ChannelContextCounter.getVersion());

        synchronized (TioMessage.threadPoolLock) {
            try {
                log.info("推送线程池正在执行任务，等待推送任务結束，counter版本号=" + ChannelContextCounter.getVersion());
                if (TioConfig.PUSH_EXECUTOR.getActiveCount() > 0) {
                    TioMessage.threadPoolLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("推送线程执行完成，唤醒推送线程，counter版本号=" + ChannelContextCounter.getVersion());
    }


}
