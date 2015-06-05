package de.grajcar.fmt.misc;

import junit.framework.TestCase;

import de.grajcar.fmt.Fmt;
import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;
import de.grajcar.fmt.FmtOption;

public class _FmtCharSequenceAppenderTest extends TestCase {
	public void testNormal() {
		final FmtKey fmtKey = new FmtKey("n", String.class);
		final FmtAppender appender = new FmtCharSequenceAppender().delegateAppender(fmtKey);
		final StringBuilder sb = new StringBuilder();
		for (final String s : testStrings) {
			appender.appendTo(sb, context, s);
			assertEquals(s, sb.toString());
			sb.delete(0, sb.length());
		}
	}

	public void testJavaSyntax() {
		final FmtKey fmtKey = new FmtKey("j", String.class);
		final FmtAppender appender = new FmtCharSequenceAppender().delegateAppender(fmtKey);
		final StringBuilder sb = new StringBuilder();
		for (int i=0; i<testStrings.length; i+=2) {
			final String s = testStrings[i];
			final String expected = testStrings[i+1];
			appender.appendTo(sb, context, s);
			assertEquals(expected, sb.toString());
			sb.delete(0, sb.length());
		}
	}

	public void testPreferJavaSyntax() {
		final Fmt fmt = context.prefer("j", String.class, false).fmt();
		for (int i=0; i<testStrings.length; i+=2) {
			final String s = testStrings[i];
			final String expected = testStrings[i+1];
			assertEquals(expected, fmt.format("[]", s).take());
		}
	}

	private final String[] testStrings = {
			"abc", "\"abc\"",
			"\r\n", "\"\\r\\n\"",
			"\"\\", "\"\\\"\\\\\"",
	};

	private static final FmtContext context = FmtContext.richContext(FmtOption.ON_ERROR_THROWING);
}
