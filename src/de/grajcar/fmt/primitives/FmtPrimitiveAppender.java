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

	@Override public String helpOnFormatsFor(Class<?> subjectClass) {
		final MgPrimitiveInfo info = MgPrimitiveInfo.of(subjectClass);
		if (info==null) return "";
		return ""
		+"a combination of jsupdxX:"
		+ "\n"
		+ "_: separated: separate groups of 6 decimal or 8 hexadecimal digits by underscores;"
		+ " repeating this option leads to separating groups of 3 decimal or 4 hexadecimal digits"
		+ "\n"
		+ "j: javaSyntax"
		+ "\n"
		+ "s: signed"
		+ "\n"
		+ "u: unsigned"
		+ "\n"
		+ "p: padded"
		+ "\n"
		+ "d: decimal"
		+ "\n"
		+ "x: hexadecimal lowercase"
		+ "\n"
		+ "X: hexadecimal uppercase"
		+ "";
	}

	final void appendTo(StringBuilder target, FmtContext context, long subject) {
		if (options.unsigned() && info.byteLength()<8) subject &= ~(-1L << (8*info.byteLength()));
		if (options.hex()) {
			hexAppendTo(target, subject);
		} else {
			if (options.separated() > 0) {
				appendSeparatedTo(target, subject);
			} else if (subject>=0 || !options.unsigned()) {
				target.append(subject);
			} else {
				final long tenth = (subject>>>1) / 5;
				target.append(tenth);
				target.append(LOWERCASE_HEX[(int) (subject - 10*tenth)]);
			}
		}
	}

	private void appendSeparatedTo(StringBuilder target, long subject) {
		boolean started = false;
		if (subject >= MILLION_TO_3) {
			final long x = subject / MILLION_TO_3;
			target.append(x);
			subject -= x * MILLION_TO_3;
			started = true;
		} else if (subject>=0) {
			// nothing to do
		} else if (options.unsigned()) {
			final long x = (subject >>> 1) / (MILLION_TO_3/2);
			target.append(x);
			subject -= x * MILLION_TO_3;
			started = true;
		} else if (subject <= -MILLION_TO_3) {
			final long x = subject / MILLION_TO_3;
			target.append(x);
			subject -= x * MILLION_TO_3;
			subject = -subject;
			started = true;
		} else {
			target.append('-');
			subject = -subject;
		}
		assert 0 <= subject && subject < MILLION_TO_3;

		if (subject > MILLION_TO_2) {
			final int x = (int) (subject / MILLION_TO_2);
			appendBelowMillionTo(target, started, x);
			subject -= MILLION_TO_2 * x;
			started = true;
		}
		if (subject > MILLION_TO_1) {
			final int x = (int) (subject / MILLION_TO_1);
			appendBelowMillionTo(target, started, x);
			subject -= MILLION_TO_1 * x;
			started = true;
		}
		appendBelowMillionTo(target, started, (int) subject);
	}

	private void appendBelowMillionTo(StringBuilder target, boolean started, int subject) {
		assert 0 <= subject && subject < MILLION_TO_1;
		assert options.separated() > 0;
		final int x = subject / 1000;
		if (started) target.append('_');
		appendBelowThousandTo(target, started, x);
		if (x>0) {
			if (options.separated() == 2) target.append('_');
			subject -= 1000 * x;
			started = true;
		} else if (started) {
			if (options.separated() == 2) target.append('_');
		}
		appendBelowThousandTo(target, started, subject);
	}

	private void appendBelowThousandTo(StringBuilder target, boolean started, int subject) {
		assert 0 <= subject && subject < 1000;
		if (subject >= 100) {
			target.append(subject);
		} else if (subject >= 10) {
			if (started) target.append("0");
			target.append(subject);
		} else if (subject >= 1) {
			if (started) target.append("00");
			target.append(subject);
		} else {
			if (started) target.append("000");
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
			subject = -subject;
		}
		if (options.javaSyntax()) target.append("0x");
		final char[] hex = options.uppercase() ? UPPERCASE_HEX : LOWERCASE_HEX;
		boolean nonZero = false;
		final int positionMask = options.separated() == 2 ? 3 : options.separated() == 1 ? 7 : -1;
		for (int pos=2*info.byteLength(); pos-->0; ) {
			final char hexDigit = hexDigit(hex, subject, pos);
			if (nonZero || hexDigit != '0') {
				nonZero = true;
				target.append(hexDigit);
				if ((pos&positionMask) == 0 && pos > 0) target.append('_');
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

	private static final long MILLION_TO_1 = 1_000_000L;
	private static final long MILLION_TO_2 = MILLION_TO_1 * MILLION_TO_1;
	private static final long MILLION_TO_3 = MILLION_TO_1 * MILLION_TO_2;

	@NonNull private final PrimitiveOptions options;
	@NonNull private final MgPrimitiveInfo info;
}
