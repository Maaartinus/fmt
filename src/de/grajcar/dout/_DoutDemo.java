package de.grajcar.dout;

import java.sql.SQLException;
import java.util.Date;

import de.grajcar.fmt.FmtContext;
import de.grajcar.fmt.FmtOption;

@SuppressWarnings("boxing")
public class _DoutDemo {
	public static void main(String[] args) {
		final FmtContext context = FmtContext
				.richContext(FmtOption.LOCALIZED_NO)
				.prefer("dd.MM.yyyy=EEE", Date.class, false);
		Dout.Initializer.doutifier().context(context);
		Dout.Initializer.useSystemOut();
		Dout.a("STARTED");
		Dout.Initializer.useSystemErr(); // too late, does nothing, don't do it
		new _DoutDemo().go();
		Dout.a("DONE");
	}

	private void go() {
		Dout.a(new byte[] {1, 2, 3});
		Dout.a(new SQLException());
		Dout.a("This is 'today' using the prespecified format:", new Date());
		Dout.f("The answer on [yyyy-MM-dd] is still [jX].", new Date(), 42);
		Dout.f("This is an error: [qwerty]", 42);
	}
}
