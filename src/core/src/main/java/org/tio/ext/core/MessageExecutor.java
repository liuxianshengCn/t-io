package org.tio.ext.core;

import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.ext.task.SendRunnable;
import org.tio.utils.Threads;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

import java.util.Map;
import java.util.Queue;

public class MessageExecutor {

	public static void execute(TioConfig tioConfig, Map message, Queue<ChannelContext> queue) {

		SynThreadPoolExecutor tioExecutor = Threads.getTioExecutor();

		if (null != tioExecutor) {
			int size = queue.size();
			if (size > tioExecutor.getCorePoolSize()) {
				size = tioExecutor.getCorePoolSize();
			}

			for (int i = 0; i < size; i++) {
				tioExecutor.execute(new SendRunnable(tioConfig, message, queue));
			}
		}
	}

}
