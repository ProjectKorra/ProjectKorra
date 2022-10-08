package com.projectkorra.projectkorra.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>ReflectionUtils</b>
 * <p>
 * This class provides useful methods which makes dealing with reflection much
 * easier, especially when working with Bukkit
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following
 * conditions:
 * <ul>
 * <li>Don't claim this class as your own
 * <li>Don't remove this disclaimer
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a
 * published project</i>
 *
 * @author DarkBlade12
 * @version 1.1
 */
public final class ReflectionHandler {
	// Prevent accidental construction.
	private ReflectionHandler() {}

	/**
	 * Returns the constructor of a given class with the given parameter types
	 *
	 * @param clazz Target class
	 * @param parameterTypes Parameter types of the desired constructor
	 * @return The constructor of the target class with the specified parameter
	 *         types
	 * @throws NoSuchMethodException If the desired constructor with the
	 *             specified parameter types cannot be found
	 * @see DataType
	 * @see DataType#getPrimitive(Class[])
	 * @see DataType#compare(Class[], Class[])
	 */
	public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameterTypes) throws NoSuchMethodException {
		final Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
		for (final Constructor<?> constructor : clazz.getConstructors()) {
			if (!DataType.compare(DataType.getPrimitive(constructor.getParameterTypes()), primitiveTypes)) {
				continue;
			}
			return constructor;
		}
		throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
	}

	/**
	 * Returns the constructor of a desired class with the given parameter types
	 *
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param parameterTypes Parameter types of the desired constructor
	 * @return The constructor of the desired target class with the specified
	 *         parameter types
	 * @throws NoSuchMethodException If the desired constructor with the
	 *             specified parameter types cannot be found
	 * @throws ClassNotFoundException ClassNotFoundException If the desired
	 *             target class with the specified name and package cannot be
	 *             found
	 * @see #getConstructor(Class, Class...)
	 */
	public static Constructor<?> getConstructor(final String className, final PackageType packageType, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
		return getConstructor(packageType.getClass(className), parameterTypes);
	}

	/**
	 * Returns an instance of a class with the given arguments
	 *
	 * @param clazz Target class
	 * @param arguments Arguments which are used to construct an object of the
	 *            target class
	 * @return The instance of the target class with the specified arguments
	 * @throws InstantiationException If you cannot create an instance of the
	 *             target class due to certain circumstances
	 * @throws IllegalAccessException If the desired constructor cannot be
	 *             accessed due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not
	 *             match the parameter types of the constructor (this should not
	 *             occur since it searches for a constructor with the types of
	 *             the arguments)
	 * @throws InvocationTargetException If the desired constructor cannot be
	 *             invoked
	 * @throws NoSuchMethodException If the desired constructor with the
	 *             specified arguments cannot be found
	 */
	public static Object instantiateObject(final Class<?> clazz, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
	}

	/**
	 * Returns an instance of a desired class with the given arguments
	 *
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param arguments Arguments which are used to construct an object of the
	 *            desired target class
	 * @return The instance of the desired target class with the specified
	 *         arguments
	 * @throws InstantiationException If you cannot create an instance of the
	 *             desired target class due to certain circumstances
	 * @throws IllegalAccessException If the desired constructor cannot be
	 *             accessed due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not
	 *             match the parameter types of the constructor (this should not
	 *             occur since it searches for a constructor with the types of
	 *             the arguments)
	 * @throws InvocationTargetException If the desired constructor cannot be
	 *             invoked
	 * @throws NoSuchMethodException If the desired constructor with the
	 *             specified arguments cannot be found
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #instantiateObject(Class, Object...)
	 */
	public static Object instantiateObject(final String className, final PackageType packageType, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		return instantiateObject(packageType.getClass(className), arguments);
	}

	/**
	 * Returns a method of a class with the given parameter types
	 *
	 * @param clazz Target class
	 * @param methodName Name of the desired method
	 * @param parameterTypes Parameter types of the desired method
	 * @return The method of the target class with the specified name and
	 *         parameter types
	 * @throws NoSuchMethodException If the desired method of the target class
	 *             with the specified name and parameter types cannot be found
	 * @see DataType#getPrimitive(Class[])
	 * @see DataType#compare(Class[], Class[])
	 */
	public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
		final Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
		for (final Method method : clazz.getMethods()) {
			if (!method.getName().equals(methodName) || !DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
				continue;
			}
			return method;
		}
		throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
	}

	/**
	 * Returns a method of a desired class with the given parameter types
	 *
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param methodName Name of the desired method
	 * @param parameterTypes Parameter types of the desired method
	 * @return The method of the desired target class with the specified name
	 *         and parameter types
	 * @throws NoSuchMethodException If the desired method of the desired target
	 *             class with the specified name and parameter types cannot be
	 *             found
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #getMethod(Class, String, Class...)
	 */
	public static Method getMethod(final String className, final PackageType packageType, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
		return getMethod(packageType.getClass(className), methodName, parameterTypes);
	}

	/**
	 * Invokes a method on an object with the given arguments
	 *
	 * @param instance Target object
	 * @param methodName Name of the desired method
	 * @param arguments Arguments which are used to invoke the desired method
	 * @return The result of invoking the desired method on the target object
	 * @throws IllegalAccessException If the desired method cannot be accessed
	 *             due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not
	 *             match the parameter types of the method (this should not
	 *             occur since it searches for a method with the types of the
	 *             arguments)
	 * @throws InvocationTargetException If the desired method cannot be invoked
	 *             on the target object
	 * @throws NoSuchMethodException If the desired method of the class of the
	 *             target object with the specified name and arguments cannot be
	 *             found
	 * @see #getMethod(Class, String, Class...)
	 * @see DataType#getPrimitive(Object[])
	 */
	public static Object invokeMethod(final Object instance, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
	}

	/**
	 * Invokes a method of the target class on an object with the given
	 * arguments
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param methodName Name of the desired method
	 * @param arguments Arguments which are used to invoke the desired method
	 * @return The result of invoking the desired method on the target object
	 * @throws IllegalAccessException If the desired method cannot be accessed
	 *             due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not
	 *             match the parameter types of the method (this should not
	 *             occur since it searches for a method with the types of the
	 *             arguments)
	 * @throws InvocationTargetException If the desired method cannot be invoked
	 *             on the target object
	 * @throws NoSuchMethodException If the desired method of the target class
	 *             with the specified name and arguments cannot be found
	 * @see #getMethod(Class, String, Class...)
	 * @see DataType#getPrimitive(Object[])
	 */
	public static Object invokeMethod(final Object instance, final Class<?> clazz, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
	}

	/**
	 * Invokes a method of a desired class on an object with the given arguments
	 *
	 * @param instance Target object
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param methodName Name of the desired method
	 * @param arguments Arguments which are used to invoke the desired method
	 * @return The result of invoking the desired method on the target object
	 * @throws IllegalAccessException If the desired method cannot be accessed
	 *             due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not
	 *             match the parameter types of the method (this should not
	 *             occur since it searches for a method with the types of the
	 *             arguments)
	 * @throws InvocationTargetException If the desired method cannot be invoked
	 *             on the target object
	 * @throws NoSuchMethodException If the desired method of the desired target
	 *             class with the specified name and arguments cannot be found
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #invokeMethod(Object, Class, String, Object...)
	 */
	public static Object invokeMethod(final Object instance, final String className, final PackageType packageType, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
	}

	/**
	 * Returns a field of the target class with the given name
	 *
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The field of the target class with the specified name
	 * @throws NoSuchFieldException If the desired field of the given class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 */
	public static Field getField(final Class<?> clazz, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException {
		final Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
		field.setAccessible(true);
		return field;
	}

	/**
	 * Returns a field of a desired class with the given name
	 *
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The field of the desired target class with the specified name
	 * @throws NoSuchFieldException If the desired field of the desired class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #getField(Class, boolean, String)
	 */
	public static Field getField(final String className, final PackageType packageType, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		return getField(packageType.getClass(className), declared, fieldName);
	}

	/**
	 * Returns the value of a field of the given class of an object
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The value of field of the target object
	 * @throws IllegalArgumentException If the target object does not feature
	 *             the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #getField(Class, boolean, String)
	 */
	public static Object getValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return getField(clazz, declared, fieldName).get(instance);
	}

	/**
	 * Returns the value of a field of a desired class of an object
	 *
	 * @param instance Target object
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The value of field of the target object
	 * @throws IllegalArgumentException If the target object does not feature
	 *             the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the desired class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #getValue(Object, Class, boolean, String)
	 */
	public static Object getValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		return getValue(instance, packageType.getClass(className), declared, fieldName);
	}

	/**
	 * Returns the value of a field with the given name of an object
	 *
	 * @param instance Target object
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The value of field of the target object
	 * @throws IllegalArgumentException If the target object does not feature
	 *             the desired field (should not occur since it searches for a
	 *             field with the given name in the class of the object)
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target object
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #getValue(Object, Class, boolean, String)
	 */
	public static Object getValue(final Object instance, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return getValue(instance, instance.getClass(), declared, fieldName);
	}

	/**
	 * Sets the value of a field of the given class of an object
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @param value New value
	 * @throws IllegalArgumentException If the type of the value does not match
	 *             the type of the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #getField(Class, boolean, String)
	 */
	public static void setValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		getField(clazz, declared, fieldName).set(instance, value);
	}

	/**
	 * Sets the value of a field of a desired class of an object
	 *
	 * @param instance Target object
	 * @param className Name of the desired target class
	 * @param packageType Package where the desired target class is located
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @param value New value
	 * @throws IllegalArgumentException If the type of the value does not match
	 *             the type of the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the desired class
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @throws ClassNotFoundException If the desired target class with the
	 *             specified name and package cannot be found
	 * @see #setValue(Object, Class, boolean, String, Object)
	 */
	public static void setValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		setValue(instance, packageType.getClass(className), declared, fieldName, value);
	}

	/**
	 * Sets the value of a field with the given name of an object
	 *
	 * @param instance Target object
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @param value New value
	 * @throws IllegalArgumentException If the type of the value does not match
	 *             the type of the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target object
	 *             cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #setValue(Object, Class, boolean, String, Object)
	 */
	public static void setValue(final Object instance, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		setValue(instance, instance.getClass(), declared, fieldName, value);
	}

	/**
	 * Represents an enumeration of dynamic packages of NMS and CraftBukkit
	 * <p>
	 * This class is part of the <b>ReflectionUtils</b> and follows the same
	 * usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.0
	 */
	public enum PackageType {
		MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()), CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()), CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"), CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"), CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"), CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"), CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"), CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"), CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"), CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"), CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"), CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"), CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"), CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"), CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"), CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"), CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"), CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"), CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"), CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

		private final String path;

		/**
		 * Construct a new package type
		 *
		 * @param path Path of the package
		 */
		private PackageType(final String path) {
			this.path = path;
		}

		/**
		 * Construct a new package type
		 *
		 * @param parent Parent package of the package
		 * @param path Path of the package
		 */
		private PackageType(final PackageType parent, final String path) {
			this(parent + "." + path);
		}

		/**
		 * Returns the path of this package type
		 *
		 * @return The path
		 */
		public String getPath() {
			return this.path;
		}

		/**
		 * Returns the class with the given name
		 *
		 * @param className Name of the desired class
		 * @return The class with the specified name
		 * @throws ClassNotFoundException If the desired class with the
		 *             specified name and package cannot be found
		 */
		public Class<?> getClass(final String className) throws ClassNotFoundException {
			return Class.forName(this + "." + className);
		}

		// Override for convenience.
		@Override
		public String toString() {
			return this.path;
		}

		/**
		 * Returns the version of your server
		 *
		 * @return The server version
		 */
		public static String getServerVersion() {
			return Bukkit.getServer().getClass().getPackage().getName().substring(23);
		}
	}

	/**
	 * Represents an enumeration of Java data types with corresponding classes
	 * <p>
	 * This class is part of the <b>ReflectionUtils</b> and follows the same
	 * usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.0
	 */
	public enum DataType {
		BYTE(byte.class, Byte.class), SHORT(short.class, Short.class), INTEGER(int.class, Integer.class), LONG(long.class, Long.class), CHARACTER(char.class, Character.class), FLOAT(float.class, Float.class), DOUBLE(double.class, Double.class), BOOLEAN(boolean.class, Boolean.class);

		private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<Class<?>, DataType>();
		private final Class<?> primitive;
		private final Class<?> reference;

		// Initialize map for quick class lookup.
		static {
			for (final DataType type : values()) {
				CLASS_MAP.put(type.primitive, type);
				CLASS_MAP.put(type.reference, type);
			}
		}

		/**
		 * Construct a new data type
		 *
		 * @param primitive Primitive class of this data type
		 * @param reference Reference class of this data type
		 */
		private DataType(final Class<?> primitive, final Class<?> reference) {
			this.primitive = primitive;
			this.reference = reference;
		}

		/**
		 * Returns the primitive class of this data type
		 *
		 * @return The primitive class
		 */
		public Class<?> getPrimitive() {
			return this.primitive;
		}

		/**
		 * Returns the reference class of this data type
		 *
		 * @return The reference class
		 */
		public Class<?> getReference() {
			return this.reference;
		}

		/**
		 * Returns the data type with the given primitive/reference class
		 *
		 * @param clazz Primitive/Reference class of the data type
		 * @return The data type
		 */
		public static DataType fromClass(final Class<?> clazz) {
			return CLASS_MAP.get(clazz);
		}

		/**
		 * Returns the primitive class of the data type with the given reference
		 * class
		 *
		 * @param clazz Reference class of the data type
		 * @return The primitive class
		 */
		public static Class<?> getPrimitive(final Class<?> clazz) {
			final DataType type = fromClass(clazz);
			return type == null ? clazz : type.getPrimitive();
		}

		/**
		 * Returns the reference class of the data type with the given primitive
		 * class
		 *
		 * @param clazz Primitive class of the data type
		 * @return The reference class
		 */
		public static Class<?> getReference(final Class<?> clazz) {
			final DataType type = fromClass(clazz);
			return type == null ? clazz : type.getReference();
		}

		/**
		 * Returns the primitive class array of the given class array
		 *
		 * @param classes Given class array
		 * @return The primitive class array
		 */
		public static Class<?>[] getPrimitive(final Class<?>[] classes) {
			final int length = classes == null ? 0 : classes.length;
			final Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getPrimitive(classes[index]);
			}
			return types;
		}

		/**
		 * Returns the reference class array of the given class array
		 *
		 * @param classes Given class array
		 * @return The reference class array
		 */
		public static Class<?>[] getReference(final Class<?>[] classes) {
			final int length = classes == null ? 0 : classes.length;
			final Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getReference(classes[index]);
			}
			return types;
		}

		/**
		 * Returns the primitive class array of the given object array
		 *
		 * @param objects Given object array
		 * @return The primitive class array
		 */
		public static Class<?>[] getPrimitive(final Object[] objects) {
			final int length = objects == null ? 0 : objects.length;
			final Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getPrimitive(objects[index].getClass());
			}
			return types;
		}

		/**
		 * Returns the reference class array of the given object array
		 *
		 * @param objects Given object array
		 * @return The reference class array
		 */
		public static Class<?>[] getReference(final Object[] objects) {
			final int length = objects == null ? 0 : objects.length;
			final Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getReference(objects[index].getClass());
			}
			return types;
		}

		/**
		 * Compares two class arrays on equivalence
		 *
		 * @param primary Primary class array
		 * @param secondary Class array which is compared to the primary array
		 * @return Whether these arrays are equal or not
		 */
		public static boolean compare(final Class<?>[] primary, final Class<?>[] secondary) {
			if (primary == null || secondary == null || primary.length != secondary.length) {
				return false;
			}
			for (int index = 0; index < primary.length; index++) {
				final Class<?> primaryClass = primary[index];
				final Class<?> secondaryClass = secondary[index];
				if (primaryClass.equals(secondaryClass) || primaryClass.isAssignableFrom(secondaryClass)) {
					continue;
				}
				return false;
			}
			return true;
		}
	}
}
