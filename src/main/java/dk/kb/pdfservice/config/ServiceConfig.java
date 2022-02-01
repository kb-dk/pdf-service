package dk.kb.pdfservice.config;

import com.google.common.util.concurrent.Striped;
import com.google.common.util.concurrent.StripedFactory;
import dk.kb.alma.client.AlmaRestClient;
import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.footer.FontEnum;
import dk.kb.pdfservice.utils.SizeUtils;
import dk.kb.util.yaml.YAML;
import org.apache.commons.io.FileUtils;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    
    private static ExecutorService
            threadPool = null;
    
    public static synchronized ExecutorService getPdfBuildersThreadPool() {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(ServiceConfig.getConcurrentPdfBuilds());
        }
        return threadPool;
    }
    
    
    
    public static MemoryUsageSetting getMemoryUsageSetting() {
        MemoryUsageSetting
                memUsageSetting
                = MemoryUsageSetting.setupMixed(SizeUtils.toBytes(ServiceConfig.getConfig()
                                                                               .getString(
                                                                                       "pdfService.temp.memoryForPDFs")));
        memUsageSetting.setTempDir(new File(ServiceConfig.getConfig().getString("pdfService.temp.folder")));
        return memUsageSetting;
    }
    
    public static void shutdown() {
        log.warn("ServiceConfig is being closed");
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //ignore
            } finally {
                threadPool.shutdownNow();
            }
        }
        
    }
    
    /**
     * Each key is mapped to as tripe. Num of stripes are the number of possible locks
     * So this means that at most 1023 locks (i.e. different files) can be handled at the same time
     * We do not use the special features of ReentrantLock, but it basically means that the same thread can lock a
     * Reentrant Lock multiple times, and must unlock the same number of times to release it
     * When fair=true locks favor granting access to the longest-waiting thread.
     */
    private static Striped<ReadWriteLock> stripedLock = null;
    
    public static synchronized Striped<ReadWriteLock> getPdfServeLocks() {
        if (stripedLock == null) {
            stripedLock
                    = StripedFactory.readWriteLockLazyWeak(ServiceConfig.getConcurrentServes(), true);
        }
        return stripedLock;
    }
    
    
    public static final String DEFAULT = "__default__";
    
    private static List<ApronMapping> apronMappingList = null;
    
    public static synchronized List<ApronMapping> getApronTypeMapping() {
        if (apronMappingList == null) {
            apronMappingList = new ArrayList<>();
            
            @NotNull List<YAML> entries = getConfig().getYAMLList("pdfService.apron.apronTypeMapper");
            for (YAML entry : entries) {
                
                String keyType = "999a";
                String key = entry.getString(keyType, null);
                if (key == null) {
                    keyType = "997a";
                    key     = entry.getString(keyType, null);
                    if (key == null) {
                        keyType = DEFAULT;
                        key     = entry.getString(keyType, null);
                    }
                }
                
                String type1 = entry.getString("apronWithinCopyright", null);
                ApronType value1 = ApronType.valueOf(type1);
                
                String type2 = entry.getString("apronOutOfCopyright", null);
                ApronType value2 = ApronType.valueOf(type2);
                
                apronMappingList.add(new ApronMapping(keyType, key, value1, value2));
            }
        }
        return apronMappingList;
    }
    
    public static List<String> getPdfSourcePath() {
        return getConfig().getList("pdfService.PDFsource");
    }
    
    
    public static String getPdfCachePath() {
        return getConfig().getString("pdfService.cache.cacheFolder");
    }
    
    public static TemporalAmount getMaxAgeTempPdf() {
        @NotNull Integer value = getConfig().getInteger(
                "pdfService.cache.maxAgeOfCachedPdfs.value");
        
        String unit = getConfig().getString(
                "pdfService.cache.maxAgeOfCachedPdfs.unit");
        return Duration.of(value, ChronoUnit.valueOf(unit));
    }
    
    //OldHeaders
    public static List<String> getHeaderLines() {
        return getConfig().getList("pdfService.apronRemoval.oldHeaderStrings");
    }
    
    public static File getOldHeaderImageDir() {
        return new File(ServiceConfig.getConfig().getString("pdfService.apronRemoval.oldHeaderImages.imageDirectory"));
    }
    
    public static Double getOldHeaderImagesmMaxDifferenceAllowedForMatch() {
        return ServiceConfig.getConfig()
                            .getDouble("pdfService.apronRemoval.oldHeaderImages.maxDifferenceAllowedForMatch");
    }
    
    private static List<BufferedImage> oldHeaderImages = null;
    
    public static synchronized List<BufferedImage> getOldHeaderImages() {
        if (oldHeaderImages == null) {
            oldHeaderImages = FileUtils.listFiles(ServiceConfig.getOldHeaderImageDir(), new String[]{"png"}, false)
                                       .stream()
                                       .map(file -> {
                                           try {
                                               return ImageIO.read(file);
                                           } catch (IOException e) {
                                               throw new UncheckedIOException(e);
                                           }
                                       })
                                       .collect(Collectors.toList());
        }
        return oldHeaderImages;
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
    public static List<String> getTheatreCriteria() {
        return getConfig().getList("pdfService.theaterCriteria.999a");
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
    
    public static PDFont getCopyrightFooterFont() {
        return FontEnum.valueOf(getConfig().getString("pdfService.copyrightFooter.Font")).getFont();
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
    
    private static TTFFile ttfFile = null;
    
    public static synchronized TTFFile getApronMetadataTableFont() {
        if (ttfFile == null) {
            File file = new File(getConfig().getString("pdfService.apron.metadataTable.fontFile")).getAbsoluteFile();
            
            ttfFile = new TTFFile(false, true);
            try (InputStream stream = new FileInputStream(file)) {
                FontFileReader reader = new FontFileReader(stream);
                String header = OFFontLoader.readHeader(reader);
                boolean supported = ttfFile.readFont(reader, header, "name");
                if (!supported) {
                    log.warn("Font file {} cannot be read as a truetype font", file);
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ttfFile;
    }
    
    private static Map<Integer, OFMtxEntry> fontWidthMap = null;
    
    public static synchronized Map<Integer, OFMtxEntry> getFontWidthMap() {
        if (fontWidthMap == null) {
            fontWidthMap = Collections.unmodifiableMap(
                    ttfFile.getMtx()
                           .stream()
                           .filter(ofMtxEntry -> !ofMtxEntry.getUnicodeIndex().isEmpty())
                           .collect(Collectors.toMap(ofMtxEntry -> ofMtxEntry.getUnicodeIndex().get(0),
                                                     Function.identity())));
        }
        return fontWidthMap;
    }
    
    public static File getFOPConfigFile() {
        return new File(ServiceConfig.getConfig().getString("pdfService.apron.FOPconfig")).getAbsoluteFile();
    }
    
    public static String getPrimoLink(String mmsId) {
        return getConfig().getString("pdfService.apron.primo.host")
               + getConfig().getString("pdfService.apron.primo.path")
               + mmsId
               + getConfig().getString("pdfService.apron.primo.postfix");
    }
    
    public static int getConcurrentServes() {
        return getConfig().getInteger("pdfService.concurrency.numConcurrentCacheDownloads");
    }
    
    public static int getConcurrentPdfBuilds() {
        return getConfig().getInteger("pdfService.concurrency.numConcurrentPdfConstructions");
    }
    
}
