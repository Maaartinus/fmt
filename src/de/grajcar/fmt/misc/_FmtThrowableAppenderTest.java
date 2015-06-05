package de.grajcar.fmt.misc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public final class _FmtThrowableAppenderTest extends TestCase {
	public void test0() throws Exception {
		try {
			someMethod(2);
		} catch (final Exception e) {
			final String standard = toString(e).replace("\r", "");
			final String normal = context.fmt().add("", "n", e).toString();
			final String legacy = context.fmt().add("", "l", e).toString();

			assertFalse(normal.equals(legacy));
			assertEquals(standard.split("\r?\n").length, legacy.split("\r?\n").length);
			assertEquals(standard, legacy);
			assertEquals(toMultiset(legacy), toMultiset(normal));
		}
	}

	private void someMethod(int count) {
		try {
			someMethod2(count-1);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void someMethod2(int count) throws Exception {
		if (count<0) throw new Exception("testik");
		someMethod(count-1);
	}

	private Multiset<String> toMultiset(String trace) {
		final HashMultiset<String> result = HashMultiset.create();
		for (final String s : trace.split("\r?\n")) result.add(s.replaceFirst("more below$", "more"));
		return result;
	}

	private String toString(Throwable e) throws UnsupportedEncodingException {
		if (e==null) return "null\n";
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final String encoding = "UTF-8";
		final PrintStream ps = new PrintStream(out, false, encoding);
		e.printStackTrace(ps);
		return out.toString(encoding);
	}

	private static final FmtContext context = FmtContext.richContext(FmtOption.ON_ERROR_THROWING);
}
