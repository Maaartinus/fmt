package de.grajcar.fmt.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

final class DateHelper {
	@RequiredArgsConstructor @EqualsAndHashCode private static final class CacheKey {
		private final Thread thread;
		private final String format;
		private final Locale locale;
		private final TimeZone timeZone;
	}

	static String format(String format, Locale locale, TimeZone timeZone, Date date) {
		return get(format, locale, timeZone).format(date);
	}

	private static SimpleDateFormat get(String format, Locale locale, TimeZone timeZone) {
		return loadingCache.getUnchecked(new CacheKey(Thread.currentThread(), format, locale, timeZone));
	}

	private static final LoadingCache<CacheKey, SimpleDateFormat> loadingCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<CacheKey, SimpleDateFormat>() {
				@Override public SimpleDateFormat load(CacheKey key) {
					final SimpleDateFormat result = new SimpleDateFormat(key.format, key.locale);
					result.setTimeZone(key.timeZone);
					return result;
				}
			});
}
