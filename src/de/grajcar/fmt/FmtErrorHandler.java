package de.grajcar.fmt;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Handler for errors like IOException, format errors, and bugs in the appenders.
 * 
 * <p>TODO This class is a mess.
 */
public abstract class FmtErrorHandler {
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	static final class ReportingHandler extends FmtErrorHandler {
		@Override protected String toString(FmtError error, Exception exception, Object... details) {
			return standardToString(isVerbose, error, exception, details);
		}

		@Override protected void handle(Appendable target, String message) throws IOException {
			target.append(message);
		}

		@Override protected void handleDoubleFailure(String message, Exception exception) {
			logDoubleFailure(message, exception);
		}

		static final FmtErrorHandler SUCCINCT_INSTANCE = new ReportingHandler(false);
		static final FmtErrorHandler VERBOSE_INSTANCE = new ReportingHandler(true);

		private final boolean isVerbose;
	}

	static final class LoggingHandler extends FmtErrorHandler {
		@Override protected void handle(Appendable target, String message) throws IOException {
			logger.warning(message);
		}

		@Override protected void handleDoubleFailure(String message, Exception exception) {
			logDoubleFailure(message, exception);
		}

		static final FmtErrorHandler INSTANCE = new LoggingHandler();
	}

	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	static final class ThrowingHandler extends FmtErrorHandler {
		@Override protected void handle(Appendable target, String message) throws IOException {
			FmtException.throwException(message, null);
		}

		@Override protected void handleDoubleFailure(String message, Exception exception) {
			FmtException.throwException(null, exception);
		}

		static final FmtErrorHandler INSTANCE = new ThrowingHandler();
	}

	private static String standardToString(boolean fullStacktrace, FmtError error, @Nullable Exception exception, Object... details) {
		// TODO replace by FmtExceptionAppender
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		printWriter.print("<");
		printWriter.print(error);
		if (details.length > 0) printWriter.print(": " + Arrays.asList(details));
		if (exception!=null) {
			printWriter.print(": ");
			if (fullStacktrace) {
				exception.printStackTrace(printWriter);
			} else {
				final StackTraceElement e = relevantStackTraceElement(exception);
				if (e==null) {
					exception.printStackTrace(printWriter);
				} else {
					printWriter.print(exception);
					printWriter.print(" at ");
					printWriter.print(e);
				}
			}
		}
		printWriter.println(">");
		printWriter.close();
		return stringWriter.toString();
	}

	@Nullable private static StackTraceElement relevantStackTraceElement(Exception exception) {
		//TODO relevantStackTraceElement
		final StackTraceElement[] stackTrace = exception.getStackTrace();
		for (final StackTraceElement e : stackTrace) {
			final String className = e.getClassName();
			if (className.startsWith("de.grajcar.fmt.") && !isDemoOrTest(e.getClassName())) continue;
			if (className.startsWith("de.grajcar.dout.") && !isDemoOrTest(e.getClassName())) continue;
			if (className.startsWith("java.")) continue;
			if (className.startsWith("sun.")) continue;
			if (className.startsWith("com.sun.")) continue;
			if (className.startsWith("com.google.")) continue;
			if (className.startsWith("junit.")) continue;
			if (className.startsWith("org.junit.")) continue;
			if (className.startsWith("org.eclipse.")) continue;
			if (className.startsWith("org.gradle.")) continue;
			return e;
		}
		return null;
	}

	private static boolean isDemoOrTest(String className) {
		if (className.endsWith("Demo")) return true;
		if (className.endsWith("Test")) return true;
		if (className.contains("Demo$")) return true;
		return false;
	}

	private static void logDoubleFailure(String message, Exception exception) {
		logger.severe(standardToString(true, FmtError.DOUBLE_FAILURE, exception, message));
	}

	protected String toString(FmtError error, @Nullable Exception exception, Object... details) {
		return standardToString(false, error, exception, details);
	}

	//TODO change FmtErrorHandler.handle to accept arguments like handleSafely so that exceptions may be rethrown
	protected abstract void handle(Appendable target, String message) throws IOException;

	protected abstract void handleDoubleFailure(String message, Exception exception);

	final void handleSafely(Appendable target, FmtError error, @Nullable Exception exception, Object... details) {
		if (exception instanceof InterruptedException || exception instanceof InterruptedIOException) {
			Thread.currentThread().interrupt();
		}
		if (exception==null) exception = FmtException.makeException(error);
		synchronized (lock) {
			String message;
			try {
				message = toString(error, exception, details);
			} catch (final Exception e) {
				handleDoubleFailure("Cannot convert: " + exception, e);
				return;
			}
			try {
				handle(target, message);
			} catch (final Exception e) {
				handleDoubleFailure(message, e);
			}
		}
	}

	private static final Logger logger = Logger.getLogger(FmtErrorHandler.class.getName());

	private final Object lock = new Object[0];
}
