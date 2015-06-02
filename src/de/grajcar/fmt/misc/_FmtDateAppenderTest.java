package de.grajcar.fmt.misc;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import de.grajcar.fmt.Fmt;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public class _FmtDateAppenderTest extends TestCase {
	public void test_Default() {
		final String s = context.fmt().add("", "", JU_DATE).take();
		assertEquals("15-06-02T05:12:13", s);
	}

	public void test_DayOnly() {
		final String s = context.fmt().add("", "yy-MM-dd", JU_DATE).take();
		assertEquals("15-06-02", s);
	}

	public void test_TimeWithMillis() {
		final String s = context.fmt().add("", "HH:mm:ss.S", JU_DATE).take();
		assertEquals("05:12:13.987", s);
	}

	public void test_SqlTime() {
		final String s = context.fmt().add("", "", new java.sql.Time(JU_DATE.getTime())).take();
		assertEquals("05:12:13", s);
	}

	public void test_SqlDate() {
		final String s = context.fmt().add("", "", new java.sql.Date(JU_DATE.getTime())).take();
		assertEquals("15-06-02", s);
	}

	public void test_SqlTimestamp() {
		final String s = context.fmt().add("", "", new java.sql.Timestamp(JU_DATE.getTime())).take();
		assertEquals("15-06-02T05:12:13", s);
	}

	public void test_TimeZone() {
		final TimeZone timeZone = TimeZone.getTimeZone("PST");
		final Fmt fmt = context.withTimeZone(timeZone).fmt();
		assertEquals("PDT", fmt.add("", "z", JU_DATE).take()); // PST -> PDT because of summer
		assertEquals("15-06-01T20:12:13", fmt.add("", "", JU_DATE).take());
	}

	private static final FmtContext context =
			FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);

	private static final Calendar CALENDAR = new GregorianCalendar(2015, 5, 2, 3, 12, 13);
	static {
		CALENDAR.set(Calendar.MILLISECOND, 987);
		CALENDAR.setTimeZone(TimeZone.getTimeZone("CEST"));
	}
	private static final Date JU_DATE = CALENDAR.getTime();
}
