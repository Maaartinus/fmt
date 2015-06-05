package de.grajcar.fmt;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

@SuppressWarnings("boxing") public final class _FmtContextTest extends TestCase {
	public void testRawPoor() {
		final FmtContext context = FmtContext.poorContext(FmtOption.LOCALIZED_NO);
		assertIsPoor(context);
		assertIsRaw(context);
	}

	public void testRawLocalized() {
		final FmtContext context = FmtContext.poorContext(FmtOption.LOCALIZED_YES);
		assertIsPoor(context);
		assertIsLocalized(context);
	}

	public void testRichRaw() {
		final FmtContext context = FmtContext.richContext(FmtOption.LOCALIZED_NO);
		assertIsRich(context);
		assertIsRaw(context);
	}

	public void testRichLocalized() {
		final FmtContext context = FmtContext.richContext(FmtOption.LOCALIZED_YES);
		assertIsRich(context);
		assertIsLocalized(context);
	}

	public void testPrefer() {
		final FmtContext preferringContext = FmtContext
				.richContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING)
				.prefer("X", Byte.class, false)
				.prefer("px", Long.class, false);
		assertEquals("fffffffffffffffe", preferringContext.stringify(-2L));
		assertEquals("C8", preferringContext.stringify((byte) 200));
	}

	public void testPrefer2() {
		final Calendar calendar = new GregorianCalendar(2014, 8, 16, 20, 46, 25);
		final long millis = calendar.getTimeInMillis() + 123;
		final FmtContext context = FmtContext
				.richContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING)
				.prefer("dd.MM.yyyy (EEE)", java.sql.Date.class, true)
				.prefer("hh:mm:ss.SSS", java.sql.Time.class, true)
				.prefer("yyMMdd-hhmmss", java.util.Date.class, false);
		assertEquals("16.09.2014 (Tue)", context.stringify(new java.sql.Date(millis)));
		assertEquals("08:46:25.123", context.stringify(new java.sql.Time(millis)));
		assertEquals("140916-084625", context.stringify(new java.util.Date(millis)));
	}

	private void assertIsPoor(FmtContext context) {
		final String actual = context.fmt().add("", "", bytes).toString();
		assertEquals("[B@" + String.format("%x", System.identityHashCode(bytes)), actual);
	}

	private void assertIsRich(FmtContext context) {
		final String actual = context.fmt().add("", "", bytes).toString();
		assertEquals("[0, 1]", actual);
	}

	private void assertIsRaw(FmtContext context) {
		assertEquals(Locale.ENGLISH, context.locale());
	}

	private void assertIsLocalized(FmtContext context) {
		assertEquals(Locale.GERMAN, context.locale());
	}

	private final byte[] bytes = new byte[] {0, 1};
	{
		Locale.setDefault(Locale.GERMAN);
	}
}
