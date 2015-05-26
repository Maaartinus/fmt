package de.grajcar.fmt.primitives;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public final class _FmtIntAppenderTest extends TestCase {
	public void test() {
		check("0", "", 0);
		check("1", "", 1);
		check("123", "", 123);
		check("234", "", 234);
		check("1234567890", "", 1234567890);
		check("-1234567890", "", -1234567890);
		check("1234567890", "sd", 1234567890);
		check("-1234567890", "d", -1234567890);
		check("3060399406", "ud", -1234567890);
		check("0", "X", 0);
		check("EA", "X", 234);
		check("FA", "X", 250);
		check("0", "sx", 0);
		check("-fa", "sx", -250);
		check("80", "sx", 128);
		check("00000013", "px", 19);
		check("ffffed55", "px", -0x12AB);
		check("-000012AB", "psX", -0x12AB);
	}

	public void test_default() {
		check("0", "%", 0);
		check("-100", "%", -100);
		check("0", "", 0);
		check("-100", "", -100);
	}

	public void test_toString() {
		check("-100", "%s", -100);
		check("ffffff9c", "%x", -100);
		check("FFFFFF9C", "%04X", -100);
	}

	private void check(String expected, String specifier, int value) {
		assertEquals(expected, context.fmt().add("", specifier, Integer.valueOf(value)).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
