package de.grajcar.fmt.primitives;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import de.grajcar.fmt.intenal.MgPrimitiveInfo;

@RequiredArgsConstructor @Getter @EqualsAndHashCode final class PrimitiveOptions {
	@SuppressFBWarnings("SF_SWITCH_FALLTHROUGH") static PrimitiveOptions from(String specifier) {
		boolean hex = false;
		boolean decimal = false;
		boolean unsigned = false;
		boolean signed = false;
		boolean uppercase = false;
		boolean padded = false;
		boolean javaSyntax = false;
		int separated = 0;

		for (int i=0; i<specifier.length(); ++i) {
			switch (specifier.charAt(i)) {
				case '_':
					if (separated==2) return null;
					++separated;
					break;
				case 'j':
					if (javaSyntax) return null;
					javaSyntax = true;
					break;
				case 's':
					if (unsigned | signed) return null;
					signed = true;
					break;
				case 'u':
					if (unsigned | signed) return null;
					unsigned = true;
					break;
				case 'p':
					if (padded) return null;
					padded = true;
					break;
				case 'd':
					if (decimal | hex) return null;
					decimal = true;
					break;
				case 'X':
					uppercase = true;
					//$FALL-THROUGH$
				case 'x':
					if (decimal | hex) return null;
					hex = true;
					break;
				default: return null;
			}
		}
		unsigned |= !signed & hex;
		return new PrimitiveOptions(hex, unsigned, uppercase, padded, javaSyntax, separated);
	}

	public boolean isCompatibleWith(MgPrimitiveInfo info) {
		if (info.isIntLike()) {
			return true; //TODO
		}
		return false;
	}

	static final PrimitiveOptions DEFAULT = new PrimitiveOptions(false, false, false, false, false, 0);

	private final boolean hex;
	private final boolean unsigned;
	private final boolean uppercase;
	private final boolean padded;
	private final boolean javaSyntax;
	private final int separated;
}
