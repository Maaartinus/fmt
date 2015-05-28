package de.grajcar.fmt.primitives;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtKey;
import de.grajcar.fmt.intenal.MgPrimitiveInfo;

@RequiredArgsConstructor @Getter(AccessLevel.PACKAGE) @Wither(AccessLevel.PRIVATE)
public final class FmtPrimitiveAppender extends FmtAppender {
	public FmtPrimitiveAppender() {
		this(PrimitiveOptions.DEFAULT, MgPrimitiveInfo.BYTE);
	}

	@Override @Nullable public final FmtPrimitiveAppender delegateAppender(FmtKey key) {
		FmtPrimitiveAppender result = this;
		final Class<?> subjectClass = key.subjectClass();
		final MgPrimitiveInfo info = MgPrimitiveInfo.of(subjectClass);
		if (info==null) return null;
		result = result.withInfo(info);
		final String specifier = key.specifier();
		if (specifier.isEmpty()) return result;
		final PrimitiveOptions options = PrimitiveOptions.from(specifier);
		if (options==null) return null;
		if (!options.isCompatibleWith(info)) return null;
		result = result.withOptions(options);
		return result;
	}

	@Override public void appendTo(StringBuilder target, FmtContext context, Object subject) {
		if (subject instanceof Float) {
			appendTo(target, context, ((Float) subject).doubleValue());
		} else if (subject instanceof Double) {
			appendTo(target, context, ((Double) subject).doubleValue());
		} else if (subject instanceof Number) { // can be Byte, Short, Integer, or Long only
			appendTo(target, context, ((Number) subject).longValue());
		} else if (subject instanceof Character) {
			appendTo(target, context, ((Character) subject).charValue());
		} else {
			appendTo(target, context, ((Boolean) subject).booleanValue());
		}
	}

	final void appendTo(StringBuilder target, FmtContext context, long subject) {
		assert Integer.bitCount(info.byteLength()) == 1;
		if (options.unsigned() && info.byteLength()<8) subject &= ~(-1L << (8*info.byteLength()));
		if (options.hex()) {
			hexAppendTo(target, subject);
		} else {
			if (subject>=0 || !options.unsigned()) {
				target.append(subject);
			} else {
				final long tenth = (subject>>>1) / 5;
				target.append(tenth);
				target.append(LOWERCASE_HEX[(int) (subject - 10*tenth)]);
			}
		}
	}

	final void appendTo(StringBuilder target, FmtContext context, double subject) {
		target.append(String.format(context.locale(), "%s", Double.valueOf(subject))); //TODO
	}

	final void appendTo(StringBuilder target, FmtContext context, char subject) {
		target.append("" + subject); //TODO
	}

	final void appendTo(StringBuilder target, FmtContext context, boolean subject) {
		target.append("" + subject); //TODO
	}

	private void hexAppendTo(StringBuilder target, long subject) {
		final boolean isNegative = subject<0;
		if (isNegative && !options.unsigned()) {
			target.append('-');
			subject =- subject;
		}
		if (options.javaSyntax()) target.append("0x");
		final char[] hex = options.uppercase() ? UPPERCASE_HEX : LOWERCASE_HEX;
		boolean nonZero = false;
		for (int pos=2*info.byteLength(); pos-->0; ) {
			final char hexDigit = hexDigit(hex, subject, pos);
			if (nonZero || hexDigit != '0') {
				nonZero = true;
				target.append(hexDigit);
			} else if (pos==0 | options.padded()) {
				target.append('0');
			}
		}
	}

	private char hexDigit(char[] hex, long subject, int pos) {
		subject >>>= 4*pos;
		return hex[(int) (subject&15)];
	}

	private static final char[] LOWERCASE_HEX = "0123456789abcdef".toCharArray();
	private static final char[] UPPERCASE_HEX = "0123456789ABCDEF".toCharArray();

	@NonNull private final PrimitiveOptions options;
	@NonNull private final MgPrimitiveInfo info;
}
