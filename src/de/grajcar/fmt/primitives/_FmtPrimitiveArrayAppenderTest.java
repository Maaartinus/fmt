package de.grajcar.fmt.primitives;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

@SuppressWarnings("boxing") public final class _FmtPrimitiveArrayAppenderTest extends TestCase {
	public void test() {
		check("[60, -100]", "", bytes1, bytes2);
		check("[60, -100]", "", shorts1, shorts2);
		check("[60, -100]", "", ints1, ints2);
		check("[60, -100]", "", longs1, longs2);
	}

	private void check(String expected, String specifier, Object subject1, Object subject2) {
		assertEquals(expected, context.fmt().add("", specifier, subject1).take());
		assertEquals(expected, context.fmt().add("", specifier, subject2).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);

	private final byte[] bytes1 = {60, -100};
	private final short[] shorts1 = {60, -100};
	private final int[] ints1 = {60, -100};
	private final long[] longs1 = {60, -100};

	private final Byte[] bytes2 = {60, -100};
	private final Short[] shorts2 = {60, -100};
	private final Integer[] ints2 = {60, -100};
	private final Long[] longs2 = {60L, -100L};
}
