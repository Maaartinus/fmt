package de.grajcar.fmt;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Immutable @RequiredArgsConstructor(access=AccessLevel.PACKAGE) final class FmtParserResult {
	enum MissingArgReplacement {MISSING}

	static FmtParserResult get(String format, int start, Class<?> subjectClass, FmtContext context) {
		final int hashCode = (format.hashCode() ^ start) + subjectClass.hashCode();
		final int index = hashCode & (cache.length - 1);
		FmtParserResult result = cache[index];
		if (result!=null && result.matches(start, format, subjectClass, context)) return result;

		result = new FmtParser(format, start, subjectClass, context).result();
		cache[index] = result;
		return result;
	}

	boolean usesArgument() {
		return appender!=null;
	}

	int appendTo(Appendable target, Object subject) throws IOException {
		target.append(prefix);
		if (appender!=null) appender.appendTo(target, context, subject);
		return end;
	}

	@SuppressFBWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ") // For a cache it's alright.
	private boolean matches(int pos, String format, Class<?> subjectClass, FmtContext context) {
		if (pos != this.start) return false;
		if (format != this.format) return false;
		if (subjectClass != this.subjectClass) return false;
		if (context != this.context) return false;
		return true;
	}

	private static final int CACHE_SIZE = 1 << 8;

	/**
	 * A very primitive and fast cache.
	 *
	 * <p>Its thread-safety depends on the immutability of the entries.
	 * While multiple threads may overwrite each others' entries (or help each other),
	 * a matching entry is always correct and its visibility is guaranteed by the final field semantics.
	 * 
	 * <p>This cache is very important as it shortcuts most of the overhead:
	 * <ul>
	 * <li>parsing of the format string
	 * <li>extraction of the substring from between the brackets
	 * <li>creation of the {@link FmtKey}
	 * <li>lookup of the proper appender in {@link FmtContext#appender()}
	 * </ul>
	 */
	private static final FmtParserResult[] cache = new FmtParserResult[CACHE_SIZE];

	private final String format;
	private final int start;
	private final Class<?> subjectClass;
	private final FmtContext context;

	private final String prefix;
	@Nullable private final FmtAppender appender;
	@Getter private final int end;
}
