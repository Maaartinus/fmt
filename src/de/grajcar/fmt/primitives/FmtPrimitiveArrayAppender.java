package de.grajcar.fmt.primitives;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;
import de.grajcar.fmt.intenal.MgPrimitiveInfo;

@RequiredArgsConstructor @Wither(AccessLevel.PRIVATE) public final class FmtPrimitiveArrayAppender extends FmtAppender {
	public FmtPrimitiveArrayAppender() {
		this(new FmtPrimitiveAppender());
	}

	@Override public FmtAppender delegateAppender(FmtKey key) {
		final Class<?> subjectClass = key.subjectClass();
		Class<?> componentClass = subjectClass.getComponentType();
		if (componentClass==null) return null;
		componentClass = MgPrimitiveInfo.of(componentClass).wrapperClass();
		final FmtKey componentKey = key.withSubjectClass(componentClass);
		final FmtPrimitiveAppender componentAppender = this.componentAppender.delegateAppender(componentKey);
		if (componentAppender==null) return null;
		return withComponentAppender(componentAppender);
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		final boolean packed = componentAppender.options().padded();
		if (!packed) target.append('[');
		if (subject.getClass().getComponentType().isPrimitive()) {
			primitiveAppendTo(target, subject, context);
		} else {
			wrapperAppendTo(target, subject, context);
		}
		if (!packed) target.append(']');
	}

	private void primitiveAppendTo(StringBuilder target, Object subject, FmtContext context) {
		final boolean packed = componentAppender.options().padded();
		boolean addComma = false;
		if (subject instanceof byte[]) {
			for (final byte x : (byte[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof short[]) {
			for (final short x : (short[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof int[]) {
			for (final int x : (int[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof long[]) {
			for (final long x : (long[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof char[]) {
			for (final char x : (char[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof float[]) {
			for (final double x : (float[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof double[]) {
			for (final double x : (double[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else if (subject instanceof boolean[]) {
			for (final boolean x : (boolean[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x);
			}
		} else {
			throw new RuntimeException("impossible");
		}
	}

	private void wrapperAppendTo(StringBuilder target, Object subject, FmtContext context) {
		final boolean packed = componentAppender.options().padded();
		boolean addComma = false;
		if (isIntLikeArray(subject)) {
			for (final Number x : (Number[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x.longValue());
			}
		} else if (subject instanceof Float[]) {
			for (final Float x : (Float[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x.doubleValue());
			}
		} else if (subject instanceof Double[]) {
			for (final Double x : (Double[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x.doubleValue());
			}
		} else if (subject instanceof Character[]) {
			for (final Character x : (Character[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x.charValue());
			}
		} else if (subject instanceof Boolean[]) {
			for (final Boolean x : (Boolean[]) subject) {
				if (addComma) target.append(", ");
				addComma = !packed;
				componentAppender.appendTo(target, context, x.booleanValue());
			}
		} else {
			throwBugException(subject);
		}
	}

	private boolean isIntLikeArray(Object subject) {
		final Class<?> componentClass = subject.getClass().getComponentType();
		if (componentClass==null) return false;
		final MgPrimitiveInfo info = MgPrimitiveInfo.of(componentClass);
		if (info==null) return false;
		return info.isIntLike();
	}

	@NonNull private final FmtPrimitiveAppender componentAppender;
}
