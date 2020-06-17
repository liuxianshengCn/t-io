package org.tio.ext.core;

import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.ext.model.MsgKey;
import org.tio.ext.task.SendRunnable;

import java.util.HashMap;

public class MessageExecutor {


	public static void execute(TioConfig tioConfig, ChannelContext[] contexts, HashMap<MsgKey, String> msgMap) {
		if(TioConfig.PUSH_EXECUTOR == null){
			throw new IllegalArgumentException("请配置推送线程池");
		}
		int size = TioConfig.PUSH_EXECUTOR.getCorePoolSize();
		if(contexts.length < TioConfig.PUSH_EXECUTOR.getCorePoolSize()){
			size = contexts.length;
		}
		for (int i = 0; i < size; i++) {
			TioConfig.PUSH_EXECUTOR.execute(new SendRunnable(tioConfig, contexts, msgMap));
		}
	}

}
