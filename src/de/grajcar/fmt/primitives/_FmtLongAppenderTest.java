package de.grajcar.fmt.primitives;

import java.math.BigInteger;
import java.util.Random;

import com.google.common.math.LongMath;
import com.google.common.primitives.UnsignedLongs;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

@SuppressWarnings("boxing") public final class _FmtLongAppenderTest extends TestCase {
	public void test() {
		check("0", "", 0);
		check("0", "u", 0);
		check("0", "s", 0);
		check("1", "", 1);
		check("1", "u", 1);
		check("1", "s", 1);
		check("-1", "", -1);
		check(BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).toString(), "u", -1);
		check("-1", "s", -1);
		check("123", "", 123);
		check("234", "", 234);
		check("1234567890", "", 1234567890);
		check("-1234567890", "", -1234567890);
		check("1234567890", "sd", 1234567890);
		check("-1234567890", "d", -1234567890);
		check("12000000000000000000", "ud", 3*4000000000000000000L);
		check("0xFAFAFAFACDCDEDED", "juX", 0xFAFAFAFACDCDEDEDL);
		check("8000000000000000", "uX", Long.MIN_VALUE);
		check("-8000000000000000", "sX", Long.MIN_VALUE);
		check("7FFFFFFFEECCAA88", "sX", Long.MAX_VALUE - 0x11335577);
		check("0", "X", 0);
		check("EA", "X", 234);
		check("FA", "X", 250);
		check("0", "sx", 0);
		check("-fa", "sx", -250);
		check("80", "sx", 128);
	}

	public void test_big() {
		check("" + Long.MIN_VALUE, "s", Long.MIN_VALUE);
		check("" + Long.MAX_VALUE, "s", Long.MAX_VALUE);
		check(UnsignedLongs.toString(Long.MIN_VALUE), "u", Long.MIN_VALUE);
		check("" + Long.MAX_VALUE, "u", Long.MAX_VALUE);
		for (int i=1; i<=18; ++i) {
			final long n = LongMath.pow(10, i);
			check(Long.toString(n), "", n);
			check(Long.toString(9*n), "", 9*n);
			check(Long.toString(-n), "", -n);
			check(Long.toString(-9*n), "", -9*n);
			check(UnsignedLongs.toString(-n), "u", -n);
			check(UnsignedLongs.toString(-9*n), "u", -9*n);
		}
	}

	public void test_random() {
		final Random random = new Random(0);
		for (int i=0; i<1000; ++i) {
			final long n = random.nextLong();
			check(Long.toString(n), "", n);
			check(Long.toString(n), "s", n);
			check(UnsignedLongs.toString(n), "u", n);
		}
	}

	public void test_default() {
		check("0", "%", 0);
		check("-100", "%", -100);
		check("0", "", 0);
		check("-100", "", -100);
	}

	public void test_toString() {
		check("-100", "%s", -100);
		check("ffffffffffffff9c", "%x", -100);
		check("FFFFFFFFFFFFFF9C", "%04X", -100);
	}

	private void check(String expected, String specifier, long value) {
		assertEquals(expected, context.fmt().add("", specifier, value).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
