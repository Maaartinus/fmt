package de.grajcar.fmt.misc;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public final class FmtClassAppender extends FmtAppender {
	public FmtClassAppender() {
		this(DEFAULT_SPECIFIER);
	}

	@Override @Nullable public FmtAppender delegateAppender(FmtKey key) {
		if (key.subjectClass() != Class.class) return null;
		final String specifierString = key.specifier();
		if (specifierString.length() > 1) return null;
		final char specifier = specifierString.isEmpty() ? DEFAULT_SPECIFIER : specifierString.charAt(0);
		if (specifier == this.specifier) return this;
		if (ALLOWED_SPECIFIERS.indexOf(specifier) == -1) return null;
		return new FmtClassAppender(specifier);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final String s = toString((Class<?>) subject, specifier);
		target.append(s);
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		if (!CharSequence.class.isAssignableFrom(subjectClass)) return "";
		return ""
		+ "n: normal, i.e., using getName()"
		+ "\n"
		+ "s: simple, i.e., using getSimpleName()"
		+ "\n"
		+ "c: canonical, i.e., using getCanonicalName()"
		+ "\n"
		+ "l: local, i.e., using the getName() stripped of the package name";
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
	private static final char DEFAULT_SPECIFIER = ALLOWED_SPECIFIERS.charAt(0);

	private final char specifier;
}
