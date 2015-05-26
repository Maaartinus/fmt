package de.grajcar.fmt;

import java.io.IOException;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import de.grajcar.fmt.intenal.MgPrimitiveInfo;
import de.grajcar.fmt.primitives.FmtPrimitiveAppender;

/**
 * The engine responsible for loading predefined appenders for {@link FmtContext#newRichContext(FmtOption...)}.
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) final class FmtLoadingAppender extends FmtAppender {
	@Override public FmtAppender delegateAppender(FmtKey key) {
		throw throwBugException(key);
	}

	@Override public void appendTo(Appendable target, FmtContext context, Object subject) throws IOException {
		throw throwBugException(subject);
	}

	ImmutableList<FmtAppender> appenders(Class<?> subjectClass) {
		return cache.getUnchecked(subjectClass);
	}

	private ImmutableList<FmtAppender> load(Class<?> subjectClass) {
		if (isPrimitiveOrWrapper(subjectClass)) return primitiveAppenders(subjectClass, false);
		if (subjectClass.isArray()) return arrayAppenders(subjectClass);
		final List<String> fragments = Lists.newArrayList();
		addMiscAppenderNameFragmentTo(fragments, subjectClass);
		final ImmutableList.Builder<FmtAppender> result = ImmutableList.builder();
		for (final String s : fragments) result.add(loadAppender(MISC_PACKAGE_NAME, s));
		return result.build();
	}

	private void addMiscAppenderNameFragmentTo(List<String> result, Class<?> subjectClass) {
		for (Class<?> cl = subjectClass; cl!=null; cl = cl.getSuperclass()) {
			final String name = cl.getName();
			if (!MISC_CLASS_NAMES.contains(name)) continue;
			final String simpleName = cl.getSimpleName();
			final String fragment = simpleName.equals("Throwable") ? "Exception" : simpleName;
			result.add(fragment);
		}
	}

	private ImmutableList<FmtAppender> arrayAppenders(Class<?> subjectClass) {
		final Class<?> componentClass = subjectClass.getComponentType();
		if (isPrimitiveOrWrapper(componentClass)) return primitiveAppenders(componentClass, true);
		return ImmutableList.of();
	}

	private ImmutableList<FmtAppender> primitiveAppenders(Class<?> subjectClass, boolean asArray) {
		final String fragment = "Primitive" + (asArray ? "Array" : "");
		final FmtAppender appender = loadAppender(PRIMITIVES_PACKAGE_NAME, fragment);
		return ImmutableList.of(appender);
	}

	private boolean isPrimitiveOrWrapper(Class<?> subjectClass) {
		return MgPrimitiveInfo.of(subjectClass) != null;
	}

	private FmtAppender loadAppender(String packageName, String fragment) {
		final String appenderClassName = packageName + "." + "Fmt" + fragment + "Appender";
		try {
			final Class<?> appenderClass = getClass().getClassLoader().loadClass(appenderClassName);
			final Object result = appenderClass.newInstance();
			return FmtAppender.class.cast(result);
		} catch (final Exception e) {
			throw FmtException.throwException("Cannot get appender " + appenderClassName, e);
		}
	}

	private static final ImmutableSet<String> MISC_CLASS_NAMES = ImmutableSet.of(
			"java.util.Date",
			"java.lang.Throwable",
			"java.lang.Class"
			);

	private static final String PRIMITIVES_PACKAGE_NAME = FmtPrimitiveAppender.class.getPackage().getName();
	private static final String MISC_PACKAGE_NAME = PRIMITIVES_PACKAGE_NAME.replaceFirst("[^.]+$", "misc");

	static final FmtLoadingAppender INSTANCE = new FmtLoadingAppender();

	private final LoadingCache<Class<?>, ImmutableList<FmtAppender>> cache = CacheBuilder
			.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Class<?>, ImmutableList<FmtAppender>>() {
				@Override public ImmutableList<FmtAppender> load(Class<?> subjectClass) {
					return FmtLoadingAppender.this.load(subjectClass);
				}
			});
}
