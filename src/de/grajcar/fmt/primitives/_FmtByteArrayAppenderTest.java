package de.grajcar.fmt.primitives;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public final class _FmtByteArrayAppenderTest extends TestCase {
	public void test() {
		check("[0, 1, 30, 100, -100]", "");
		check("[0, 1, 30, 100, -100]", "d");
		check("[0, 1, 30, 100, -100]", "sd");
		check("[0, 1, 30, 100, 156]", "ud");
		check("[0, 1, 1e, 64, 9c]", "x");
		check("[0, 1, 1e, 64, 9c]", "ux");
		check("[0, 1, 1e, 64, -64]", "sx");
		check("[0, 1, 1E, 64, 9C]", "X");
		check("[0, 1, 1E, 64, 9C]", "uX");
		check("[0, 1, 1E, 64, -64]", "sX");
		check("[0x0, 0x1, 0x1e, 0x64, 0x9c]", "jx");
		check("[0x0, 0x1, 0x1E, 0x64, 0x9C]", "jX");
		check("00011e649c", "px");
		check("00011E649C", "pX");
	}

	public void test_wrapper() {
		check("[0, -1]", "", new Byte[] {Byte.valueOf((byte) 0), Byte.valueOf((byte) -1)});
		check("[0, FF]", "X", new Byte[] {Byte.valueOf((byte) 0), Byte.valueOf((byte) -1)});
		check("00ff", "px", new Byte[] {Byte.valueOf((byte) 0), Byte.valueOf((byte) -1)});
	}

	private void check(String expected, String specifier) {
		check(expected, specifier, bytes);
	}

	private void check(String expected, String specifier, Object subject) {
		assertEquals(expected, context.fmt().add("", specifier, subject).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);

	private final byte[] bytes = {0, 1, 30, 100, -100};
}
