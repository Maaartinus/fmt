package de.grajcar.fmt.demo;

import de.grajcar.fmt.Fmt;
import de.grajcar.fmt.FmtAppender;
import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtErrorHandler;
import de.grajcar.fmt.FmtOption;

@SuppressWarnings("boxing") public class FmtDemo {
	private enum Part {
		INTRO {
			@Override void print() {
				fmt.format("`[s]` is a very flexible formatting class.", Fmt.class);
				fmt.format("It can be obtained from via `[s]` which is configurable in many ways.", FmtContext.class);
				fmt.format("Most important are `[s]`s, which do the real work.", FmtAppender.class);
				fmt.format("They get selected based both on the format string and the actual argument.");
				sep();
				fmt.format("There are a few predefined `[s]`s and it''s easy to create your own.", FmtAppender.class);
				fmt.format("You can also delegate to `String.format`, `SimpleDateFormat`, and others.");
				sep();
				fmt.format("Handling of all kinds of errors can be configured with a `[s]`.", FmtErrorHandler.class);
				fmt.format("Especially, you can make sure that all exceptions get only logged,");
				fmt.format("or that they get always thrown, or whatever you want.");
			}
		},
		INTRO_2 {
			@Override void print() {
				fmt.format("`Fmt` can be used similarly to `String.format`, but it''s much more flexible.");
				fmt.format("Especially, you can handle errors however you want.");
				succinctFmt.format("An error report can point to the faulty line like this: [].");
				fmt.format("Or (good for developers) or be logged (good in production), or whatever.");
				fmt.format("See [], if you want to know more.", FmtErrorHandler.class);
				sep();
				succinctFmt.format("Unused arguments don''t get lost silently: ", "an_unused_argument");
				sep();
				fmt.format("You can also choose how objects are to be formatted.");
				fmt.format("Do you prefer [%], or [], or [ud], or maybe [p] for a byte array?", bytes, bytes, bytes, bytes);
				fmt.format("Or maybe you own format?");
			}
		},
		QUOTING {
			@Override void print() {
				fmt.format("Normally, only these three chars are special: '''[]'.");
				fmt.format("No chars except them get interpreted: ~+-#@%{}\\ => ~+-#@%{}\\.");
				fmt.format("No chars enclosed in quotes get interpreted: '''[\\]''' -> '[\\]'.");
				fmt.format("To output a single quote, it needs to be doubled: '''' -> ''.");
				fmt.format("Brackets need to be enclosed in single quotes: '''[]''' -> '[]'.");
			}
		},
		ERROR_HANDLING {
			@Override void print() {
				succinctFmt.format("You can get a single stacktrace element like this: []");
				verboseFmt.format("You can get a full stacktrace like this: []");
			}
		},
		QUOTING_ERRORS {
			@Override void print() {
				succinctFmt.format("An unmatched quote gets reported: ''tail -> 'tail.");
				succinctFmt.format("An unmatched opening bracket gets reported: '[tail' -> [tail.");
				succinctFmt.format("An unmatched closing bracket gets reported, too: ']tail' -> ]tail.");
			}
		},
		TRIVIAL {
			@Override void print() {
				fmt.format("For a default-looking output use empty brackets: '[]' -> [].", now);
				fmt.format("Unless configured otherwise, they use `toString` for formatting."); //TODO
				dumbFmt.format("This is pretty dumb for arrays: '[]' -> [].", bytes);
				fmt.format("But there are preconfigured fmts doing better: '[]' -> [].", bytes);
			}
		},
		INTEGERS {
			@Override void print() {
				fmt.format("The Answer to Life, the Universe and Everything using predefined format via '[]': [].", 42);
				fmt.format("The same using `String.format` via '[%%05X]': [%05X].", 42);
				fmt.format("The same via '[X]': [X].", 42);
			}
		},
		BYTE_ARRAYS {
			@Override void print() {
				fmt.format("A byte array using predefined format via '[]': [].", bytes);
				fmt.format("A byte array using `toString` via '[%]': [%].", bytes);
				fmt.format("A byte array as unsigned uppercase hex via '[X]': [X].", bytes);
				fmt.format("A byte array as signed lowecase hex via '[sx]': [sx].", bytes);
				fmt.format("A byte array as unsigned decimal via '[u]' [u].", bytes);
				fmt.format("A byte array as signed decimal via '[s]': [s].", bytes);
				fmt.format("A byte array as packed lowercase hex via '[px]': [px].", bytes);
				fmt.format("A byte array as packed uppercase hex via '[pX]': [pX].", bytes);
				succinctFmt.format("A byte array with an invalid format via '[qaz]': [qaz].", bytes);
			}
		},
		DATES {
			@Override void print() {
				fmt.format("Now using `toString()` via [%].", now);
				fmt.format("Now using default `SimpleDateFormat` via '[]': [].", now);
				fmt.format("Now using `SimpleDateFormat` via '[yy-MM-dd''T''HH:mm]': [yy-MM-dd'T'HH:mm].", now);
				succinctFmt.format("An invalid `SimpleDateFormat` may throw or not, depending on configuraton: [sdf:xx].", now);
				fmt.format("The `SimpleDateFormat` gets used in a thread-safe way.");
			}
		},
		CLASSES {
			@Override void print() {
				final Class<?> clazz = FmtDemo.Part.class;
				fmt.format("This class'' using `toString()` via '[%]': [%].", clazz);
				fmt.format("This class'' name using via '[n]': [n].", clazz);
				fmt.format("This class'' simple name via '[s]': [s].", clazz);
				fmt.format("This class'' canonical name via '[c]': [c].", clazz);
				fmt.format("This class'' \"local\" name, i.e., the name without the package via '[l]': [l].", clazz);
			}
		},
		ERRORS {
			@Override void print() {
				succinctFmt.format("Two unused arguments.", now, getClass());
				succinctFmt.format("A byte array [] and one unused argument.", bytes, getClass());
				succinctFmt.format("See [] in case this format doesn''t suit you.", FmtErrorHandler.class);
			}
		},
		ADVANCED {
			@Override void print() {
				succinctFmt.format("A byte array [] and one unused argument.", bytes, getClass());
				fmt.format("The current thread [].", Thread.currentThread());
				fmt.format("Now [] using toString().", now);

				fmt.format("The current time [] using toString.", currentTime);
				fmt.format("The current date [] using toString.", currentDate);
			}
		},
		PRIMITIVES {
			@Override void print() {
				final Byte b = 15;
				fmt.format("A byte using `toString` via '[%]': [%]", b);
				fmt.format("A byte in unsigned uppercase hex via '[X]': [X]", b);
				fmt.format("A byte in padded unsigned uppercase hex via '[pX]': [pX]", b);
			}
		},
		EXCEPTIONS {
			@Override void print() {
				final Exception e = DemoExceptionMaker.newException();
				usingToString("An exception", e);
				line("An exception with a stack trace in legacy format", "l", e);
				line("An exception with a stack trace in normal format", "n", e);
				line("An exception with the default format", "", e);
			}
		}
		;

