package dk.kb.pdfservice.config;

import dk.kb.util.yaml.YAML;

import java.io.IOException;
import java.util.List;

/**
 * Sample configuration class using the Singleton pattern.
 * This should work well for most projects with non-dynamic properties.
 */
public class ServiceConfig {
    
    /**
     * Besides parsing of YAML files using SnakeYAML, the YAML helper class provides convenience
     * methods like {@code getInteger("someKey", defaultValue)} and {@code getSubMap("config.sub1.sub2")}.
     */
    private static YAML serviceConfig;
    
    /**
     * Initialized the configuration from the provided configFile.
     * This should normally be called from {@link dk.kb.pdfservice.webservice.ContextListener} as
     * part of web server initialization of the container.
     *
     * @param configFile the configuration to load.
     * @throws IOException if the configuration could not be loaded or parsed.
     */
    public static synchronized void initialize(String configFile) throws IOException {
        serviceConfig = YAML.resolveLayeredConfigs(configFile);
    }
  
    public static String getBasepath() {
        return serviceConfig.getString("config.basePath");
    }
    
    public static String getOutputDir() {
        return serviceConfig.getString("config.outputDir");
    }
    
    public static String getResourcesDir() {
        return serviceConfig.getString("config.resourcesDir");
    }
    
    /**
     * Direct access to the backing YAML-class is used for configurations with more flexible content
     * and/or if the service developer prefers key-based property access.
     *
     * @return the backing YAML-handler for the configuration.
     */
    public static YAML getConfig() {
        if (serviceConfig == null) {
            throw new IllegalStateException("The configuration should have been loaded, but was not");
        }
        return serviceConfig;
    }
    
}
