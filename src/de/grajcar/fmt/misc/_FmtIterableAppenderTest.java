package de.grajcar.fmt.misc;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public class _FmtIterableAppenderTest extends TestCase {
	public void testEmpty() {
		check(ImmutableList.of(), "[]");
	}

	@SuppressWarnings("boxing") public void testSimple() {
		check(ImmutableList.of(100, 200), "[64, c8]");
	}

	public void testByteArrayList() {
		check(ImmutableList.of(new byte[] {1, 100, -100}), "[[1, 100, 156]]");
	}

	private void check(Object subject, String expected) {
		final StringBuilder sb = new StringBuilder();
		appender.appendTo(sb, context, subject);
		assertEquals(expected, sb.toString());
		assertEquals(expected, context.fmt().format("[]", subject).take());
	}

	private static final FmtContext context = FmtContext.richContext(FmtOption.ON_ERROR_THROWING)
			.prefer("x", Integer.class, false)
			.prefer("u", byte[].class, true);
	private static final FmtAppender appender = new FmtIterableAppender();
}
