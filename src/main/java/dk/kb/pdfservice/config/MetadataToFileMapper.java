package dk.kb.pdfservice.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MetadataToFileMapper {
    
    private final Map<File, String> map;
    
    public MetadataToFileMapper() {
        //TODO make this an interface and implement other, more persistent backends
        map = Collections.synchronizedMap(new HashMap<>());
    }
    
    public String getMetadataChecksumForFile(File cachedPdfFile) {
        return map.get(cachedPdfFile);
    }
    
    public void updateMetadataChecksumForFile(File file, String checksum){
        map.put(file, checksum);
    }
    
}
