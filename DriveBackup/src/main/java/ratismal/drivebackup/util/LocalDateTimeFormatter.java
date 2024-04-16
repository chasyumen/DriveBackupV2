package ratismal.drivebackup.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ratismal.drivebackup.configuration.ConfigurationObject;
import ratismal.drivebackup.platforms.DriveBackupInstance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class LocalDateTimeFormatter {
    private static final String FORMAT_KEYWORD = "%FORMAT";
    private static final String FORMAT_REPLACEMENT = "'yyyy-M-d--HH-mm'";
    private static final Pattern VALID_FORMAT = Pattern.compile("^[\\w\\-.'% ]+$");
    private static final Pattern FORMAT = Pattern.compile(FORMAT_KEYWORD, Pattern.LITERAL);
    
    private final DateTimeFormatter formatter;
    private final DriveBackupInstance instance;

    @Contract (pure = true)
    private LocalDateTimeFormatter(DriveBackupInstance instance, DateTimeFormatter formatter) {
        this.formatter = formatter;
        this.instance = instance;
    }

    @NotNull
    @Contract ("_, _ -> new")
    public static LocalDateTimeFormatter ofPattern(DriveBackupInstance instance, String pattern) throws IllegalArgumentException {
        verifyPattern(pattern);
        if (pattern.contains(FORMAT_KEYWORD)) {
            int frontOffset = pattern.startsWith(FORMAT_KEYWORD) ? 2 : 0;
            int backOffset = pattern.endsWith(FORMAT_KEYWORD) ? 2 : 0;
            pattern = "'" + FORMAT.matcher(pattern).replaceAll(FORMAT_REPLACEMENT) + "'";
            pattern = pattern.substring(frontOffset, pattern.length() - backOffset);
        }
        return new LocalDateTimeFormatter(instance, DateTimeFormatter.ofPattern(pattern));
    }

    public @NotNull String format(@NotNull ZonedDateTime timeDate) {
        return timeDate.format(getFormatter());
    }

    public @NotNull ZonedDateTime parse(String text) throws DateTimeParseException {
        return ZonedDateTime.parse(text, getFormatter());
    }

    @NotNull
    private DateTimeFormatter getFormatter() {
        ConfigurationObject config = instance.getConfigHandler().getConfig();
        Locale locale = Locale.forLanguageTag(config.getValue("advanced", "date-language").getString());
        ZoneOffset zoneOffset = ZoneOffset.of(config.getValue("advanced", "date-timezone").getString());
        return formatter.withLocale(locale).withZone(zoneOffset);
    }

    private static void verifyPattern(String pattern) throws IllegalArgumentException {
        boolean isValid = true;
        if (!VALID_FORMAT.matcher(pattern).find()) {
            isValid = false;
        }
        if (pattern.contains(FORMAT_KEYWORD)) {
            if (pattern.contains("'")) {
                isValid = false;
            }
        } else {
            if (pattern.contains("%") && !pattern.contains("%NAME")) {
                isValid = false;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Format pattern contains illegal characters");
        }
    }
}
