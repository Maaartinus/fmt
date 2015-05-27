package de.grajcar.fmt;

import static com.google.common.base.Verify.verify;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import lombok.RequiredArgsConstructor;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.SkipThisScenarioException;
import com.google.caliper.runner.CaliperMain;
import junit.framework.Assert;

@SuppressWarnings("boxing") public final class _FmtBenchmark {
	@RequiredArgsConstructor enum FormatType {
		PLAIN("First=[], second=[]", "First=[%s], second=[%s]", "First=%s, second=%s"),
		HEX("First=[x], second=[pX]", "First=[%x], second=[%02X]", "First=%x, second=%02X"),
		DATE("First=[sdf%yy-MM-dd], second=[sdf%yyyy-MM-dd]", null,
				"First=%1$ty-%1$tm-%1$td, second=%2$tY-%2$tm-%2$td"),
				;
		private final String fmtFormat;
		private final String delegatingFormat;
		private final String sfFormat;
	}

	public static void main(String[] args) {
		CaliperMain.main(_FmtBenchmark.class, args);
	}

	@Benchmark public int timeFmt(int reps) {
		int result = 0;
		final String format = formatType.fmtFormat;
		final Fmt fmt = context.fmt();
		while (reps-->0) {
			for (final Object n : objects) {
				result += fmt.format(format, n, n).take().length();
			}
		}
		return result;
	}

	@Benchmark public int timeFmtAlloc(int reps) {
		int result = 0;
		final String format = formatType.fmtFormat;
		while (reps-->0) {
			for (final Object n : objects) {
				result += context.fmt().format(format, n, n).toString().length();
			}
		}
		return result;
	}

	@Benchmark public int timeDelegating(int reps) {
		int result = 0;
		final String format = formatType.delegatingFormat;
		if (format==null) throw new SkipThisScenarioException();
		final Fmt fmt = context.fmt();
		while (reps-->0) {
			for (final Object n : objects) {
				result += fmt.format(format, n, n).take().length();
			}
		}
		return result;
	}

	@Benchmark public int timeManuallyDelegating(int reps) {
		int result = 0;
		final String format = formatType.delegatingFormat;
		if (format==null) throw new SkipThisScenarioException();
		final String[] split = format.split("[\\[\\]]");
		verify(split.length==4);
		final StringBuilder sb = new StringBuilder();
		while (reps-->0) {
			for (final Object n : objects) {
				sb.append(split[0]).append(String.format(split[1], n)).append(split[2]).append(String.format(split[3], n));
				result += sb.length();
				sb.delete(0, sb.length());
			}
		}
		return result;
	}

	@Benchmark public int timeManuallyDelegatingAlloc(int reps) {
		int result = 0;
		final String format = formatType.delegatingFormat;
		if (format==null) throw new SkipThisScenarioException();
		final String[] split = format.split("[\\[\\]]");
		verify(split.length==4);
		while (reps-->0) {
			for (final Object n : objects) {
				final String s = split[0] + String.format(split[1], n) + split[2] + String.format(split[3], n);
				result += s.length();
			}
		}
		return result;
	}

	@Benchmark public int timeStringFormat(int reps) {
		int result = 0;
		final String format = formatType.sfFormat;
		if (format==null) throw new SkipThisScenarioException();
		while (reps-->0) {
			for (final Object n : objects) {
				result += String.format(format, n, n).length();
			}
		}
		return result;
	}

	@Benchmark public int timeDirect(int reps) {
		int result = 0;
		switch (formatType) {
			case PLAIN:
				while (reps-->0) {
					for (final Object n : objects) {
						result += ("First=" + n + ", second=" + n).length();
					}
				}
				return result;
			case HEX:
				while (reps-->0) {
					for (final Object n : objects) {
						final String s = "First="
								+ Integer.toHexString((byte) n)
								+ ", second="
								+ Integer.toHexString((byte) n).toUpperCase();
						result += s.length();
					}
				}
				return result;
			case DATE:
				final SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
				final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
				while (reps-->0) {
					for (final Object n : objects) {
						final String s = "First="
								+ sdf1.format((Date) n)
								+ ", second="
								+ sdf2.format((Date) n).toUpperCase();
						result += s.length();
					}
				}
				return result;
		}
		throw new RuntimeException("unreachable");
	}

	@BeforeExperiment public void setup() {
		final Random random = new Random();
		final long now = System.currentTimeMillis();
		for (int i=0; i<bytes.length; ++i) {
			bytes[i] = (byte) random.nextInt();
			dates[i] = new Date(now + random.nextInt());
		}
		switch (formatType) {
			case HEX:
			case PLAIN: objects = bytes; break;
			case DATE: objects = dates; break;
		}
		test();
	}

	private void test() {
		final Object[] args = {(byte) 12, (byte) -34};
		for (final FormatType ft : FormatType.values()) {
			if (ft == FormatType.DATE) continue;
			final String expected = String.format(ft.sfFormat, args);
			final String actual = context.fmt().format(ft.fmtFormat, args).toString();
			final String delegated = context.fmt().format(ft.delegatingFormat, args).toString();
			Assert.assertEquals(expected, delegated);
			Assert.assertEquals(expected, actual);
		}
	}

	@Param
	private FormatType formatType;

	private Object[] objects;
	private final Object[] bytes = new Object[100];
	private final Object[] dates = new Object[100];
	private static final FmtContext context = FmtContext.newRichContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);
}
