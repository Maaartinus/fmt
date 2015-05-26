package de.grajcar.fmt;

import java.util.Locale;

public enum FmtOption {
	/** Use the English locale. */
	LOCALIZED_NO,
	/** Use the default locale. */
	LOCALIZED_YES,
	/** On error, throw an exception. */
	ON_ERROR_THROWING,
	/** On error, log it. */
	ON_ERROR_LOGGING,
	/** On error, append the full stacktrace to the output. */
	ON_ERROR_VERBOSE,
	/** On error, append the one stacktrace element identifying the invoking line to the output. */
	ON_ERROR_SUCCINCT,
	;

	FmtContext apply(FmtContext context) {
		switch (this) {
			case LOCALIZED_NO:
				return context.withLocale(Locale.ENGLISH);
			case LOCALIZED_YES:
				return context.withLocale(Locale.getDefault());
			case ON_ERROR_THROWING:
				return context.withErrorHandler(FmtErrorHandler.ThrowingHandler.INSTANCE);
			case ON_ERROR_LOGGING:
				return context.withErrorHandler(FmtErrorHandler.LoggingHandler.INSTANCE);
			case ON_ERROR_VERBOSE:
				return context.withErrorHandler(FmtErrorHandler.ReportingHandler.VERBOSE_INSTANCE);
			case ON_ERROR_SUCCINCT:
				return context.withErrorHandler(FmtErrorHandler.ReportingHandler.SUCCINCT_INSTANCE);
		}
		throw new RuntimeException("Unexpected option: " + this);
	}
}
