package de.grajcar.fmt.misc;

import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public class _FmtMapAppenderTest extends TestCase {
	public void testEmpty() {
		check(ImmutableMap.of(), "{}");
	}

	@SuppressWarnings("boxing") public void testSimple() {
		check(ImmutableMap.of(1, 2), "{1=2}");
	}

	public void testByteArrayList() {
		final byte[] b = {1, 2, -1};
		check(ImmutableMap.of("a", b), "{\"a\"=[1, 2, 255]}");
	}

	private void check(Object subject, String expected) {
		final StringBuilder sb = new StringBuilder();
		appender.appendTo(sb, context, subject);
		assertEquals(expected, sb.toString());
		assertEquals(expected, context.fmt().format("[]", subject).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.ON_ERROR_THROWING)
			.prefer("u", byte[].class, true)
			.prefer("j", String.class, true);
	private static final FmtAppender appender = new FmtMapAppender();
}
