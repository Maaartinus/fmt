package de.grajcar.fmt.misc;

import java.util.Iterator;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

public class FmtIteratorAppender extends FmtAppender {
	@Override public FmtAppender delegateAppender(FmtKey key) {
		if (!Iterator.class.isAssignableFrom(key.subjectClass())) return null;
		if (!key.specifier().equals("n")) return null;
		return this;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final Iterator<?> iterator = (Iterator<?>) subject;
		if (!iterator.hasNext()) {
			target.append("[]");
			return;
		}
		final FmtAppender delegate = context.appender();
		target.append('[');
		while (true) {
			delegate.appendTo(target, context, iterator.next());
			if (!iterator.hasNext()) break;
			target.append(", ");
		}
		target.append(']');
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		if (!Iterator.class.isAssignableFrom(subjectClass)) return "";
		return "n: normal, append the context as if it was a list";
	}
}
