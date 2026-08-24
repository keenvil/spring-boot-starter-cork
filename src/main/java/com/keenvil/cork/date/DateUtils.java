package com.keenvil.cork.date;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.slf4j.Logger;

import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;

/**
 * Date Utils to work with date in ISO8601 format and in UTC.
 */
public class DateUtils {

  private static Logger log = getLogger(DateUtils.class);

  /**
   * Returns current date time in UTC.
   *
   * @return date time.
   */
  public static Date nowInUtc() {
    return Date.from(Instant.now());
  }

  /**
   * Current date time plus a given amount of minutes.
   *
   * @param minutes Minutes to be added to the current date time.
   * @return date time.
   */
  public static Date nowPlusMinutesInUtc(int minutes) {
    return Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
  }

  /**
   * Parse a string date time with the given formatter.
   *
   * @param text String date time to be parsed.
   * @param formatter java.time DateTimeFormatter.
   * @return date time.
   */
  public static Date parseWithFormat(final String text,
      DateTimeFormatter formatter) {
    if (log.isInfoEnabled()) {
      log.info("Parsing date for [{}].", text);
    }
    try {
      return Date.from(OffsetDateTime.parse(text, formatter).toInstant());
    } catch (DateTimeParseException exception) {
      throw new InvalidArgument(format("Invalid date format for [%s].", text),
          exception);
    }
  }

  /**
   * Parse a date time string as a date time.
   *
   * @param text String to be parsed.
   * @return date time.
   */
  public static Date parseIsoDateTime(final String text) {
    return parseWithFormat(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  /**
   * Parse a time string as a date anchored at epoch day.
   *
   * @param text String to be parsed.
   * @return datetime.
   */
  public static Date parseIsoTime(final String text) {
    if (log.isInfoEnabled()) {
      log.info("Parsing time for [{}].", text);
    }
    try {
      OffsetTime offsetTime = OffsetTime.parse(text, DateTimeFormatter.ISO_OFFSET_TIME);
      return Date.from(offsetTime.atDate(LocalDate.ofEpochDay(0)).toInstant());
    } catch (DateTimeParseException exception) {
      throw new InvalidArgument(format("Invalid time format for [%s].", text),
          exception);
    }
  }

  /**
   * Returns the day of the week for the given date.
   *
   * @param date Date time.
   * @return day of week.
   */
  public static DayOfWeek dayOfWeek(Date date) {
    return date.toInstant().atOffset(ZoneOffset.UTC).getDayOfWeek();
  }

  /**
   * Returns the time for the given date time, anchored at epoch day.
   * @param date Date time.
   * @return date time with only the time component preserved.
   */
  public static Date time(Date date) {
    OffsetTime offsetTime = date.toInstant().atOffset(ZoneOffset.UTC).toOffsetTime();
    return Date.from(offsetTime.atDate(LocalDate.ofEpochDay(0)).toInstant());
  }
}
