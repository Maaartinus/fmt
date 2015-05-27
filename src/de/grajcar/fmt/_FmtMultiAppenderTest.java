package de.grajcar.fmt;

import junit.framework.TestCase;

@SuppressWarnings("boxing") public final class _FmtMultiAppenderTest extends TestCase {
	public void test_fallback() {
		check("5", "", 5);
		check("5", "%", 5);
		check("toString", "", OBJECT);
		check("toString", "%", OBJECT);
	}

	public void test_fallback_when_unhandled() {
		final Object subject = OBJECT;
		final FmtAppender delegateAppender = poorAppender.delegateAppender(new FmtKey("qaz", subject.getClass()));
		assertNotNull(delegateAppender);
		final StringBuilder sb = new StringBuilder();
		final FmtContext context = FmtContext.newRichContext(FmtOption.ON_ERROR_VERBOSE, FmtOption.LOCALIZED_NO);
		delegateAppender.appendTo(sb, context, subject);
		assertTrue(sb.toString().contains(FmtError.NO_SUCH_APPENDER.toString()));
	}

	private void check(String expected, String format, Object subject) {
		final FmtAppender delegateAppender = poorAppender.delegateAppender(new FmtKey(format, subject.getClass()));
		assertNotNull(delegateAppender);
		final StringBuilder sb = new StringBuilder();
		delegateAppender.appendTo(sb, null, subject);
		assertEquals(expected, sb.toString());
	}

	private static final FmtMultiAppender poorAppender = FmtMultiAppender.POOR;

	private static final Object OBJECT = new Object() {
		@Override public String toString() {
			return "toString";
		}
	};
}
