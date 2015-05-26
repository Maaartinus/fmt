package de.grajcar.dout;

import java.io.PrintStream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import de.grajcar.fmt.FmtContext;


/**
 * A utility class for printf-style debugging.
 * If in doubt, {@code Dout} it out!
 *
 * <p> The name stands for Debug out; the purpose of the class is to enable useful degigging prints.
 * Unlike logging it's not supposed to stay in the final application,
 * calls to {@code Dout} should be placed into code while debugging and removed (or replaced by logging) afterwards.
 *
 * <p>Each output contains (at least) the deepest {@code StackTraceElement},
 * so it's easy to find the line and to remove it when done.
 */
@UtilityClass public class Dout {
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE) public static class Initializer {
		public static void disable() {
			doutifier.destination((PrintStream) null);
		}

		public static void useSystemOut() {
			doutifier.destination(System.out);
		}

		public static void useSystemErr() {
			doutifier.destination(System.err);
		}

		public static void context(FmtContext context) {
			doutifier.context(context);
		}

		public final static Doutifier doutifier() {
			return doutifier;
		}

		private static final Doutifier doutifier= new Doutifier();
	}

	public static void a(Object... objects) {
		if (isEnabled) doutifier.print(doutifier.defaultDepth(), objects);
	}
	/**
	 * Output the one {@code StackTraceElement} corresponding with the source line number of the caller
	 * and the supplied {@code objects}.
	 */
	public static void a1(Object... objects) {
		if (isEnabled) doutifier.print(1, objects);
	}

	/**
	 * Output two {@code StackTraceElement}s corresponding with the source line number of the caller
	 * and of its caller and the supplied {@code objects}.
	 */
	public static void a2(Object... objects) {
		if (isEnabled) doutifier.print(2, objects);
	}

	public static void a3(Object... objects) {
		if (isEnabled) doutifier.print(3, objects);
	}

	public static void a9(Object... objects) {
		if (isEnabled) doutifier.print(9, objects);
	}

	public static void a99(Object... objects) {
		if (isEnabled) doutifier.print(99, objects);
	}

	public static void f(String format, Object... objects) {
		if (isEnabled) doutifier.format(format, objects);
	}

	private static final Doutifier doutifier = Initializer.doutifier().clone();
	private static final boolean isEnabled = doutifier.isEnabled();
}
