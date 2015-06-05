package de.grajcar.fmt.primitives;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public final class _FmtByteAppenderTest extends TestCase {
	public void test() {
		check("0", "", 0);
		check("1", "", 1);
		check("123", "", 123);
		check("-22", "", 234);
		check("0", "X", 0);
		check("EA", "X", 234);
		check("FA", "X", 250);
		check("0", "sx", 0);
		check("-6", "sx", 250);
		check("-80", "sx", 128);
		check("01", "px", 1);
		check("13", "px", 19);
		check("5a", "px", 90);
		check("00", "psX", 0);
		check("5A", "psX", 90);
		check("90", "d", 90);
		check("90", "sd", 90);
		check("90", "ud", 90);
		check("-57", "d", 199);
		check("-57", "sd", 199);
		check("199", "ud", 199);
		check("-39", "psx", 199);
		check("0x0", "jX", 0);
		check("0xc7", "jx", 199);
		check("-0x39", "jsx", 199);
		check("0", "j", 0);
		check("-57", "j", 199);
		check("199", "ju", 199);
	}

	public void test_default() {
		check("0", "%", 0);
		check("-100", "%", -100);
		check("0", "", 0);
		check("-100", "", -100);
	}

	public void test_toString() {
		check("-100", "%s", -100);
		check("9c", "%x", -100);
		check("009C", "%04X", -100);
	}

	private void check(String expected, String specifier, int value) {
		assertEquals(expected, context.fmt().add("", specifier, Byte.valueOf((byte) value)).take());
	}

	private static final FmtContext context = FmtContext.richContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
