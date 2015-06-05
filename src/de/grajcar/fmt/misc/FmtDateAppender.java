package de.grajcar.fmt.misc;

import java.util.Date;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) @Wither(AccessLevel.PRIVATE)
public final class FmtDateAppender extends FmtAppender {
	public FmtDateAppender() {
		this(DEFAULT_SPECIFIER, Date.class);
	}

	@Nullable @Override public FmtAppender delegateAppender(FmtKey key) {
		if (!Date.class.isAssignableFrom(subjectClass)) return null;
		final Class<?> subjectClass = key.subjectClass();
		String specifier = key.specifier();
		if (specifier.indexOf('%') != -1) return null;
		if (specifier.isEmpty()) {
			if (subjectClass == this.subjectClass) return this;
			specifier = defaultSpecifierFor(subjectClass);
		}
		return withSpecifier(specifier).withSubjectClass(subjectClass);
	}

	private String defaultSpecifierFor(Class<?> subjectClass) {
		if (subjectClass.equals(Date.class)) return DEFAULT_SPECIFIER;
		if (subjectClass.equals(java.sql.Date.class)) return "yy-MM-dd";
		if (subjectClass.equals(java.sql.Time.class)) return "HH:mm:ss";
		return DEFAULT_SPECIFIER;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final String cs = DateHelper.format(specifier, context.locale(), context.timeZone(), (Date) subject);
		target.append(cs);
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		if (!Date.class.isAssignableFrom(subjectClass)) return "";
		return "xxx, where xxx is defined by java.text.SimpleDateFormat.";
	}

	private static final String DEFAULT_SPECIFIER = "yy-MM-dd'T'HH:mm:ss";

	private final String specifier;
	private final Class<?> subjectClass;
}
