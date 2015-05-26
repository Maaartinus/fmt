package de.grajcar.fmt.primitives;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.grajcar.fmt.intenal.MgPrimitiveInfo;

@Getter @RequiredArgsConstructor @EqualsAndHashCode @Builder(builderClassName="Builder") final class PrimitiveOptions {
	static class Builder {
		@Nullable PrimitiveOptions apply(String specifier) {
			for (int i=0; i<specifier.length(); ++i) apply(specifier.charAt(i));
			return finish();
		}

		private void apply(char c) {
			switch (c) {
				case 's': unsigned = false; explicitSigned = true; break;
				case 'u': unsigned = true; explicitSigned = true; break;
				case 'd': hex = false; break;
				case 'x': hex = true; uppercase = false; break;
				case 'X': hex = true; uppercase = true; break;
				case 'p': padded = true; hex = true; break;
				case 'j': javaSyntax = true; break;
				default: invalid = true; break;
			}
		}

		@Nullable private PrimitiveOptions finish() {
			if (invalid) return null;
			uppercase &= hex;
			if (!explicitSigned) unsigned = hex;
			return build();
		}

		private boolean invalid;
		private boolean explicitSigned;
	}

	public boolean isCompatibleWith(MgPrimitiveInfo info) {
		if (info.isIntLike()) {
			return true; //TODO
		}
		return false;
	}

	static final PrimitiveOptions DEFAULT = new PrimitiveOptions(false, false, false, false, false);

	private final boolean hex;
	private final boolean unsigned;
	private final boolean uppercase;
	private final boolean padded;
	private final boolean javaSyntax;
}
