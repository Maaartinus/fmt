package de.grajcar.fmt.demo;

import java.io.IOException;

class DemoExceptionMaker {
	public static Exception newException() {
		try {
			f();
		} catch (final Exception e) {
			return e;
		}
		throw new AssertionError();
	}

	private static void f() throws Exception {
		try {
			g();
		} catch (final Exception e) {
			helper(e);
		}
	}

	private static void g() throws Exception {
		try {
			h();
		} catch (final Exception e) {
			helper(e);
		}
	}

	private static void h() throws Exception {
		throw new IOException();
	}

	private static void helper(Exception cause) throws Exception {
		throw new Exception(cause);
	}
}
