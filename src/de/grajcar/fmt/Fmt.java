package de.grajcar.fmt;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender.NullReplacement;
import de.grajcar.fmt.FmtParserResult.MissingArgReplacement;

/**
 * A flexible and fast formatter, obtainable from {@link FmtContext}.
 */
@RequiredArgsConstructor(access=AccessLevel.PACKAGE) public final class Fmt {
	@Override public String toString() {
		if (target instanceof StringBuilder) return target.toString();
		return getClass().getSimpleName() + "(" + target.toString() + ")";
	}

	/**
	 * Creates a formatted string using the specified format string and arguments
	 * and returns {@code this} in order to allow fluent syntax.
	 * The result depends on the underlying {@link FmtContext}.
	 *
	 * <p>The format string looks like {@code "some text [format1] text [format2] ..."},
	 * where the formats are interpreted by the {@code FmtAppender}s plugged in.
	 * The exact action depends on both the format and the corresponding argument,
	 * e.g., {@code [x]} may onvoke one {@code FmtAppender} for {@code Integer}
	 * and a different one for {@code double[]}.
	 *
	 * <p>In order to put brackets in the format string, they have to be enclosed in single quotes.
	 * To put a single quote, it needs to be doubled. So to get "{@code [']'}", use "{@code '['']'''}".
	 * An unmatched quote or bracket is an error.
	 */
	public Fmt format(String format, Object... args) {
		try {
			throwingFormat(format, args);
		} catch (final RuntimeException | IOException e) {
			handle(FmtError.EXCEPTION, e);
		}
		return this;
	}

	public Fmt add(String prefix, String specifier, @Nullable Object subject, String suffix) { //TODO use or remove
		try {
			target.append(prefix);
			nullableAdd(specifier, subject);
			target.append(suffix);
			transfer();
		} catch (final RuntimeException | IOException e) {
			handle(FmtError.EXCEPTION, e);
		}
		return this;
	}

	public Fmt add(String prefix, String specifier, @Nullable Object subject) {
		return add(prefix, specifier, subject, "");
	}

	/**
	 * Read and clear the underlying {@code StringBuilder}.
	 * It's an error to call this method when a non-StringBuilder target is used.
	 */
	public String take() {
		checkState(target instanceof StringBuilder, "Only allowed when the target is a StringBuilder.");
		final String result = target.toString();
		final StringBuilder sb = (StringBuilder) target;
		sb.delete(0, sb.length());
		return result;
	}

	private void throwingFormat(String format, Object... args) throws IOException {
		index = 0;
		for (int pos=0; pos<format.length(); ) {
			Object subject = index<args.length ? args[index] : MissingArgReplacement.MISSING;
			if (subject==null) subject = NullReplacement.NULL;
			try {
				final Class<?> subjectClass = subject.getClass();
				final FmtParserResult result = FmtParserResult.get(format, pos, subjectClass, context);
				result.appendTo(target, subject);
				if (result.usesArgument()) ++index;
				pos = result.end();
			} catch (final FmtParser.FormatException e) {
				final Object[] details = { format, "at position " + e.pos() };
				handle(e.error(), e, details);
				break;
			}
		}
		if (index==args.length) {
			target.append(context.afterEach());
		} else if (index<args.length) {
			handle(FmtError.UNUSED_ARGUMENTS, null, Arrays.asList(args).subList(index, args.length).toArray());
		} else {
			final Object[] details = { Integer.valueOf(index - args.length) };
			handle(FmtError.MISSING_ARGUMENT, null, details);
		}
		transfer();
	}

	private void nullableAdd(String specifier, Object subject) throws IOException {
		if (subject==null) subject = FmtAppender.NullReplacement.NULL;
		simpleAdd(specifier, subject);
	}

	private void simpleAdd(String specifier, Object subject) throws IOException {
		final FmtKey key = new FmtKey(specifier, subject.getClass());
		final FmtAppender appender = context.appender().delegateAppender(key);
		appender.appendTo(target, context, subject);
	}

	private void transfer() throws IOException {
		if (realTarget==null) return;
		final StringBuilder sb = (StringBuilder) target;
		realTarget.append(sb.toString());
		sb.delete(0, sb.length());
	}

	private void handle(FmtError error, Exception exception, Object... details) {
		context.errorHandler().handleSafely(target, error, exception, details);
	}

	@Getter private final FmtContext context;
	private final Appendable target;
	@Nullable private final Appendable realTarget;
	private int index;
}
