package de.grajcar.fmt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import junit.framework.TestCase;

@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE") @SuppressWarnings("boxing")
public final class _FmtTest extends TestCase {
	private static class ThrowingAppendable implements Appendable {
		@Override public Appendable append(CharSequence csq) throws IOException {
			throw new IOException(SOME_MESSAGE);
		}

		@Override public Appendable append(CharSequence csq, int start, int end) throws IOException {
			throw new IOException(SOME_MESSAGE);
		}

		@Override public Appendable append(char c) throws IOException {
			throw new IOException(SOME_MESSAGE);
		}
	}

	private static class TestErrorHandler extends FmtErrorHandler {
		@Override public String toString() {
			return errorLogBuilder.toString();
		}

		@Override protected void handle(Appendable target, String message) throws IOException {
			errorLogBuilder.append(message);
		}

		@Override protected void handleDoubleFailure(String message, Exception exception) {
			fail();
		}

		private final StringBuilder errorLogBuilder = new StringBuilder();
	}

	public void testFmt_throwing() {
		final TestErrorHandler testErrorHandler = new TestErrorHandler();
		final FmtContext context = FmtContext.newPoorContext(FmtOption.LOCALIZED_NO).withErrorHandler(testErrorHandler);
		fmt = context.fmt(new ThrowingAppendable());
		fmt.format("a");
		final String message = testErrorHandler.toString();
		assertTrue(message, message.contains(SOME_MESSAGE));
		assertTrue(message, message.contains(IOException.class.getName().toString()));
		assertTrue(message, message.contains(FmtError.EXCEPTION.toString()));
		assertTrue(message, message.startsWith("<" + FmtError.EXCEPTION.toString()));
		assertTrue(message, message.endsWith(">\n"));
	}

	public void testNullFormat() {
		fmt.format(null);
		check("", FmtError.EXCEPTION, "", NullPointerException.class, null);
	}

	public void testNullArgs() {
		check("null", FmtError.UNUSED_ARGUMENTS, "", FmtException.class, "[]", new Object[] {null, null});
	}

	public void testNullArgArray() {
		check("", FmtError.EXCEPTION, "", NullPointerException.class, "[]", (Object[]) null);
	}

	public void testFormat_empty() {
		final Object[] args = {};
		check("", "", args);
	}

	public void testFormat() {
		check("quote: '", "quote: ''");
		check("quoted brackets: []", "quoted brackets: '[]'");
	}

	public void testFormat_missingArgument() {
		check("missing: MISSING", FmtError.MISSING_ARGUMENT, "", FmtException.class,
				"missing: []");
		check("first MISSING and second MISSING", FmtError.MISSING_ARGUMENT, "", FmtException.class,
				"first [] and second [] argument");
		check("first argument present, but second MISSING", FmtError.MISSING_ARGUMENT, "", FmtException.class,
				"first argument [], but second []", new Object[] { "present" });
	}

	public void testFormat_unusedArgument() {
		check("unused<UNUSED_ARGUMENTS: [superfluous]", FmtError.UNUSED_ARGUMENTS, "", FmtException.class,
				"unused", "superfluous");
		check("one used and one<UNUSED_ARGUMENTS: [superfluous]", FmtError.UNUSED_ARGUMENTS, "", FmtException.class,
				"one [] and one", "used", "superfluous");
	}

	public void testFormat_invalid() {
		check("<UNMATCHED_BRACKET: [[, at position 0]", FmtError.UNMATCHED_BRACKET, "", FmtParser.FormatException.class,
				"[");
		check("<TRAILING_QUOTE: [', at position 1", FmtError.TRAILING_QUOTE, "", FmtParser.FormatException.class,
				"'");
		check("<UNMATCHED_QUOTE: [1'[, at position -1]", FmtError.UNMATCHED_QUOTE, "", FmtParser.FormatException.class,
				"1'[");
	}

	public void testAdd() {
		assertEquals("anullb", fmt.add("a", "", null, "b").take());
		assertEquals("a-123b", fmt.add("a", "", (byte) -123, "b").take());
		assertEquals("a0", fmt.add("a", "", (byte) 0).take());
	}

	public void test_poorFmt() {
		check(SQLException.class.getName(), "[%]", new SQLException());
		check(SQLException.class.getName(), "[]", new SQLException());
	}

	public void test_richFmt() {
		fmt = richFmt;
		check(SQLException.class.getName(), "[%]", new SQLException());

		final String actual = fmt.format("[]", new SQLException()).toString();
		assertTrue(actual, actual.contains(getClass().getName()));
		assertTrue(actual, actual.contains("\n"));
		assertTrue(actual, actual.contains(SQLException.class.getName()));
	}

	private void check(String expected, String format, Object... args) {
		for (int i=0; i<2; ++i) {
			final String actual = fmt.format(format, args).take();
			if (expected.equals(actual)) continue;
			final String s = "Format \"%s\" and arguments %s\n";
			final String msg = String.format(s, format, args==null ? "null" : Arrays.asList(args));
			assertEquals(msg, expected, actual);
		}
	}

	private void check(String expectedStart, FmtError expectedError, String expectedEnd,
			Class<? extends Exception> expectedExceptionClass,
			String format, Object... args) {
		for (int i=0; i<2; ++i) {
			final String actual = fmt.format(format, args).take();
			final List<String> msg = Lists.newArrayList();
			if (!actual.startsWith(expectedStart)) msg.add("- doesn't start with \"" + expectedStart + "\"");
			if (!actual.endsWith(expectedEnd)) msg.add("- doesn't end with \"" + expectedEnd + "\"");
			if (!actual.contains(expectedError.toString())) msg.add("- doesn't contain \"" + expectedError + "\"");
			final String exceptionClassName = expectedExceptionClass.getName();
			if (!actual.contains(exceptionClassName)) msg.add("- doesn't contain \"" + exceptionClassName + "\"");
			if (!actual.contains(getClass().getName())) msg.add("- doesn't contain \"" + getClass().getName() + "\"");
			if (msg.isEmpty()) continue;
			final String s = "Errors for format \"%s\" and arguments %s";
			msg.add(0, String.format(s, format, args==null ? "null" : Arrays.asList(args)));
			msg.add("Actual: " + actual);
			fail(Joiner.on("\n").join(msg));
		}
	}

	private static final String SOME_MESSAGE = "someMessage";

	private final FmtContext context =
			FmtContext.newPoorContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_VERBOSE);
	private final Fmt poorFmt = context.fmt();
	private final Fmt richFmt = context.withPrependedAppenders(FmtLoadingAppender.INSTANCE).fmt();

	private Fmt fmt = poorFmt;
}
