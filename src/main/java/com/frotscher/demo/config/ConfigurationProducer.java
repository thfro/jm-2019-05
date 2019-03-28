package com.frotscher.demo.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.File;

public class ConfigurationProducer {

    private static Configuration configuration;

    // Suppresses default constructor, ensuring non-instantiability.
    private ConfigurationProducer() {
    }


    @Produces
    @ApplicationScoped
    public static Configuration getConfiguration() {

        if (configuration == null) {
            configuration = readConfigurationFromFile("config.properties");
        }
        return configuration;
    }


    protected static Configuration readConfigurationFromFile(String fileName) {
        Configurations configs = new Configurations();
        File propertiesFile = new File(fileName);

        try {
            PropertiesConfiguration configuration = configs.properties(propertiesFile);
            return configuration;

        } catch (ConfigurationException e) {
            throw new RuntimeException("Unable to load configuration.", e);
        }
    }
}