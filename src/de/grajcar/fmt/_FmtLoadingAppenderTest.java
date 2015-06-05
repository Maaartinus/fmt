package de.grajcar.fmt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import junit.framework.TestCase;

import de.grajcar.fmt.misc.FmtCharSequenceAppender;
import de.grajcar.fmt.misc.FmtClassAppender;
import de.grajcar.fmt.misc.FmtDateAppender;
import de.grajcar.fmt.misc.FmtIterableAppender;
import de.grajcar.fmt.misc.FmtIteratorAppender;
import de.grajcar.fmt.misc.FmtMapAppender;
import de.grajcar.fmt.misc.FmtThrowableAppender;
import de.grajcar.fmt.primitives.FmtPrimitiveAppender;
import de.grajcar.fmt.primitives.FmtPrimitiveArrayAppender;

public final class _FmtLoadingAppenderTest extends TestCase {
	public void testAppenders_primitive() {
		check(FmtPrimitiveAppender.class, byte.class);
		check(FmtPrimitiveAppender.class, Byte.class);
		check(FmtPrimitiveAppender.class, int.class);
		check(FmtPrimitiveAppender.class, Integer.class);
	}

	public void testAppenders_primitiveArrays() {
		check(FmtPrimitiveArrayAppender.class, byte[].class);
		check(FmtPrimitiveArrayAppender.class, Byte[].class);
		check(FmtPrimitiveArrayAppender.class, int[].class);
		check(FmtPrimitiveArrayAppender.class, Boolean[].class);
	}

	public void testAppenders_misc() {
		check(FmtDateAppender.class, java.util.Date.class);
		check(FmtDateAppender.class, java.sql.Date.class);
		check(FmtDateAppender.class, java.sql.Time.class);
		check(FmtDateAppender.class, java.sql.Timestamp.class);
		check(FmtThrowableAppender.class, Throwable.class);
	}

	public void test_AllPredefinedAppendersAreListed() throws IOException {
		final ClassPath classPath = ClassPath.from(getClass().getClassLoader());
		final String prefix = Fmt.class.getPackage().getName() + ".";
		for (final ClassInfo c : classPath.getTopLevelClasses()) {
			final String packageName = c.getPackageName();
			if (!packageName.startsWith(prefix)) continue;
			final Class<?> appenderClass = c.load();
			if (!FmtAppender.class.isAssignableFrom(appenderClass)) continue;
			assertTrue(appenderToExampleSubjectMap.containsKey(appenderClass));
		}
	}

	public void test_AllPredefinedAppendersHaveDefaults() {
		for (final Map.Entry<Class<? extends FmtAppender>, Object> e : appenderToExampleSubjectMap.entrySet()) {
			final FmtKey fmtKey = new FmtKey("", e.getValue().getClass());
			final FmtAppender delegateAppender = loader.delegateAppender(fmtKey);
			assertNotNull(e.toString(), delegateAppender);
			assertEquals(e.getKey(), delegateAppender.getClass());
		}
	}

	private void check(Class<?> expectedAppenderClass, Class<?> subjectClass) {
		final List<FmtAppender> appenders = loader.appenders(subjectClass);
		final FmtAppender actualAppender = Iterables.getOnlyElement(appenders);
		assertSame(expectedAppenderClass, actualAppender.getClass());
	}

	private static final ImmutableMap<Class<? extends FmtAppender>, Object> appenderToExampleSubjectMap =
			ImmutableMap.<Class<? extends FmtAppender>, Object>builder()
			.put(FmtCharSequenceAppender.class, "a string")
			.put(FmtClassAppender.class, System.class)
			.put(FmtDateAppender.class, new Date())
			.put(FmtIterableAppender.class, Iterables.concat(ImmutableList.of("a", "bc"), ImmutableList.of("def")))
			.put(FmtIteratorAppender.class, ImmutableList.of("A", "BC").iterator())
			.put(FmtMapAppender.class, ImmutableMap.of("K", "V", "K2", "V2"))
			.put(FmtThrowableAppender.class, new SQLException("sequel"))
			.put(FmtPrimitiveAppender.class, Long.valueOf(43))
			.put(FmtPrimitiveArrayAppender.class, new long[] {44, 45})
			.build();

	private final FmtLoadingAppender loader = FmtLoadingAppender.INSTANCE;
}
