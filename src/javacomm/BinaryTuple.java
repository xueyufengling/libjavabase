package javacomm;

import java.util.Objects;

/**
 * 键值对
 * 
 * @param <T1>
 * @param <T2>
 */
public class BinaryTuple<T1, T2> {
	public T1 val1;
	public T2 val2;

	public BinaryTuple(T1 val1, T2 val2) {
		this.val1 = val1;
		this.val2 = val2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		return (obj instanceof BinaryTuple key_tuple) && this.val1.equals(key_tuple.val1) && this.val2.equals(key_tuple.val2);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(val1) ^ Objects.hashCode(val2);
	}

	public static <T1, T2> BinaryTuple<T1, T2> of(T1 v1, T2 v2) {
		return new BinaryTuple<T1, T2>(v1, v2);
	}
}