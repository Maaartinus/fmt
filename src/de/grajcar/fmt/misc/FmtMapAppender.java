package de.grajcar.fmt.misc;

import java.util.Iterator;
import java.util.Map;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

public class FmtMapAppender extends FmtAppender {
	@Override public FmtAppender delegateAppender(FmtKey key) {
		if (!Map.class.isAssignableFrom(key.subjectClass())) return null;
		final String specifier = key.specifier();
		if (!specifier.isEmpty() && !specifier.equals("n")) return null;
		return this;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final Map<?, ?> map = (Map<?, ?>) subject;
		@SuppressWarnings("unchecked")
		final Iterator<Map.Entry<?, ?>> iterator = (Iterator<Map.Entry<?, ?>>) (Object) map.entrySet().iterator();
		if (!iterator.hasNext()) {
			target.append("{}");
			return;
		}
		final FmtAppender delegate = context.appender();
		target.append('{');
		while (true) {
			final Map.Entry<?, ?> e = iterator.next();
			delegate.appendTo(target, context, e.getKey());
			target.append('=');
			delegate.appendTo(target, context, e.getValue());
			if (!iterator.hasNext()) break;
			target.append(", ");
		}
		target.append('}');
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		if (!Map.class.isAssignableFrom(subjectClass)) return "";
		return "n: normal, append the context as usual";
	}
}
