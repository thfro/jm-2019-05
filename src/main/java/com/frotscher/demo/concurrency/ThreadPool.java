package com.frotscher.demo.concurrency;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class implements a thread pool with a fixed number of threads
 * and a bounded queue for tasks that could not be executed immediately
 * due to thread pool exhaustion.
 */
public class ThreadPool extends ThreadPoolExecutor {

	protected final Logger log = Logger.getLogger(ThreadPool.class.getName());

	public ThreadPool(int numberOfThreads, int queueCapacity) {
		super(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity));
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		log.info("Started task  (active: " + getActiveCount() + ", queued: " + getQueue().size() + ")");
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		log.info("Finished task (active: " + (getActiveCount() - 1) + ", queued: " + getQueue().size() + ")");
	}

	@Override
	public void execute(Runnable command) {
		super.execute(command);
		log.info("Accepted new task (active: " + getActiveCount() + ", queued: " + getQueue().size() + ")");
	}
}
