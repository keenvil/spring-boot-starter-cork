package com.keenvil.cork.date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

  @Test
  void parseaFechaIsoConOffset() {
    Date date = DateUtils.parseIsoDateTime("2026-10-21T13:00:00-03:00");
    assertThat(date.toInstant(), is(Instant.parse("2026-10-21T16:00:00Z")));
  }

  @Test
  void fechaInvalidaEsInvalidArgument() {
    InvalidArgument e = assertThrows(InvalidArgument.class, () -> DateUtils.parseIsoDateTime("21/10/2026"));
    assertThat(e.getMessage(), is("Invalid date format for [21/10/2026]."));
  }

  @Test
  void horaIsoQuedaAnclada1970EnUtc() {
    Date time = DateUtils.parseIsoTime("08:30:00-03:00");
    assertThat(time.toInstant(), is(Instant.parse("1970-01-01T11:30:00Z")));
  }

  @Test
  void horaInvalidaEsInvalidArgument() {
    assertThrows(InvalidArgument.class, () -> DateUtils.parseIsoTime("8 y media"));
  }

  @Test
  void diaDeLaSemanaYHoraEnUtc() {
    Date date = Date.from(Instant.parse("2026-10-21T23:15:00Z"));
    assertThat(DateUtils.dayOfWeek(date), is(DayOfWeek.WEDNESDAY));
    assertThat(DateUtils.time(date).toInstant(), is(Instant.parse("1970-01-01T23:15:00Z")));
  }

  @Test
  void ahoraYAhoraMasMinutos() {
    long before = System.currentTimeMillis();
    Date now = DateUtils.nowInUtc();
    Date later = DateUtils.nowPlusMinutesInUtc(10);
    assertTrue(now.getTime() >= before);
    long diff = later.getTime() - now.getTime();
    assertTrue(diff >= 10 * 60_000L && diff < 11 * 60_000L);
  }
}
