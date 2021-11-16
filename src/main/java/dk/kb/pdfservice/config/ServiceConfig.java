package dk.kb.pdfservice.config;

import dk.kb.alma.client.AlmaRestClient;
import dk.kb.util.yaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Sample configuration class using the Singleton pattern.
 * This should work well for most projects with non-dynamic properties.
 */
public class ServiceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceConfig.class);
    /**
     * Besides parsing of YAML files using SnakeYAML, the YAML helper class provides convenience
     * methods like {@code getInteger("someKey", defaultValue)} and {@code getSubMap("config.sub1.sub2")}.
     */
    private static YAML serviceConfig;
    private static AlmaRestClient almaRestClient;
    
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
    
    public static Path getFrontPageFopFile(){
        return Path.of(getConfig().getString("pdfService.frontpageFOPfile"));
    }
    
    public static String getCopyrightFooterText(){
        return getConfig().getString("pdfService.copyrightFooterText");
    }
    
    public static Integer getCopyrightFooterFontSize(){
        return getConfig().getInteger("pdfService.copyrightFooterFontsize");
    }
    
    public static List<String> getHeaderLines(){
        return getConfig().getList("pdfService.oldHeaderStrings");
    }
    
    public static String getLogoPath() {
        return getConfig().getString("pdfService.frontpageLogo");
    }
    
    public static String getPdfSourcePath() {
        return getConfig().getString("pdfService.PDFsource");
    }
    
    
    public static Integer getYearsSincePublicationToBeOutsideCopyright(){
        return getConfig().getInteger("pdfService.yearsSincePublicationToBeOutsideCopyright");
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
    
    
    public static synchronized AlmaRestClient getAlmaRestClient(){
        if (almaRestClient == null){
            log.debug("Creating new AlmaRestClient");
            YAML config = getConfig();
            almaRestClient = new AlmaRestClient(config.getString("alma.url"),
                                                config.getString("alma.apikey"),
                                                Long.parseLong(config.getString("alma.rate_limit.min_sleep_millis")),
                                                Long.parseLong(config.getString("alma.rate_limit.sleep_variation_millis")),
                                                config.getString("alma.lang"),
                                                Integer.parseInt(config.getString("alma.connect_timeout")),
                                                Integer.parseInt(config.getString("alma.read_timeout")),
                                                Long.parseLong(config.getString("alma.cache_timeout", "0")),
                                                Integer.parseInt(config.getString("alma.max_retries", "3")));
        }
        return almaRestClient;
    }
    
}
