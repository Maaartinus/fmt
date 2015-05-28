package de.grajcar.fmt.misc;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public class _FmtIteratorAppenderTest extends TestCase {
	public void testEmpty() {
		check(ImmutableList.of().iterator(), "[]");
	}

	@SuppressWarnings("boxing") public void testSimple() {
		check(ImmutableList.of(1, 2, 3).iterator(), "[1, 2, 3]");
	}

	public void testByteArrayList() {
		check(ImmutableList.of(new byte[] {1, 100, -100}).iterator(), "[[1, 100, -100]]");
	}

	private void check(Object subject, String expected) {
		final StringBuilder sb = new StringBuilder();
		appender.appendTo(sb, context, subject);
		assertEquals(expected, sb.toString());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.ON_ERROR_THROWING);
	private static final FmtAppender appender = new FmtIteratorAppender();
}
