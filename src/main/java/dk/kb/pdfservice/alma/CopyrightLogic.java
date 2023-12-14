package dk.kb.pdfservice.alma;

import dk.kb.alma.gen.bibs.Bib;
import dk.kb.pdfservice.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopyrightLogic {
    
    private static final Logger log = LoggerFactory.getLogger(CopyrightLogic.class);
    private static final String PREFIX = "^.*?";
    private static final String POSTFIX = ".*?$";
    
    //17. maj 2012
    /**
     * Extract date from DD-MonthName-YYYY
     * Day can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * It does NOT need to be the same in both places
     **/
    private static final Pattern yearFromNamedMonthPattern = Pattern.compile(PREFIX
                                                                             + "(?<day>\\d{1,2})[-/ .]+(?<month>\\w*[a-zA-Z]\\w*)[-/ .]+(?<year>\\d{4})"
                                                                             + POSTFIX);
    /**
     * Extract date from format DD-MM-YYYY
     * Day and Month can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * The separator must be same in both places
     */
    private static final Pattern yearFromDMYPattern = Pattern.compile(PREFIX
                                                                      + "(?<day>\\d{1,2})(?<sep>[-/ .]+)(?<month>\\d{1,2})\\k<sep>(?<year>\\d{4})"
                                                                      + POSTFIX);
    /**
     * Extract date from format YYYY-MM-DD
     * Day and Month can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * The separator must be same in both places
     */
    private static final Pattern yearFromYMDPattern = Pattern.compile(PREFIX
                                                                      + "(?<year>\\d{4})(?<sep>[-/ .]+)(?<month>\\d{1,2})\\k<sep>(?<day>\\d{1,2})"
                                                                      + POSTFIX);
    
    /**
     * Extract year from format YYYY-YYYY. Returns 1. of jan of the first year in the pair
     */
    private static final Pattern yearFromD4D4Pattern = Pattern.compile(PREFIX + "(?<year>\\d{4})-\\d{4}\\.?" + POSTFIX);
    
    /**
     * Extract year from format YYYY og YYYY. Returns 1. of jan of the first year in the pair
     * Introduced to cover publication format: "mellem YYYY og YYYY"
     */
    private static final Pattern yearFromD4OgD4Pattern = Pattern.compile(PREFIX + "mellem (?<year>\\d{4}) og \\d{4}\\.?" + POSTFIX);

    /**
     * Extract year from format YYYY. Returns 1. of jan of the year
     */
    private static final Pattern yearFromD4Pattern = Pattern.compile(PREFIX + "(?<year>\\d{4})" + POSTFIX);
    
    /**
     * Extract year from format YYYY. Returns 1. of jan of the year
     * [175-?]
     */
    private static final Pattern yearFromD3Pattern = Pattern.compile(PREFIX + "\\[(?<year>\\d{3})-\\?\\]" + POSTFIX);
    
    private static final Pattern yearFromD2Pattern = Pattern.compile(PREFIX + "\\[(?<year>\\d{2})--\\?\\]" + POSTFIX);
    
    private static final Pattern yearFromSAPattern = Pattern.compile(PREFIX + "\\[(?<year>\\d{0})s\\.a\\.]" + POSTFIX);
    
    
    protected static boolean isWithinCopyright(LocalDate dateOfPublication) {
        
        if (dateOfPublication == null) {
            //If we cannot parse a date, it is ALWAYS too young
            return true;
        }
        return dateOfPublication.plus(ServiceConfig.getTimeSincePublicationToBeOutsideCopyright())
                                .isAfter(LocalDate.now(ZoneId.systemDefault()));
    }
    
    protected static LocalDate getPublicationDate(String dateField) {
    
        log.debug("input date is {}", dateField);
        LocalDate parsedDate;
        if (dateField == null) {
            parsedDate = LocalDate.now(ZoneId.systemDefault());
        } else {
            parsedDate = parseDate(dateField);
        }
        log.info("Input date {} parsed to {}", dateField, parsedDate);
        return parsedDate;
    }
    
    protected static String getPublicationDateString(Bib bib, Element marc21, RecordType recordType) {
        final String dateOfPublication = bib.getDateOfPublication();
        log.debug("Alma dateOfPublication (should correspond to marc008) {}",dateOfPublication);
        
        final Optional<String> tag260c = MarcClient.getString(marc21, "260", "c");
        log.debug("tag260c {}", tag260c);
        
        
        final Optional<String> premiere =
                MarcClient.ifTeater(recordType, () -> {
                              List<String> tag500a = MarcClient.getStrings(marc21, "500", "a");
                              log.debug("tag500a {}", tag500a);
                              return tag500a;
                          })
                          .stream()
                          .filter(str -> str.startsWith("Premiere"))
                          .map(a -> a.split(" ", 3)[1])
                          .findFirst();
        
        return premiere.orElse(tag260c.orElse(dateOfPublication));
    }
    
    @Nullable
    public static LocalDate parseDate(String dateField) {
    // Bemærk: Rækkefølgen betyder alt! Sørg for at køre alle tests - også PdfTitlePageCleanerTest før prod!
        Optional<LocalDate> firstYear =
                parsePublicationDateWithPattern(dateField, yearFromD4OgD4Pattern, "yearFromD4OgD4Pattern")
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromD4D4Pattern, "yearFromD4D4Pattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromNamedMonthPattern, "yearFromNamedMonthPattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromYMDPattern, "yearFromYMDPattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromDMYPattern, "yearFromDMYPattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromD4Pattern, "yearFromD4Pattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromD3Pattern, "yearFromD3Pattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromD2Pattern, "yearFromD2Pattern"))
                        .or(() -> parsePublicationDateWithPattern(dateField, yearFromSAPattern, "yearFromSAPattern"));
