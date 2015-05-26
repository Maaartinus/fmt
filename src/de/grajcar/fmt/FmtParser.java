package de.grajcar.fmt;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

class FmtParser {
	@RequiredArgsConstructor @Getter static class FormatException extends RuntimeException {
		private final FmtError error;
		private final int pos;
	}

	FmtParser(String format, int start, Class<?> subjectClass, FmtContext context) {
		this.format = format;
		this.start = start;
		opening = findOpening();
		assert opening > -1;
		if (opening==format.length()) {
			closing = 0;
			result = new FmtParserResult(format, start, subjectClass, context, makePrefix(), null, format.length());
		} else {
			closing = findClosing();
			final String specifier = format.substring(opening+1, closing);
			final FmtKey key = new FmtKey(specifier, subjectClass);
			final FmtAppender appender = context.appender().delegateAppender(key);
			checkNotNull(appender);
			result = new FmtParserResult(format, start, subjectClass, context, makePrefix(), appender, closing+1);
		}
	}

	private String makePrefix() {
		if (prefixBuilder!=null) return prefixBuilder.toString();
		if (opening==-1) return format.substring(start);
		return format.substring(start, opening);
	}

	/** Return the position of the opening bracket. */
	private int findOpening() {
		for (int pos=start; ; ++pos) {
			if (pos==format.length()) return pos;
			final char c = format.charAt(pos);
			if (c == '[') return pos;
			if (c == '\'') return findOpening2(pos);
		}
	}

	/** Return the position of the opening bracket in case a quote was found. */
	private int findOpening2(int pos) {
		assert format.charAt(pos) == '\'';
		prefixBuilder = new StringBuilder();
		int unappendedStart = start;
		for (; pos<format.length(); ++pos) {
			final char c = format.charAt(pos);
			if (c == '[') break;
			if (c != '\'') continue;
			if (pos==format.length()) handle(FmtError.TRAILING_QUOTE, pos);
			prefixBuilder.append(format, unappendedStart, pos);
			++pos;
			unappendedStart = pos;
			if (pos == format.length()) {
				handle(FmtError.TRAILING_QUOTE, pos);
			} else if (format.charAt(pos) == '\'') {
				prefixBuilder.append('\'');
			} else {
				pos = format.indexOf('\'', pos+1);
				if (pos==-1) handle(FmtError.UNMATCHED_QUOTE, pos);
				prefixBuilder.append(format, unappendedStart, pos);
			}
			unappendedStart = pos+1;
		}
		prefixBuilder.append(format, unappendedStart, pos);
		return pos;
	}

	/** Return the position of the closing bracket. */
	private int findClosing() {
		assert opening > -1;
		final int result = format.indexOf(']', opening+1);
		if (result==-1) handle(FmtError.UNMATCHED_BRACKET, opening);
		return result;
	}

	private static void handle(FmtError error, int pos) {
		throw new FormatException(error, pos);
	}

	@SuppressWarnings("boxing")	@VisibleForTesting ImmutableList<Object> computedValues() {
		return ImmutableList.<Object>of(makePrefix(), opening, closing);
	}

	private final String format;
	private final int start;

	private final int opening;
	private final int closing;
	private StringBuilder prefixBuilder;
	@Getter private final FmtParserResult result;
}