		void usingToString(String intro, Object subject) {
			line(intro + " using `toString`", "%", subject);
		}

		void line(String intro, String format, Object subject) {
			fmt.format(intro + " via '[" + format + "]': [" + format + "]", subject);
		}

		abstract void print();
	}

	public static void main(String[] args) {
		new FmtDemo().go();
	}

	private void go() {
		for (final Part p : Part.values()) {
			sep();
			System.out.println(p);
			p.print();
		}
	}

	private static void sep() {
		System.out.println();
	}


	private static final FmtContext DEMO_CONTEXT = FmtContext.newRichContext(FmtOption.ON_ERROR_THROWING)
			.withAfterEach("\n");

	private static final Fmt fmt = DEMO_CONTEXT.fmt(System.out);
	private static final Fmt succinctFmt = fmt.context()
			.withOption(FmtOption.ON_ERROR_SUCCINCT).fmt(System.out);
	private static final Fmt verboseFmt = fmt.context()
			.withOption(FmtOption.ON_ERROR_VERBOSE).fmt(System.out);

	private static final Fmt dumbFmt = FmtContext.newRichContext(FmtOption.ON_ERROR_THROWING).
			withAfterEach("\n").fmt(System.out);

	private static final byte[] bytes = new byte[] {0, 100, (byte) 200};
	private static final java.util.Date now = new java.util.Date();
	private static final java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());
	private static final java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
}