/*

        Optional<LocalDate> firstYear =
                parsePublicationDateWithPattern(dateField, yearFromD4OgD4Pattern, "yearFromD4OgD4Pattern");

*/
        return firstYear.orElse(null);
    }
    
    
    private static Optional<LocalDate> parsePublicationDateWithPattern(String tag260c, Pattern pattern, String patternName) {
        //17. maj 2012
        final Matcher matcher = pattern.matcher(tag260c);
        if (matcher.matches()) {
            log.debug(tag260c +  " matcher: " + patternName + " " + pattern );
            int firstYear = getFirstYear(matcher);
            Month firstMonth = parseMonth(getGroupOrDefault(matcher, "month", "01"));
            if (firstMonth == null) {
                firstMonth = Month.JANUARY;
            }
            int firstDay = Integer.parseInt(getGroupOrDefault(matcher, "day", "1"));
            return Optional.of(LocalDate.of(firstYear, firstMonth, firstDay));
        }
        return Optional.empty();
    }
    
    private static int getFirstYear(Matcher matcher) {
        String yearRaw = matcher.group("year");
        if (yearRaw == null || yearRaw.isEmpty()) {
            //It no year specified, return current year
            return LocalDate.now(ZoneId.systemDefault()).getYear();
        } else {
            //Ensure that the year is 4 digits long
            String year4Digits = (yearRaw + "99").substring(0, 4);
            return Integer.parseInt(year4Digits); //year must be there
        }
    }
    
    private static String getGroupOrDefault(Matcher matcher, String group, String defaultValue) {
        try {
            return matcher.group(group);
        } catch (IllegalArgumentException e) {
            //Group not defined in this pattern
            return defaultValue;
        }
    }
    
    
    private static Month parseMonth(String month) {
        if (month == null) {
            return null;
        }
        month = month.toLowerCase(Locale.getDefault());
        switch (month) {
            case "01":
            case "1":
            case "january":
            case "januar":
            case "jan":
                return Month.JANUARY;
            case "02":
            case "2":
            case "february":
            case "februar":
            case "feb":
                return Month.FEBRUARY;
            case "03":
            case "3":
            case "march":
            case "marts":
            case "mar":
                return Month.MARCH;
            case "04":
            case "4":
            case "april":
            case "apr":
                return Month.APRIL;
            case "05":
            case "5":
            case "may":
            case "maj":
                return Month.MAY;
            case "06":
            case "6":
            case "june":
            case "juni":
            case "jun":
                return Month.JUNE;
            case "07":
            case "7":
            case "july":
            case "juli":
            case "jul":
                return Month.JULY;
            case "08":
            case "8":
            case "august":
            case "aug":
                return Month.AUGUST;
            case "09":
            case "9":
            case "september":
            case "sep":
                return Month.SEPTEMBER;
            case "10":
            case "october":
            case "oktober":
            case "okt":
                return Month.OCTOBER;
            case "11":
            case "november":
            case "nov":
                return Month.NOVEMBER;
            case "12":
            case "december":
            case "dec":
                return Month.DECEMBER;
            default:
                return null;
        }
    }
    
}
