package de.grajcar.fmt.primitives;

import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
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

	public void test_separated_hex() {
		check("12345678", "_x", 0x12345678);
		check("1_23456789", "_x", 0x123456789L);
		check("1234_5678", "__x", 0x12345678);
		check("1_2345_6789", "__x", 0x123456789L);
	}

	public void test_separated_hex_random() {
		final CharMatcher matcher = CharMatcher.is('_');
		final Random random = new Random(51132);
		for(int i=0; i<1000; ++i) {
			final boolean signed = (i&1) != 0;
			final String signedModifier = signed ? "s" : "";
			final long value = random.nextLong();
			final String expected = signed ? Long.toString(value, 16) : String.format("%x", value);
			final String separated0 = context.fmt().add("", "x" + signedModifier, value).take();
			final String separated1 = context.fmt().add("", "_x" + signedModifier, value).take();
			final String separated2 = context.fmt().add("", "__x" + signedModifier, value).take();
			assertEquals(expected, separated0);
			assertEquals(expected, matcher.removeFrom(separated1));
			assertEquals(expected, matcher.removeFrom(separated2));
			assertTrue(HEX1_PATTERN.matcher(separated1).matches());
			assertTrue(HEX2_PATTERN.matcher(separated2).matches());
		}
	}

	public void test_separated_decimal() {
		check("1234", "_", 1234);
		check("12_345678", "_", 12345678);
		check("1_234", "__", 1234);
		check("12_345_678", "__", 12345678);
		check("4_793971_110569_000604", "_", 4793971110569000604L);
	}

	public void test_separated_decimal_huge() {
		check("1_234567_890123_456789", "_", 1234567890123456789L);
		check("9_223372_036854_775807", "_", Long.MAX_VALUE);
		check("-9_223372_036854_775808", "_", Long.MIN_VALUE);
		check("9_223372_036854_775808", "_u", Long.MIN_VALUE);
		check("18_446744_073709_551615", "_u", -1);
	}

	public void test_separated_decimal_random() {
		final CharMatcher matcher = CharMatcher.is('_');
		final Random random = new Random(0);
		for(int i=0; i<1000; ++i) {
			final long value = random.nextLong();
			final String expected = Long.toString(value);
			final String separated0 = context.fmt().add("", "", value).take();
			final String separated1 = context.fmt().add("", "_", value).take();
			final String separated2 = context.fmt().add("", "__", value).take();
			assertEquals(expected, separated0);
			assertEquals(expected, matcher.removeFrom(separated1));
			assertEquals(expected, matcher.removeFrom(separated2));
			assertTrue(MILLIONS_PATTERN.matcher(separated1).matches());
			assertTrue(THOUSANDS_PATTERN.matcher(separated2).matches());
		}
	}

	private void check(String expected, String specifier, long value) {
		assertEquals(expected, context.fmt().add("", specifier, value).take());
	}

	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
	private static final Pattern MILLIONS_PATTERN = Pattern.compile("-?\\d{1,6}(_\\d{6})*");
	private static final Pattern THOUSANDS_PATTERN = Pattern.compile("-?\\d{1,3}(_\\d{3})*");
	private static final Pattern HEX1_PATTERN = Pattern.compile("-?\\w{1,8}(_\\w{8})*");
	private static final Pattern HEX2_PATTERN = Pattern.compile("-?\\w{1,4}(_\\w{4})*");
}
