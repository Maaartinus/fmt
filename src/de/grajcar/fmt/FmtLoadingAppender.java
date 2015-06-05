package de.grajcar.fmt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.grajcar.fmt.intenal.MgPrimitiveInfo;
import de.grajcar.fmt.primitives.FmtPrimitiveAppender;

/**
 * The engine responsible for loading predefined appenders for {@link FmtContext#richContext(FmtOption...)}.
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) final class FmtLoadingAppender extends FmtAppender {
	@Override public FmtAppender delegateAppender(FmtKey key) {
		final ImmutableList<FmtAppender> appenders = appenders(key.subjectClass());
		for (final FmtAppender a : appenders) {
			final FmtAppender delegateAppender = a.delegateAppender(key);
			if (delegateAppender!=null) return delegateAppender;
		}
		return null;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		throw throwBugException(subject);
	}

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		final ImmutableList<FmtAppender> appenders = appenders(subjectClass);
		final List<String> result = Lists.newArrayList();
		for (final FmtAppender a : appenders) {
			final String s = a.helpOnFormatsFor(subjectClass);
			if (!s.isEmpty()) result.add(s);
		}
		return Joiner.on("\n\n").join(result);
	}

	@VisibleForTesting ImmutableList<FmtAppender> appenders(Class<?> subjectClass) {
		return cache.getUnchecked(subjectClass);
	}

	private ImmutableList<FmtAppender> load(Class<?> subjectClass) {
		if (isPrimitiveOrWrapper(subjectClass)) return primitiveAppenders(subjectClass, false);
		if (subjectClass.isArray()) return arrayAppenders(subjectClass);


		final ImmutableList.Builder<FmtAppender> result = ImmutableList.builder();
		for (final String s : supertypeSimpleNames(subjectClass)) result.add(loadAppender(MISC_PACKAGE_NAME, s));
		return result.build();
	}

	private List<String> supertypeSimpleNames(Class<?> subjectClass) {
		final Map<String, String> nameToSimpleNameMap = Maps.newHashMap();
		addSupertypesTo(nameToSimpleNameMap, subjectClass);
		final List<String> result = Lists.newArrayList();
		for (final String s : MISC_CLASS_NAMES) {
			final String simpleName = nameToSimpleNameMap.get(s);
			if (simpleName!=null) result.add(simpleName);
		}
		return result;
	}

	private void addSupertypesTo(Map<String, String> nameToSimpleNameMap, Class<?> cl) {
		if (MISC_CLASS_NAMES.contains(cl.getName())) nameToSimpleNameMap.put(cl.getName(), cl.getSimpleName());
		if (cl.getSuperclass() != null) addSupertypesTo(nameToSimpleNameMap, cl.getSuperclass());
		for (final Class<?> ci : cl.getInterfaces()) addSupertypesTo(nameToSimpleNameMap, ci);
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

	/**
	 * Intentionally using class names in order not to force class loading (except for classes which are loaded anyway).
	 *
	 * <p>The order is significant as e.g. SQLException is both @ {@link Throwable} amd (@link Iterable<Throwable>}.
	 */
	private static final ImmutableSet<String> MISC_CLASS_NAMES = ImmutableSet.of(
			"java.util.Date",
			Throwable.class.getName(),
			CharSequence.class.getName(),
			Iterator.class.getName(),
			Iterable.class.getName(),
			Map.class.getName(),
			Class.class.getName()
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
