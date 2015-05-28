package de.grajcar.fmt.misc;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public final class FmtCharSequenceAppender extends FmtAppender {
	public FmtCharSequenceAppender() {
		this(DEFAULT_SPECIFIER);
	}

	@Override public FmtAppender delegateAppender(FmtKey key) {
		if (!CharSequence.class.isAssignableFrom(key.subjectClass())) return null;
		final String specifierString = key.specifier();
		if (specifierString.length() > 1) return null;
		final char specifier = specifierString.isEmpty() ? DEFAULT_SPECIFIER : specifierString.charAt(0);
		if (specifier == this.specifier) return this;
		if (ALLOWED_SPECIFIERS.indexOf(specifier) == -1) return null;
		return new FmtCharSequenceAppender(specifier);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		if (specifier=='j') {
			appendToWithJavaSyntax(target, subject);
		} else {
			target.append(subject);
		}
	}

	private void appendToWithJavaSyntax(StringBuilder target, Object subject) {
		final String s = subject.toString();
		target.append('"');
		for (int i=0; i<s.length(); ++i) {
			final char c = s.charAt(i);
			final String escape = escape(c);
			if (escape==null) {
				target.append(c);
			} else {
				target.append(escape);
			}
		}
		target.append('"');
	}

	private String escape(char c) {
		switch (c) {
			case '"': return "\\\"";
			case '\\': return "\\\\";
			case '\n': return "\\n";
			case '\r': return "\\r";
		}
		return null;
	}

	private static final String ALLOWED_SPECIFIERS = "nj";
	private static final char DEFAULT_SPECIFIER = ALLOWED_SPECIFIERS.charAt(0);

	private final char specifier;
}
