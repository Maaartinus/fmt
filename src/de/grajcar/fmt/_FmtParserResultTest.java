package de.grajcar.fmt;

import junit.framework.TestCase;

public final class _FmtParserResultTest extends TestCase {
	public void test() {
		check("[]", 0, "[1, -2]");
		check("[]", 2, "");
		check("[][]", 2, "[1, -2]");
		check("[jx]", 0, "[0x1, 0xfe]");
		check("[pX]", 0, "01FE");
	}

	private void check(String format, int start, String expected) {
		final String[] strings = new String[2];
		final FmtParserResult[] parserResults = new FmtParserResult[2];
		for (int i=0; i<2; ++i) {
			final StringBuilder sb = new StringBuilder();
			final Class<?> subjectClass = subject.getClass();
			final FmtParserResult result = FmtParserResult.get(format, start, subjectClass, context);
			result.appendTo(sb, subject);
			parserResults[i] = result;
			strings[i] = sb.toString();
		}
		assertSame(parserResults[0], parserResults[1]); // caching
		assertEquals(strings[0], strings[1]);
		assertEquals(expected, strings[0]);
	}

	private final Object subject = new byte[] {1, -2};
	private static final FmtContext context = FmtContext.richContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
