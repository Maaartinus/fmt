package de.grajcar.fmt.misc;

import java.util.Date;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public final class FmtDateAppender extends FmtAppender {
	public FmtDateAppender() {
		this("yy-MM-dd'T'HH:mm:ss");
	}

	@Nullable @Override public FmtAppender delegateAppender(FmtKey key) {
		if (!Date.class.isAssignableFrom(key.subjectClass())) return null;
		final String specifier = key.specifier();
		if (specifier.isEmpty()) return this;
		if (!specifier.startsWith(SDF_PREFIX)) return null;
		final String sdfPattern = specifier.substring(SDF_PREFIX.length());
		if (sdfPattern.equals(this.sdfPattern)) return this;
		return new FmtDateAppender(sdfPattern);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final String cs = DateHelper.format(sdfPattern, context.locale(), context.timeZone(), (Date) subject);
		target.append(cs);
	}

	private static final String SDF_PREFIX = "sdf%";

	private final String sdfPattern;
}
