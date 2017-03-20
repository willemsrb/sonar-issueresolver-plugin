package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ReflectionTestUtils {

	public static <T> T build(final Class<T> clazz, Object... fields) {
		T instance = newInstance(clazz);

		if (fields != null) {
			for (int i = 0; i+1 < fields.length; i = i + 2) {
				setField(instance, (String)fields[i], fields[i+1]);
			}
		}
		return instance;
	}

	public static <T> T newInstance(final Class<T> clazz) {
		try {
			final Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);

			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void setField(final Object object, final String name, final Object value) {
		try {
			final Field field = findField(object.getClass(), name);
			field.setAccessible(true);
			field.set(object, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Field findField(final Class<?> clazz, final String name) throws ReflectiveOperationException {
		Class<?> theClazz = clazz;
		while (theClazz != null) {
			try {
				return theClazz.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				theClazz = theClazz.getSuperclass();
			}
		}

		throw new NoSuchFieldException(name);
	}
}
