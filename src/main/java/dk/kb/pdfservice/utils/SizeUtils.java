package dk.kb.pdfservice.utils;

import static org.apache.commons.io.FileUtils.ONE_EB;
import static org.apache.commons.io.FileUtils.ONE_GB;
import static org.apache.commons.io.FileUtils.ONE_KB;
import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.apache.commons.io.FileUtils.ONE_PB;
import static org.apache.commons.io.FileUtils.ONE_TB;

public class SizeUtils {
    
    public static long toBytes(String humanReadableSize){
        double value = Double.parseDouble(humanReadableSize.replaceAll("[^\\d.]", ""));
        //Find unit
        String unit = humanReadableSize.replaceAll("[^A-Z]","").trim();
        long multiplier = 1;
        
        switch (unit){
            case "EB":
                multiplier = ONE_EB;
                break;
            case "PB":
                multiplier = ONE_PB;
                break;
            case "TB":
                multiplier = ONE_TB;
                break;
            case "GB":
                multiplier = ONE_GB;
                break;
            case "MB":
                multiplier = ONE_MB;
                break;
            case "KB":
                multiplier = ONE_KB;
                break;
            case "B":
            default:
        }
        return (long)(value*multiplier);
    }
    
    public static String toHumanReadable(long size){
        final String displaySize;
    
        if (divide(size,ONE_EB) > 0) {
            displaySize = divide(size,ONE_EB) + " EB";
        } else if (divide(size,ONE_PB)  > 0) {
            displaySize = divide(size,ONE_PB) + " PB";
        } else if (divide(size,ONE_TB) > 0) {
            displaySize = divide(size,ONE_TB) + " TB";
        } else if (divide(size,ONE_GB) > 0) {
            displaySize = divide(size,ONE_GB) + " GB";
        } else if (divide(size,ONE_MB) > 0) {
            displaySize = divide(size,ONE_MB) + " MB";
        } else if (divide(size,ONE_KB) > 0) {
            displaySize = divide(size,ONE_KB) + " KB";
        } else {
            displaySize = size + " bytes";
        }
        return displaySize;
    }
    
    private static float divide(long a, long b){
        return (a*100 / b)/100.0f;
    }
}
