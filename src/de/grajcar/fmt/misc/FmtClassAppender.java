package de.grajcar.fmt.misc;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public final class FmtClassAppender extends FmtAppender {
	public FmtClassAppender() {
		this(ALLOWED_SPECIFIERS.charAt(0));
	}

	@Override @Nullable public FmtAppender delegateAppender(FmtKey key) {
		if (key.subjectClass() != Class.class) return null;
		final String specifierString = key.specifier();
		if (specifierString.isEmpty()) return this;
		if (specifierString.length() > 1) return null;
		final char specifier = specifierString.charAt(0);
		if (specifier == this.specifier) return this;
		if (ALLOWED_SPECIFIERS.indexOf(specifier) == -1) return null;
		return new FmtClassAppender(specifier);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final String s = toString((Class<?>) subject, specifier);
		target.append(s);
	}

	private String toString(Class<?> subject, char c) {
		switch (c) {
			case 'n': return subject.getName();
			case 's': return subject.getSimpleName();
			case 'c': return subject.getCanonicalName();
			case 'l': return localName(subject);
		}
		throw throwBugException(subject);
	}

	private static String localName(Class<?> clazz) {
		final String canonicalName = clazz.getCanonicalName();
		final String fullName = canonicalName!=null ? canonicalName : clazz.getName();
		final Package packagg = clazz.getPackage();
		if (packagg==null) return fullName;
		final String packageName = packagg.getName();
		final String result = fullName.substring(packageName.length() + 1);
		return result;
	}

	private static final String ALLOWED_SPECIFIERS = "lnsc";

	private final char specifier;
}
