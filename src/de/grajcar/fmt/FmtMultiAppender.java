package de.grajcar.fmt;

import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This class is the engine reponsible for quickly finding and caching the proper appender for the given {@link FmtKey}.
 *
 * <p>While this class contains a mutable cache, it's effectively immutable and thread-safe.
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) @EqualsAndHashCode(of="appenders", callSuper=false)
final class FmtMultiAppender extends FmtAppender {
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE) static final class FmtFallbackAppender extends FmtAppender {
		@Override public FmtAppender delegateAppender(FmtKey key) {
			return this;
		}

		@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
			final Object[] details = {
					"specifier=" + key.specifier(),
					"subjectClass=" + key.subjectClass().getName(),
					"subject=" + subject,
			};
			context.errorHandler().handleSafely(target, FmtError.NO_SUCH_APPENDER, null, details);
		}

		@Override public String helpOnFormatsFor(Class<?> subjectClass) {
			return "";
		}

		private final FmtKey key;
	}

	@RequiredArgsConstructor(access=AccessLevel.PRIVATE) private static class FmtToStringAppender extends FmtAppender {
		@Override public FmtAppender delegateAppender(FmtKey key) {
			return key.specifier().equals("%") ? this : null;
		}

		@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
			target.append(subject.toString());
		}

		@Override public String helpOnFormatsFor(Class<?> subjectClass) {
			return "%, formats the subject using its toString";
		}

		private static final FmtAppender INSTANCE = new FmtToStringAppender();
	}

	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private static final class FmtStringFormatAppender extends FmtAppender {
		@Override public FmtAppender delegateAppender(FmtKey key) {
			final String specifier = key.specifier();
			if (specifier.length() < 2) return null;
			if (specifier.charAt(0) != '%') return null;
			if (specifier.length() == pattern.length() && specifier.endsWith(pattern)) return this;
			return new FmtStringFormatAppender(specifier);
		}

		@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
			target.append(String.format(context.locale(), pattern, subject));
		}

		@Override public String helpOnFormatsFor(Class<?> subjectClass) {
			return "%xxx, where xxx is a format specifier of String.format";
		}

		private static final FmtAppender INSTANCE = new FmtStringFormatAppender("%s");

		private final String pattern;
	}

	@Override public FmtAppender delegateAppender(FmtKey key) {
		return cache.getUnchecked(key);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		delegateAppender(new FmtKey("", subject.getClass())).appendTo(target, context, subject);
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		final List<String> result = Lists.newArrayList();
		for (final FmtAppender a : appenders) {
			final String s = a.helpOnFormatsFor(subjectClass);
			if (!s.isEmpty()) result.add(s);
		}
		return Joiner.on("\n\n").join(result);
	}

	FmtMultiAppender withPrepended(FmtAppender... appenders) {
		return withPrepended(Arrays.asList(appenders));
	}

	FmtMultiAppender withPrepended(List<? extends FmtAppender> appenders) {
		if (appenders.isEmpty()) return this;
		final ImmutableList.Builder<FmtAppender> builder = ImmutableList.builder();
		for (final FmtAppender a : Iterables.concat(appenders, this.appenders)) {
			if (a instanceof FmtMultiAppender) {
				builder.addAll(((FmtMultiAppender) a).appenders);
			} else {
				builder.add(a);
			}
		}
		final ImmutableList<FmtAppender> list = builder.build();
		return interner.intern(new FmtMultiAppender(list));
	}

	private FmtAppender load(FmtKey key) {
		for (final FmtAppender a : appenders) {
			if (a instanceof FmtLoadingAppender) {
				final FmtLoadingAppender loadingAppender = (FmtLoadingAppender) a;
				final ImmutableList<FmtAppender> appenders2 = loadingAppender.appenders(key.subjectClass());
				for (final FmtAppender a2 : appenders2) {
					final FmtAppender delegateAppender = a2.delegateAppender(key);
					if (delegateAppender!=null) return delegateAppender;
				}
			} else {
				final FmtAppender delegateAppender = a.delegateAppender(key);
				if (delegateAppender!=null) return delegateAppender;
			}
		}
		return key.specifier().isEmpty() ? FmtToStringAppender.INSTANCE : new FmtFallbackAppender(key);
	}

	private static final Interner<FmtMultiAppender> interner = Interners.newWeakInterner();

	static final FmtMultiAppender POOR = interner.intern(new FmtMultiAppender(ImmutableList.of(
			FmtToStringAppender.INSTANCE,
			FmtStringFormatAppender.INSTANCE)));

	private final ImmutableList<FmtAppender> appenders;

	private final LoadingCache<FmtKey, FmtAppender> cache = CacheBuilder
			.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<FmtKey, FmtAppender>() {
				@Override public FmtAppender load(FmtKey key) {
					return FmtMultiAppender.this.load(key);
				}
			});
}
