package de.grajcar.fmt;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import org.slf4j.Logger;

@RequiredArgsConstructor public final class FmtLogger implements Logger {//TODO complete
	public void fmtTrace(String format, Object subject) {
		if (isTraceEnabled()) trace(context.fmt().format(format, subject).toString());
	}

	public void fmtTrace(String format, Object subject1, Object subject2) {
		if (isTraceEnabled()) trace(context.fmt().format(format, subject1, subject2).toString());
	}

	public void fmtTrace(String format, Object... subjects) {
		if (isTraceEnabled()) trace(context.fmt().format(format, subjects).toString());
	}

	private final FmtContext context;
	@Delegate private final Logger logger;
}
