package de.grajcar.fmt;

import java.util.List;

import com.google.common.collect.Iterables;

import junit.framework.TestCase;

import de.grajcar.fmt.misc.FmtDateAppender;
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

	private void check(Class<?> expectedAppenderClass, Class<?> subjectClass) {
		final List<FmtAppender> appenders = loader.appenders(subjectClass);
		final FmtAppender actualAppender = Iterables.getOnlyElement(appenders);
		assertSame(expectedAppenderClass, actualAppender.getClass());
	}

	private final FmtLoadingAppender loader = FmtLoadingAppender.INSTANCE;
}
