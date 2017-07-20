package com.keenvil.cork.date;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.DayOfWeek;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;

/**
 * Date Utils to work with date in ISO8601 format and in UTC.
 */
public class DateUtils {

  private static Logger log = getLogger(DateUtils.class);

  /**
   * Returns current date time in ISO8601 and UTC.
   * 
   * @return date time.
   */
  public static Date nowInUtc() {
    return DateTime.now(DateTimeZone.UTC).toDate();
  }

  /**
   * Current date time plus a given amount if minutes.
   * 
   * @param minutes Minutes to be added to the current date time.
   * @return date time.
   */
  public static Date nowPlusMinutesInUtc(int minutes) {
    return DateTime.now(DateTimeZone.UTC).plusMinutes(minutes).toDate();
  }

  /**
   * Parse a string date time with the given formatter.
   * 
   * @param text String date time to be parsed.
   * @param formatter Formatter.
   * @return date time.
   */
  public static Date parseWithFormat(final String text,
      DateTimeFormatter formatter) {
    if (log.isInfoEnabled()) {
      log.info("Parsing date for [{}].", text);
    }

    Date date = null;
    try {
      date = DateTime.parse(text, formatter).toDate();      
    } catch (IllegalArgumentException exception) {
      throw new InvalidArgument(format("Invalid date format for [%s].", text),
          exception);
    }
    return date;
  }

  /**
   * Parse a date time string as a date time.
   * 
   * @param text String to be parsed.
   * @return date time.
   */
  public static Date parseIsoDateTime(final String text) {
    DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();
    return parseWithFormat(text, formatter);
  }

  /**
   * Parse a date time string as a time.
   * 
   * @param text String to be parsed.
   * @return datetime.
   */
  public static Date parseIsoTime(final String text) {
    DateTimeFormatter formatter = ISODateTimeFormat.time().withZoneUTC();
    return parseWithFormat(text, formatter);
  }

  /**
   * Returns the day of the week for the given date.
   * 
   * @param date Date time.
   * @return date time.
   */
  public static DayOfWeek dayOfWeek(Date date) {
    DateTime dateTime = new DateTime(date);
    return DayOfWeek.of(dateTime.getDayOfWeek());
  }

  /**
   * Returns the time for the given date time.
   * @param date Date time.
   * @return date time.
   */
  public static Date time(Date date) {
    DateTime dateTime = new DateTime(date);
    String truncatedTime = dateTime.toString(ISODateTimeFormat.time());
    return DateTime.parse(truncatedTime, ISODateTimeFormat.time()).toDate();
  }
}
