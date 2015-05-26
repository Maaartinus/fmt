package de.grajcar.fmt;

public final class FmtException extends RuntimeException {//TODO clean up FmtException
	private FmtException(String message, Exception cause) {
		super(message, cause);
	}

	static FmtException throwBugException(Object subject) {
		throw new FmtException("subject=\"" + subject + "\"", null);
	}

	static FmtException throwException(String message, Exception cause) {
		if (cause instanceof FmtException) throw (FmtException) cause;
		throw new FmtException(message, cause);
	}

	static FmtException makeException(FmtError error) {
		return new FmtException(error.toString(), null);
	}
}
