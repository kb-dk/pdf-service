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
        return dateOfPublication.plusYears(ServiceConfig.getYearsSincePublicationToBeOutsideCopyright())
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
        return parseDate(dateField);
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
            case "january":
            case "januar":
            case "jan":
                return Month.JANUARY;
            case "february":
            case "februar":
            case "feb":
                return Month.FEBRUARY;
            case "march":
            case "marts":
            case "mar":
                return Month.MARCH;
            case "april":
            case "apr":
                return Month.APRIL;
            case "may":
            case "maj":
                return Month.MAY;
            case "june":
            case "juni":
            case "jun":
                return Month.JUNE;
            case "july":
            case "juli":
            case "jul":
                return Month.JULY;
            case "august":
            case "aug":
                return Month.AUGUST;
            case "september":
            case "sep":
                return Month.SEPTEMBER;
            case "october":
            case "oktober":
            case "okt":
                return Month.OCTOBER;
            case "november":
            case "nov":
                return Month.NOVEMBER;
            case "december":
            case "dec":
                return Month.DECEMBER;
            default:
                return null;
        }
    }
    
    
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
