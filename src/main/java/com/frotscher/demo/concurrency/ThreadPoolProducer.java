package com.frotscher.demo.concurrency;

import com.frotscher.demo.config.ConfigurationKeys;
import org.apache.commons.configuration2.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Creates a thread pool and serves as a CDI producer for the thread pool,
 * i.e. a reference to the thread pool can be injected using @Inject and
 * the CDI framework will always get it from this producer.
 */
@Singleton
public class ThreadPoolProducer {

	protected final Logger log = Logger.getLogger(ThreadPoolProducer.class.getName());

	@Inject
	private Configuration config;

	private volatile ExecutorService threadPool;


	// Suppresses default constructor, ensuring non-instantiability.
	private ThreadPoolProducer() {}

	@Produces
	@ApplicationScoped
	public ExecutorService getThreadPool() {

		if (threadPool==null) {

			// read number of threads from configuration
			int numberOfThreads = config.getInt(ConfigurationKeys.THREAD_POOL_SIZE);
			int queueCapacity = config.getInt(ConfigurationKeys.THREAD_POOL_QUEUE_CAPACITY);

			// start thread pool
			threadPool = new ThreadPool(numberOfThreads, queueCapacity);
			log.info("Thread pool created successfully. Number of threads: " + numberOfThreads);
		}

		return threadPool;
	}

	public void destroy(@Disposes ExecutorService threadPool) {
		log.info("Disposing Campus Thread Pool");
		shutDownGracefully();
	}


	public void shutDownGracefully() {
		try {
			log.info("Initiating shutdown of thread pool.");
			threadPool.shutdown();

			log.info("Thread pool stopped accepting new tasks. Now waiting for running threads to complete (max waiting time will be 5 minutes).");
			if (!threadPool.awaitTermination(5, TimeUnit.MINUTES)) {

				log.info("Thread pool was not yet terminated as some threads are still running. Now forcing termination by interrupting these threads.");
				threadPool.shutdownNow();

				log.info("Waiting for running threads to be terminated (max waiting time will be 5 minutes).");
				threadPool.awaitTermination(5, TimeUnit.MINUTES);
			}

		} catch (Throwable t) {
			log.warning("An exception occurred while shutting down thread pool: " + t.getMessage());

		} finally {

			if (threadPool.isTerminated()) {
				log.info("Thread pool was terminated.");
			} else {
				log.warning("Thread pool was not terminated. Some threads might still be running.");
			}
		}
	}
}
