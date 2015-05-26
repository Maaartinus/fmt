package de.grajcar.dout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import de.grajcar.fmt.FmtContext;

@Getter @Setter public class Doutifier implements Cloneable {
	Doutifier() {
		String config = System.getProperty("Dout");
		if (config==null) config = System.getenv("Dout");
		if (config==null) config = "1"; //TODO Dout default enabled
		if (config.contains("1")) destination(System.out);
		if (config.contains("2")) destination(System.err);
	}

	public Doutifier destination(PrintStream destination) {
		this.destination = destination;
		return this;
	}

	@SuppressFBWarnings("OBL") // No way to clean up the OutputStream here
	public Doutifier destination(OutputStream destination) {
		try {
			return destination(new PrintStream(destination, true, charset.name()));
		} catch (final UnsupportedEncodingException impossible) {
			throw new RuntimeException(impossible); // impossible due to how the Charset name is obtained
		}
	}

	@SuppressFBWarnings("OBL") // No way to clean up the OutputStream here
	public Doutifier destination(File destination) throws FileNotFoundException {
		return destination(new FileOutputStream(destination));
	}

	public Doutifier destination(Class<?> mainClass) throws IOException {
		return destination(fileForClass(mainClass));
	}

	@Override protected Doutifier clone() {
		try {
			return (Doutifier) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	boolean isEnabled() {
		return destination != null;
	}

	private void appendNull() {
		buf.append("null");
	}

	private void appendContinued(String s) {
		if (s==null) {
			appendNull();
			return;
		}
		for (int start=0; ; ) {
			final int end = s.indexOf('\n', start+1);
			if (end == -1) {
				buf.append(s, start, s.length());
				break;
			}
			buf.append(s, start, end);
			appendSubstituted(beforeContinuationLine);
			start = end+1;
		}
	}

	private File fileForClass(Class<?> clazz) throws IOException {
		if (clazz==null) return null;
		final Pattern pattern = Pattern.compile("(.+)\\.(\\w+)(\\$.*)?");
		final Matcher m = pattern.matcher(clazz.getName());
		if (!m.matches()) throw new RuntimeException(); // impossible
		final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		final File dir = new File(tmpdir, Dout.class.getSimpleName());
		if (!dir.mkdirs() && !dir.isDirectory()) {
			throw new IOException("Can't create directory: " + dir);
		}
		return new File(dir, m.group(1).replace(".", "-") + "-" + m.group(2)  + ".log");
	}

	void print(int depth, Object... args) {
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		final int skipCount = skipCount(stackTrace);
		depth = Math.min(depth, stackTrace.length-skipCount);
		assert depth > 0;
		print(depth, stackTrace, skipCount, args);
	}

	synchronized private void print(int depth, StackTraceElement[] stackTrace, int skipCount, Object... args) {
		appendSubstituted(beforeRecord);
		for (int i=0; i<depth; ++i) {
			final Object[] a = i==depth-1 ? args : null;
			if (i>0) appendSubstituted(beforeNextStackTraceElement);
			print(stackTrace[skipCount+i], a);
		}
		buf.append(afterRecord);
		destination.print(buf);
		destination.flush();
		buf.delete(0, buf.length());
	}

	private int skipCount(StackTraceElement[] stackTrace) {
		for (int i=1; i<stackTrace.length; ++i) {
			if (!stackTrace[i].getClassName().startsWith(THIS_PACKAGE_NAME)) return i;
		}
		if (stackTrace[stackTrace.length-1].getClassName().endsWith("Demo")) return stackTrace.length-1;
		return -1;
	}

	void format(String format, Object... args) {
		print(defaultDepth, context.fmt().format(format, args));
	}

	private void print(StackTraceElement element, Object... args) {
		append(element);
		final boolean hasArgs = args!=null && args.length>0;
		if (hasArgs) {
			buf.append(beforeFirstArgument);
			for (int i=0; ; ++i) {
				appendContinued(context.stringify(args[i]));
				if (i==args.length-1) break;
				buf.append(betweenArguments);
			}
		}
		buf.append(hasArgs ? afterLastArgument : forNoArguments);
	}

	private void appendSubstituted(String format) {
		final Formatter formatter = new Formatter(buf);
		final Matcher m = formatPattern.matcher(format);
		while (m.find()) {
			m.appendReplacement(buf, "");
			final String specifier = specifier(m.group(1));
			final String selector = m.group(2);
			final Supplier<Object> objectSupplier = substitutors.get(selector);
			if (objectSupplier==null) {
				buf.append("!!!Unexpected selector: \"" + selector + "\"!!!");
			} else if (specifier.isEmpty()) {
				buf.append(objectSupplier.get());
			} else {
				formatter.format(specifier, objectSupplier.get());
			}
		}
		m.appendTail(buf);
	}

	private String specifier(String s) {
		s = s.endsWith(":") ? s.substring(0, s.length()-1) : s + "s";
		return "%" + s;
	}

	private void append(StackTraceElement element) {
		buf.append(element==null ? "null" : element); // strange hack preventing strange NPE
	}

	private final Map<String, Supplier<Object>> substitutors = ImmutableMap.of(
			"T",
			new Supplier<Object>() {
				@Override public Object get() {
					return context.stringify(new Date());
				}
			},
			"t",
			new Supplier<Object>() {
				@SuppressWarnings("boxing") @Override public Object get() {
					return 1e-3 * (System.currentTimeMillis() - startMillis);
				}
			},
			"i",
			new Supplier<Object>() {
				@SuppressWarnings("boxing") @Override public Object get() {
					return Thread.currentThread().getId();
				}
			},
			"n",
			new Supplier<Object>() {
				@Override public Object get() {
					return Thread.currentThread().getName();
				}
			});

	private static final String THIS_PACKAGE_NAME = Doutifier.class.getPackage().getName();
	private final static Pattern formatPattern = Pattern.compile("\\[(-?[.\\d]*|[^\\[\\]]+:)([^\\[\\]:]*)]");
	private final static long startMillis = System.currentTimeMillis();

	@NonNull private FmtContext context = FmtContext.newRichContext();

	private String beforeRecord = "[07.3f:t] ";
	private String afterRecord = "\n";

	private String beforeFirstArgument = "\n + ";
	private String betweenArguments = " ";
	private String afterLastArgument = "\n";
	private String forNoArguments ="\n";

	private String beforeNextStackTraceElement = " ~      ";
	private String beforeContinuationLine = "\n - ";

	private int defaultDepth = 1;

	private Charset charset = Charset.forName("UTF-8");

	@Nullable private PrintStream destination;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)  private final StringBuffer buf = new StringBuffer();
}
