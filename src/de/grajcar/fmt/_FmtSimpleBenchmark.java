package de.grajcar.fmt;

import static com.google.common.base.Verify.verify;

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import de.grajcar.dout.Dout;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

public final class _FmtSimpleBenchmark {
	enum MyOption {
		DATE_USING_TO_STRING {
			@Override Object newObject(Random random) {
				return new Date(1_000_000_000_000L + 1234L * random.nextInt(1_000_000_000));
			}
		},
		DATE {
			@Override Object newObject(Random random) {
				return new Date(1_000_000_000_000L + 1234L * random.nextInt(1_000_000_000));
			}
		},
		LONG {
			@Override Object newObject(Random random) {
				return Long.valueOf(random.nextLong() & 0xFFFFFFFF);
			}
		},
		STRING {
			@Override Object newObject(Random random) {
				return String.valueOf(random.nextLong() & 0xFFFFFFFF);
			}
		},
		MIX {
			@Override Object newObject(Random random) {
				final MyOption[] values = MyOption.values();
				return values[random.nextInt(values.length-1)].newObject(random);
			}
		},
		;

		abstract Object newObject(Random random);
	}

	public static void main(String[] args) {
		Dout.a("STARTED");
		new _FmtSimpleBenchmark().test();
		CaliperMain.main(_FmtSimpleBenchmark.class, new String[] {"-i", "runtime"});
	}

	void test() {
		for (final MyOption option : MyOption.values()) {
			this.option = option;
			setUp();
			for (final Object o : objects) {
				final String expected = o.toString();
				final Fmt fmt = context.fmt();
				final String actual = fmt.format("[]", o).take();
				verify(actual.equals(expected), "\n _actual_='%s'\n expected='%s'", actual, expected);
			}
		}
	}

	@BeforeExperiment void setUp() {
		final Random random = new Random(0);
		for (int i=0; i<objects.length; ++i) {
			final List<Object> list = Lists.newArrayList();
			for (int j=0; j<10; ++j) list.add(option.newObject(random));
			objects[i] = list;
		}
		if (option==MyOption.DATE_USING_TO_STRING) {
			context = context.prefer("%", Date.class, false);
		} else {
			context = context.prefer("EEE MMM dd HH:mm:ss zzz yyyy", Date.class, false);
		}
	}

	@Benchmark public int timeFmt() {
		int result = 0;
		final Fmt fmt = context.fmt();
		for (final Object o : objects) {
			final String s = fmt.format("[]", o).take();
			result += s.length();
		}
		return result;
	}

	@Benchmark public int timeToString() {
		int result = 0;
		for (final Object o : objects) {
			final String s = o.toString();
			result += s.length();
		}
		return result;
	}


	private FmtContext context = FmtContext.richContext(FmtOption.LOCALIZED_NO, FmtOption.ON_ERROR_THROWING);

	@Param private MyOption option;

	private final Object[] objects = new Object[1000];
}
