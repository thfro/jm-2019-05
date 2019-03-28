package com.frotscher.demo.config;

public class ConfigurationKeys {

    // Suppresses default constructor, ensuring non-instantiability.
    private ConfigurationKeys() {
    }

    public static final String THREAD_POOL_SIZE = "threadPool.size";
    public static final String THREAD_POOL_QUEUE_CAPACITY = "threadPool.queueCapacity";

    public static final String GOOGLE_APIKEY = "integration.google.apikey";
}
