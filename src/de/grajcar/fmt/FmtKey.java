package de.grajcar.fmt;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Wither;

/** The key determining what appender gets used. */
@ToString @EqualsAndHashCode @Getter @Wither public final class FmtKey {
	public FmtKey(String specifier, Class<?> subjectClass) {
		checkArgument(!subjectClass.isPrimitive(), "Please use the wrapper class instead of %s", subjectClass);
		this.specifier = specifier;
		this.subjectClass = subjectClass;
	}

	@NonNull private final String specifier;
	@NonNull private final Class<?> subjectClass;
}
