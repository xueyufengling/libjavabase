package javacomm;

import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * 一个简单的使用两个Key索引目标值的HashMap
 * 
 * @param <K1>
 * @param <K2>
 * @param <V>
 */
public class K2HashMap<K1, K2, V> extends HashMap<BinaryTuple<K1, K2>, V> {
	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = 9215470728600536712L;

	public V get(K1 key_1, K2 key_2) {
		return super.get(new BinaryTuple<K1, K2>(key_1, key_2));
	}

	public V put(K1 key_1, K2 key_2, V value) {
		return super.put(new BinaryTuple<K1, K2>(key_1, key_2), value);
	}

	public boolean contains(K1 key_1, K2 key_2) {
		return super.containsKey(new BinaryTuple<K1, K2>(key_1, key_2));
	}

	public V delete(K1 key_1, K2 key_2) {
		return super.remove(new BinaryTuple<K1, K2>(key_1, key_2));
	}

	public V computeIfPresent(K1 key_1, K2 key_2, BiFunction<? super K1, ? super K2, ? extends V> remappingFunction) {
		return super.computeIfAbsent(new BinaryTuple<K1, K2>(key_1, key_2), (BinaryTuple<K1, K2> tuple) -> {
			return remappingFunction.apply(tuple.val1, tuple.val2);
		});
	}
}