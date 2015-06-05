package de.grajcar.fmt.misc;

import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import junit.framework.TestCase;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

public class _FmtIteratorAppenderTest extends TestCase {
	public void testEmpty() {
		checkSimple(ImmutableList.of().iterator(), "[]");
		checkFmt(ImmutableList.of().iterator(), "[]");
	}

	public void testOwnIterator() {
		class MyIterator extends UnmodifiableIterator<byte[]> {
			@Override public boolean hasNext() {
				return hasNext;
			}

			@Override public byte[] next() {
				if (!hasNext()) throw new NoSuchElementException();
				hasNext = false;
				return new byte[] {1, 0, -1};
			}

			@Override public String toString() {
				return "Nooo! " + getClass();
			}

			private boolean hasNext = true;
		}
		checkSimple(new MyIterator(), "[[1, 0, -1]]");
		checkFmt(new MyIterator(), "[[1, 0, -1]]");
	}

	@SuppressWarnings("boxing") public void testSimple() {
		checkSimple(ImmutableList.of(1, 2, 3).iterator(), "[1, 2, 3]");
		checkFmt(ImmutableList.of(1, 2, 3).iterator(), "[1, 2, 3]");
	}

	public void testByteArrayList() {
		checkSimple(ImmutableList.of(new byte[] {1, 100, -100}).iterator(), "[[1, 100, -100]]");
		checkFmt(ImmutableList.of(new byte[] {1, 100, -100}).iterator(), "[[1, 100, -100]]");
	}

	private void checkSimple(Object subject, String expected) {
		final StringBuilder sb = new StringBuilder();
		appender.appendTo(sb, context, subject);
		assertEquals(expected, sb.toString());
	}

	private void checkFmt(Object subject, String expected) {
		assertEquals(expected, context.fmt().format("[]", subject).take());
	}

	private static final FmtContext context = FmtContext.richContext(FmtOption.ON_ERROR_THROWING);
	private static final FmtAppender appender = new FmtIteratorAppender();
}
