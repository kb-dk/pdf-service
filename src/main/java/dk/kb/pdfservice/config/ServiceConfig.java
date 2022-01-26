package dk.kb.pdfservice.config;

import dk.kb.alma.client.AlmaRestClient;
import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.titlepage.FontEnum;
import dk.kb.util.yaml.YAML;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;

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
    
    public static void shutdown() {
        //Anything to shut down here??
    }
    
    public static final String EMPTY = "__empty__";
    public static final String DEFAULT = "__default__";
    
    public static Map<String, ApronType> getApronTypeMapping() {
        OrderedMap<String, ApronType> documentTypeMapping = new ListOrderedMap<>();
        
        @NotNull List<YAML> entries = getConfig().getYAMLList("pdfService.apron.999aToApronType");
        for (YAML entry : entries) {
            @NotNull String key = entry.getString("999a");
            @NotNull String type = entry.getString("type");
            ApronType value = ApronType.valueOf(type);
            documentTypeMapping.put(key, value);
        }
        
        return documentTypeMapping;
    }
    
    public static List<String> getPdfSourcePath() {
        return getConfig().getList("pdfService.PDFsource");
    }
    
    public static String getPdfTempPath() {
        return getConfig().getString("pdfService.PDFTempFolder");
    }
    
    public static TemporalAmount getMaxAgeTempPdf() {
        @NotNull Integer value = getConfig().getInteger(
                "pdfService.maxAgeOfTempPdfsInMinutes.value");
        
        String unit = getConfig().getString(
                "pdfService.maxAgeOfTempPdfsInMinutes.unit");
        return Duration.of(value, ChronoUnit.valueOf(unit));
        
    }
    
    
    //OldHeaders
    public static List<String> getHeaderLines() {
        return getConfig().getList("pdfService.oldHeaderStrings");
    }
    
    public static File getOldHeaderImageDir() {
        return new File(ServiceConfig.getConfig().getString("pdfService.oldHeaderImagesDir"));
    }
    
    //Copyright determination
    public static TemporalAmount getTimeSincePublicationToBeOutsideCopyright() {
        @NotNull Integer value = getConfig().getInteger(
                "pdfService.TimeSincePublicationToBeOutsideCopyright.value");
        
        String unit = getConfig().getString(
                "pdfService.TimeSincePublicationToBeOutsideCopyright.unit");
        ChronoUnit chronoUnit = ChronoUnit.valueOf(unit);
        switch (chronoUnit) {
            case DAYS:
                return Period.of(0, 0, value);
            case WEEKS:
                return Period.of(0, 0, value * 7);
            case MONTHS:
                return Period.of(0, value, 0);
            case YEARS:
                return Period.of(value, 0, 0);
            case DECADES:
                return Period.of(value * 10, 0, 0);
            case CENTURIES:
                return Period.of(value * 100, 0, 0);
            case MILLENNIA:
                return Period.of(value * 1000, 0, 0);
            default:
                return Duration.of(value, chronoUnit);
        }
    }
    
    
    //FrontPage
    public static Path getFrontPageFopFile() {
        return Path.of(getConfig().getString("pdfService.apron.FOPfile")).toAbsolutePath();
    }
    
    //Copyright Footer
    public static List<String> getCopyrightFooterText() {
        return getConfig().getList("pdfService.copyrightFooter.Text");
    }
    
    public static Integer getCopyrightFooterFontSize() {
        return getConfig().getInteger("pdfService.copyrightFooter.Fontsize");
    }
    
    public static Color getCopyrightFooterColor() {
        return Color.decode(ServiceConfig.getConfig()
                                         .getString("pdfService.copyrightFooter.Color"));
    }
    
    public static float getCopyrightFooterTransparency() {
        return ServiceConfig.getConfig().getFloat("pdfService.copyrightFooter.Transparency");
    }
    
    public static Color getCopyrightFooterBackgroundColor() {
        return Color.decode(ServiceConfig.getConfig()
                                         .getString("pdfService.copyrightFooter.Background.Color"));
    }
    
    public static float getCopyrightFooterBackgroundTransparency() {
        return ServiceConfig.getConfig().getFloat("pdfService.copyrightFooter.Background.Transparency");
    }
    
    
    public static String getErrorMessage() {
        return ServiceConfig.getConfig().getString("pdfService.errorMessage");
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
    
    
    public static synchronized AlmaRestClient getAlmaRestClient() {
        if (almaRestClient == null) {
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
    
    public static double getMaxLinesForApron(ApronType apronType) {
        return getConfig().getDouble("pdfService.apron.metadataTable.maxlines." + apronType.name());
    }
    
    public static float getApronMetadataTableWidthCm() {
        return getConfig().getFloat("pdfService.apron.metadataTable.widthCM");
    }
    
    public static int getApronMetadataTableFontSize() {
        return getConfig().getInteger("pdfService.apron.metadataTable.fontSize");
    }
    
    public static TTFFile getApronMetadataTableFont() {
        TTFFile ttfFile = new TTFFile(false, true);
    
        File file = new File("conf/fonts/DejaVuSans.ttf").getAbsoluteFile();
        try (InputStream stream = new FileInputStream(file)) {
            FontFileReader reader = new FontFileReader(stream);
            String header = OFFontLoader.readHeader(reader);
            boolean supported = ttfFile.readFont(reader, header, "name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ttfFile;
        //
        //String fontFile = getConfig().getString("pdfService.apron.metadataTable.fontFile");
        //Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
        //return font;
        //
        
        //return FontEnum.valueOf(getConfig().getString("pdfService.apron.metadataTable.font")).getFont();
    }
    
    public static File getFOPConfigFile() {
        return new File(ServiceConfig.getConfig().getString("pdfService.apron.FOPconfig")).getAbsoluteFile();
    }
}
