package de.grajcar.fmt.misc;

import java.util.Iterator;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

public class FmtIterableAppender extends FmtAppender {
	@Override public FmtAppender delegateAppender(FmtKey key) {
		if (!Iterable.class.isAssignableFrom(key.subjectClass())) return null;
		if (!key.specifier().equals("n")) return null;
		return this;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		delegateAppender.appendTo(target, context, ((Iterable<?>) subject).iterator());
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		if (!Iterable.class.isAssignableFrom(subjectClass)) return "";
		return delegateAppender.helpOnFormatsFor(Iterator.class);
	}

	private static final FmtIteratorAppender delegateAppender = new FmtIteratorAppender();
}
