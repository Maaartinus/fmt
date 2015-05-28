package de.grajcar.fmt;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for the formatting and possibly also for finding the proper appender.
 *
 * <p>All implementations must be immutable.
 */
@Immutable @RequiredArgsConstructor public abstract class FmtAppender {
	protected enum NullReplacement {
		NULL;

		@Override public String toString() {
			return "null";
		}
	}

	/**
	 * Return an appender which can handle the key
	 * <ul>
	 * <li>{@code this} if this can handle the key
	 * <li>another appender which can handle the key (used e.g., for example for a related format)
	 * <li>{@code null} otherwise
	 */
	@Nullable public abstract FmtAppender delegateAppender(FmtKey key);

	/** Append formatted {@code subject} to {@code target}. */
	public abstract void appendTo(StringBuilder target, FmtContext context, Object subject);

	/**
	 * Return a string describing all formats usable with the given subject.
	 *
	 * <p>The result is meant for helping users.
	 * As the number of such formats is possibly unlimited and possibly hard to describe using an regex,
	 * a simple human-readable form is used.
	 *
	 * @return A human-readable description of accepted format strings, if any, the empty string otherwise.
	 */
	public abstract String helpOnFormatsFor(Class<?> subjectClass);

	protected final RuntimeException throwBugException(Object subject) {
		throw FmtException.throwBugException(subject);
	}
}
