package de.grajcar.fmt.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public final class FmtExceptionAppender extends FmtAppender {
	public FmtExceptionAppender() {
		this(ALLOWED_SPECIFIERS.charAt(0));
	}

	@Override @Nullable public FmtAppender delegateAppender(FmtKey key) {
		if (!Throwable.class.isAssignableFrom(key.subjectClass())) return null;
		final String specifierString = key.specifier();
		if (specifierString.length() > 1) return null;
		if (specifierString.isEmpty()) return this;
		final char specifier = specifierString.charAt(0);
		if (specifier == this.specifier) return this;
		if (ALLOWED_SPECIFIERS.indexOf(specifier) == -1) return null;
		return new FmtExceptionAppender(specifier);
	}

	@Override public void appendTo(Appendable target, FmtContext context, Object subject) throws IOException {
		appendTo(target, (Throwable) subject);
	}

	private void appendTo(Appendable target, Throwable e) throws IOException {
		final List<Throwable> list = new ArrayList<Throwable>();
		for (Throwable e1=e; e1!=null; e1=e1.getCause()) list.add(e1);
		appendTo(target, list);
	}

	private void appendTo(Appendable target, List<Throwable> list) throws IOException {
		final List<StackTraceElement[]> traces = new ArrayList<StackTraceElement[]>(list.size()+2);
		for (final Throwable e : list) traces.add(e.getStackTrace());
		for (int i=0; i<list.size(); ++i) {
			if (i!=0) target.append("Caused by: ");
			target.append("" + list.get(i)).append('\n');
			final StackTraceElement[] otherTrace = getOtherTrace(i, traces);
			appendTo(target, traces.get(i), otherTrace);
		}
	}

	@Nullable private StackTraceElement[] getOtherTrace(int index, List<StackTraceElement[]> traces) {
		if (isLegacy()) {
			return index==0 ? null : traces.get(index-1);
		} else {
			return index+1 < traces.size() ? traces.get(index+1) : null;
		}
	}

	private void appendTo(Appendable target, StackTraceElement[] trace, @Nullable StackTraceElement[] otherTrace) throws IOException {
		final int commonLength = commonLength(trace, otherTrace);
		for (int i=0; i<trace.length-commonLength; ++i) {
			target.append("\tat ").append(trace[i].toString()).append('\n');
		}
		if (commonLength==0) return;
		target.append("\t... ").append(String.valueOf(commonLength)).append(" more").append(isLegacy() ? "" : " below").append('\n');
	}

	private int commonLength(StackTraceElement[] trace, @Nullable StackTraceElement[] otherTrace) {
		if (otherTrace==null) return 0;
		for (int m=trace.length-1, n=otherTrace.length-1; true; --m, --n) {
			if (m<0 || n<0 || !trace[m].equals(otherTrace[n])) return trace.length - 1 - m;
		}
	}

	private boolean isLegacy() {
		return specifier == 'l';
	}

	private static final String ALLOWED_SPECIFIERS = "nl";

	private final char specifier;
}