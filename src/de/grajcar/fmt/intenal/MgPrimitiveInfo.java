package de.grajcar.fmt.intenal;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;

import lombok.Getter;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;

@Beta // DO NOT USE
@Getter public enum MgPrimitiveInfo {
	BYTE(1, byte.class, Byte.class, 'B') {
		@Override public byte[] newArray(int length) {
			return new byte[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((byte[])o).clone();
		}
	},
	SHORT(2, short.class, Short.class, 'S') {
		@Override public short[] newArray(int length) {
			return new short[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((short[])o).clone();
		}
	},
	INT(4, int.class, Integer.class, 'I') {
		@Override public int[] newArray(int length) {
			return new int[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((int[])o).clone();
		}
	},
	LONG(8, long.class, Long.class, 'J') {
		@Override public long[] newArray(int length) {
			return new long[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((long[])o).clone();
		}
	},
	FLOAT(4, float.class, Float.class, 'F') {
		@Override public float[] newArray(int length) {
			return new float[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((float[])o).clone();
		}
	},
	DOUBLE(8, double.class, Double.class, 'D') {
		@Override public double[] newArray(int length) {
			return new double[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((double[])o).clone();
		}
	},
	CHAR(2, char.class, Character.class, 'C') {
		@Override public char[] newArray(int length) {
			return new char[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((char[])o).clone();
		}
	},
	BOOLEAN(1, boolean.class, Boolean.class, 'Z') {
		@Override public boolean[] newArray(int length) {
			return new boolean[length];
		}

		@Override Object cloneArraySpecial(Object o) {
			return ((boolean[])o).clone();
		}
	},
	;

	private MgPrimitiveInfo(int byteLength, Class<?> primitiveClass, Class<?> wrapperClass, char classDescriptor) {
		this.byteLength = byteLength;
		this.primitiveClass = primitiveClass;
		this.wrapperClass = wrapperClass;
		this.jvmClassDescriptor = classDescriptor;
	}

	private final static ImmutableMap<Object, MgPrimitiveInfo> map;
	static {
		final ImmutableMap.Builder<Object, MgPrimitiveInfo> builder = ImmutableMap.builder();
		for (final MgPrimitiveInfo e : values()) {
			builder.put(e.primitiveClass(), e);
			builder.put(e.wrapperClass(), e);
			builder.put(e.primitiveClass().getName(), e);
			builder.put(e.wrapperClass().getName(), e);
		}
		map = builder.build();
	}

	@Nullable public static MgPrimitiveInfo of(@Nullable Class<?> clazz) {
		return map.get(clazz);
	}

	@Nullable public static MgPrimitiveInfo of(@Nullable String className) {
		return map.get(className);
	}

	public static Class<?> unwrap(Class<?> clazz) {
		return map.get(clazz).primitiveClass();
	}

	public static Class<?> wrap(Class<?> clazz) {
		return map.get(clazz).wrapperClass();
	}

	public abstract Object newArray(int length);

	public Object cloneArray(Object o) {
		final Class<?> componentClass = o.getClass().getComponentType();
		final MgPrimitiveInfo e = map.get(componentClass);
		checkArgument(e!=null, "Expected a primitive array, got %s", o);
		return e.cloneArray(o);
	}

	public boolean is(Class<?> clazz) {
		return clazz == primitiveClass || clazz == wrapperClass;
	}

	public boolean isIntLike() {
		return ordinal() < 4;
	}

	abstract Object cloneArraySpecial(Object o);

	/** The minimum memory requirements of the type in bytes, rounded up. */
	private final int byteLength;
	private final Class<?> primitiveClass;
	private final Class<?> wrapperClass;
	private final char jvmClassDescriptor;
}
