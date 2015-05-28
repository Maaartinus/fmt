package de.grajcar.fmt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import de.grajcar.fmt.FmtAppender.NullReplacement;



/**
 * The context (and also the factory) for {@link Fmt} instances.
 * You usually want to have just one static instance instance of it per project.
 */
@Immutable @Value @Wither public final class FmtContext {
	@RequiredArgsConstructor private static class FmtRestrictedAppender extends FmtAppender {
		@Override public FmtAppender delegateAppender(FmtKey key) {
			if (!matches(key.subjectClass())) return null;
			return delegateAppender.delegateAppender(key);
		}

		@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
			delegateAppender.appendTo(target, context, subject);
		}

		@Override public String helpOnFormatsFor(Class<?> subjectClass) {
			if (!matches(subjectClass)) return "";
			return delegateAppender.helpOnFormatsFor(subjectClass);
		}

		private boolean matches(Class<?> subjectClass) {
			if (subjectClass==this.subjectClass) return true;
			if (!acceptSubclasses) return false;
			return this.subjectClass.isAssignableFrom(subjectClass);
		}

		@NonNull private final Class<?> subjectClass;
		private final boolean acceptSubclasses;
		@NonNull private final FmtAppender delegateAppender;
	}

	/**
	 * Return a new context without any predefined appenders.
	 * You usually want to use {@link #newRichContext(FmtOption...)} instead.
	 */
	public static FmtContext newPoorContext(FmtOption... options) {
		FmtContext result = ROOT;
		for (final FmtOption o : options) result = o.apply(result);
		return result;
	}

	/** Return a new context with the standard set of predefined appenders. */
	public static FmtContext newRichContext(FmtOption... options) {
		FmtContext result = ROOT.withPrependedAppenders(FmtLoadingAppender.INSTANCE);
		for (final FmtOption o : options) result = o.apply(result);
		return result;
	}

	/** Return a new {@link Fmt} writing to the specified target appendable. */
	public Fmt fmt(Appendable target) {
		return new Fmt(this, new StringBuilder(), target);
	}

	/**
	 * Return a new {@link Fmt} writing to a new {@code StringBuilder}.
	 * The produced formatted String can be obtained via {@link Fmt#take()} or {@link Fmt#toString()}.
	 */
	public Fmt fmt() {
		final StringBuilder target = new StringBuilder();
		return new Fmt(this, target, target);
	}

	/** Return a new context by applying the given options in order. */
	public FmtContext withOption(FmtOption option) {
		return option.apply(this);
	}

	/**
	 * Return a new context which defines the formatting for instances of {@code subjectClass}
	 * (possibly including subclasses) for the default formatting.
	 * For example, in the result of the call<br>
	 * {@code prefer("x", Integer.class, false).fmt().format("[]", 43).toString()}<br>
	 * is equivalent to<br>
	 * {@code fmt().format("[x]", 43).toString()}
	 */
	public FmtContext prefer(String specifier, Class<?> subjectClass, boolean acceptSubclasses) {
		final FmtKey key = new FmtKey(specifier, subjectClass);
		final FmtAppender delegateAppender = appender().delegateAppender(key);
		checkNotNull(delegateAppender);
		final boolean ok = !(delegateAppender instanceof FmtMultiAppender.FmtFallbackAppender);
		checkArgument(ok, "Specifier \"%s\" doesn't work with %s a", specifier, subjectClass);
		return withPrependedAppenders(new FmtRestrictedAppender(subjectClass, acceptSubclasses, delegateAppender));
	}

	/** Return a new context in which the given appenders are tried first. */
	public FmtContext withPrependedAppenders(FmtAppender... appenders) {//TODO ???
		return withPrependedAppenders(Arrays.asList(appenders));
	}

	/** Return a new context in which the given appenders are tried first. */
	public FmtContext withPrependedAppenders(List<FmtAppender> appenders) {
		for (final FmtAppender a : appenders) checkNotNull(a);
		return withAppender(appender.withPrepended(appenders));
	}

	public String stringify(@Nullable Object subject) {
		if (subject==null) subject = NullReplacement.NULL;
		final StringBuilder result = new StringBuilder();
		try {
			final FmtAppender delegateAppender = appender.delegateAppender(new FmtKey("", subject.getClass()));
			delegateAppender.appendTo(result, this, subject);
		} catch (final RuntimeException e) {
			errorHandler.handleSafely(result, FmtError.EXCEPTION, e);
		}
		return result.toString();
	}

	//TODO use or remove FmtContext supplierMap, which may be possibly useful for FmtLogger
	private static final ImmutableMap<String, Supplier<? extends Object>> DEFAULT_SUPPLIER_MAP = ImmutableMap.of(
			"th", new Supplier<Thread>() {
				@Override public Thread get() {
					return Thread.currentThread();
				}
			},
			"dt", new Supplier<Date>() {
				@Override public Date get() {
					return new Date();
				}
			});

	private static final FmtContext ROOT = new FmtContext(
			FmtMultiAppender.POOR,
			DEFAULT_SUPPLIER_MAP,
			FmtErrorHandler.ThrowingHandler.INSTANCE,
			Locale.ENGLISH,
			TimeZone.getDefault(),
			"");

	@Wither(AccessLevel.PRIVATE) @NonNull private final FmtMultiAppender appender;
	@Wither(AccessLevel.NONE) @NonNull private final ImmutableMap<String, Supplier<? extends Object>> supplierMap;
	@Wither(AccessLevel.PACKAGE) @NonNull private final FmtErrorHandler errorHandler;
	@NonNull private final Locale locale;
	@NonNull private final TimeZone timeZone;
	@NonNull private final String afterEach;
}
