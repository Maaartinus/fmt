package de.grajcar.fmt;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

public final class _FmtParserTest extends TestCase {
	public void test_unquoted() {
		check("simple", "simple", 6, 0);
		check("[]", "", 0, 1);
		check("123[]", "123", 3, 4);
		check("123[%]", "123", 3, 5);
		check("123[123]", "123", 3, 7);
	}

	public void test_quotedText() {
		check("'a'", "a", 3, 0);
		check("'ab'", "ab", 4, 0);
	}

	public void test_quotedNoArgs() {
		check("''", "'", 2, 0);
		check("123''", "123'", 5, 0);
		check("''''", "''", 4, 0);
		check("1''2''3", "1'2'3", 7, 0);
		check("1'2'3'4'5", "12345", 9, 0);
		check("1'2'3", "123", 5, 0);
	}

	public void test_quotedBrackets() {
		check("a'[]'b", "a[]b", 6, 0);
		check("a'[]'b'c[]'", "a[]bc[]", 11, 0);
	}

	public void test_quotedArgs() {
		check("''[]]", "'", 2, 3);
		check("'[]'[]", "[]", 4, 5);
	}

	public void test_start() {
		check(2, "[]", "", 2, 0);
		check(2, "[][]", "", 2, 3);
	}

	private void check(String format, String prefix, int opening, int closing) {
		check(0, format, prefix, opening, closing);
	}

	private void check(int start, String format, String prefix, int opening, int closing) {
		@SuppressWarnings("boxing")
		final ImmutableList<Object> expected = ImmutableList.<Object>of(prefix, opening, closing);
		final FmtParser parser = new FmtParser(format, start, subjectClass, context);
		final ImmutableList<Object> actual = parser.computedValues();
		assertEquals(expected, actual);
	}

	private final Class<?> subjectClass = getClass();
	private final FmtContext context = FmtContext.newPoorContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
