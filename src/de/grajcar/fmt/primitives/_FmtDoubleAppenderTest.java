package de.grajcar.fmt.primitives;

import java.util.Locale;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;
import de.grajcar.fmt.FmtOption;

public final class _FmtDoubleAppenderTest extends TestCase {
	public void testDelegateAppender() {
		assertNotNull(newAppender(""));
		assertNull(newAppender("x")); //TODO
	}

	public void testAppendTo() {
		checkAppendTo("100.0", 100);
		checkAppendTo("3.14", 3.14);
	}

	public void test_toString() {
		check("-100.0", "%s", -100f);
		check("-100.000", "%6.3f", -100f);
		check("-1.000000e+02", "%e", -100f);
		context = context.withLocale(Locale.GERMAN);
		check("-100.0", "%s", -100f);
		check("-100,000", "%6.3f", -100f);
		check("-1,000000e+02", "%e", -100f);
	}

	private void checkAppendTo(String expected, double subject) {
		final StringBuilder sb = new StringBuilder();
		newAppender("").appendTo(sb, context, subject);
		assertEquals(expected, sb.toString());
	}

	private FmtPrimitiveAppender newAppender(String specifier) {
		return new FmtPrimitiveAppender().delegateAppender(new FmtKey(specifier, Double.class));
	}

	private void check(String expected, String specifier, double value) {
		assertEquals(expected, context.fmt().add("", specifier, Double.valueOf(value)).take());
	}

	private static final FmtContext rawContext = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
	private FmtContext context = rawContext;
}
