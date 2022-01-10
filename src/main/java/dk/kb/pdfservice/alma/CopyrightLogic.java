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
    
    protected static boolean isWithinCopyright(LocalDate dateOfPublication) {
        
        if (dateOfPublication == null) {
            //If we cannot parse a date, it is ALWAYS too young
            return true;
        }
        return dateOfPublication.plus(ServiceConfig.getTimeSincePublicationToBeOutsideCopyright())
                                .isAfter(LocalDate.now(ZoneId.systemDefault()));
    }
    
    protected static LocalDate getPublicationDate(Bib bib, Element marc21) {
        
        final String dateOfPublication = bib.getDateOfPublication();
        
        final String tag260c = MarcClient.getString(marc21, "260", "c").orElse(null);
        log.debug("tag260c {}", tag260c);
        
        
        final List<String> tag500a = MarcClient.getStrings(marc21, "500","a");
        log.debug("tag500a {}", tag500a);
        final Optional<String> premiere = tag500a.stream().filter(str -> str.startsWith("Premiere")).findFirst();
        
        final String dateField;
        if (premiere.isPresent()) {
            dateField = premiere.get().split(" ", 3)[1];
        } else if (tag260c != null) {
            dateField = tag260c;
        } else {
            dateField = dateOfPublication;
        }
        
        log.debug("input date is {}", dateField);
        LocalDate parsedDate = parseDate(dateField);
        log.info("Input date {} parsed to {}",dateField, parsedDate);
        return parsedDate;
    }
    
    @Nullable
    public static LocalDate parseDate(String dateField) {
        Optional<LocalDate> firstYear =
                extractPubYearFromD4_D4(dateField)
                        .or(() -> extractPubYearFromDateNamed(dateField))
                        .or(() -> extractPubYearFromDateISO(dateField))
                        .or(() -> extractPubYearFromDateCommon(dateField))
                        .or(() -> extractPubYearFromD4(dateField));
        
        
        return firstYear.orElse(null);
    }
    
    /**
     * Extract date from DD-MonthName-YYYY
     * Day can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * It does NOT need to be the same in both places
     * @param tag260c
     * @return the date
     * @see #parseMonth(String)
     */
    private static Optional<LocalDate> extractPubYearFromDateNamed(String tag260c) {
        //17. maj 2012
        Pattern date = Pattern.compile("(?<day>\\d{1,2})[-/ .]+(?<month>\\w+)[-/ .]+(?<year>\\d{4}).*");
        final Matcher matcher = date.matcher(tag260c);
        if (matcher.matches()) {
            int firstYear = Integer.parseInt(matcher.group("year"));
            Month firstMonth = parseMonth(matcher.group("month"));
            if (firstMonth == null) {
                return Optional.empty();
            }
            int firstDay = Integer.parseInt(matcher.group("day"));
            return Optional.of(LocalDate.of(firstYear, firstMonth, firstDay));
        }
        return Optional.empty();
    }
    
    private static Month parseMonth(String month) {
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
    
    /**
     * Extract date from format DD-MM-YYYY
     * Day and Month can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * The separator must be same in both places
     * @param tag260c
     * @return the date
     */
    private static Optional<LocalDate> extractPubYearFromDateCommon(String tag260c) {
        Pattern date = Pattern.compile("(?<day>\\d{1,2})(?<sep>[-/ .]+)(?<month>\\d{1,2})\\k<sep>(?<year>\\d{4}).*");
        final Matcher matcher = date.matcher(tag260c);
        if (matcher.matches()) {
            int firstYear = Integer.parseInt(matcher.group("year"));
            int firstMonth = Integer.parseInt(matcher.group("month"));
            int firstDay = Integer.parseInt(matcher.group("day"));
            return Optional.of(LocalDate.of(firstYear, firstMonth, firstDay));
        }
        return Optional.empty();
    }
    
    /**
     * Extract date from format YYYY-MM-DD
     * Day and Month can be 1 or 2 digits
     * Separator can be any combination of "-" "/" " " and "."
     * The separator must be same in both places
     * @param tag260c
     * @return the date
     */
    private static Optional<LocalDate> extractPubYearFromDateISO(String tag260c) {
        Pattern date = Pattern.compile("(?<year>\\d{4})(?<sep>[-/ .]+)(?<month>\\d{1,2})\\k<sep>(?<day>\\d{1,2}).*");
        final Matcher matcher = date.matcher(tag260c);
        if (matcher.matches()) {
            int firstYear = Integer.parseInt(matcher.group("year"));
            int firstMonth = Integer.parseInt(matcher.group("month"));
            int firstDay = Integer.parseInt(matcher.group("day"));
            return Optional.of(LocalDate.of(firstYear, firstMonth, firstDay));
        }
        return Optional.empty();
    }
    
    /**
     * Extract year from format YYYY-YYYY
     * @param tag260c
     * @return 1. of jan of the first year in the pair
     */
    private static Optional<LocalDate> extractPubYearFromD4_D4(String tag260c) {
        Pattern year2year = Pattern.compile("(\\d{4})-\\d{4}\\.?");
        final Matcher matcher = year2year.matcher(tag260c);
        if (matcher.matches()) {
            String firstYearString = matcher.group(1);
            int firstYear = Integer.parseInt(firstYearString);
            return Optional.of(LocalDate.of(firstYear, 1, 1));
        }
        return Optional.empty();
    }
    
    /**
     * Extract year from format YYYY
     * @param tag260c
     * @return 1. of jan of the year
     */
    private static Optional<LocalDate> extractPubYearFromD4(String tag260c) {
        Pattern year = Pattern.compile("(\\d{4}).*");
        final Matcher matcher = year.matcher(tag260c);
        if (matcher.matches()) {
            String firstYearString = matcher.group(1);
            int firstYear = Integer.parseInt(firstYearString);
            return Optional.of(LocalDate.of(firstYear, 1, 1));
        }
        return Optional.empty();
    }
}
