package com.projectkorra.projectkorra.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MultiKeyMap<K, V> extends HashMap<K, V> {

	private final Map<Class<V>, V> classMap = new HashMap<>();

	public boolean containsKey(Class<V> classKey) {
		return this.classMap.containsKey(classKey);
	}

	public V get(Class<V> classKey) {
		return this.classMap.get(classKey);
	}

	public V getOrDefault(Class<V> key, V defaultValue) {
		return this.classMap.getOrDefault(key, defaultValue);
	}

	@Override
	public V put(K key, V value) {
		this.classMap.put((Class<V>) value.getClass(), value);
		return super.put(key, value);
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);

		if (value != null) {
			this.classMap.remove(value.getClass());
		}

		return value;
	}

	@Override
	public boolean remove(Object key, Object value) {
		this.classMap.remove(value.getClass(), value);
		return super.remove(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		Map<Class<V>, V> classMap = new HashMap<>();

		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			classMap.put((Class<V>) entry.getValue().getClass(), entry.getValue());
		}

		this.classMap.putAll(classMap);
		super.putAll(m);
	}

	@Override
	public void clear() {
		this.classMap.clear();
		super.clear();
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V replace(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}
}
