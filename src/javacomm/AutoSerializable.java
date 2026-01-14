package javacomm;

/**
 * 自动序列化接口，提供了在自动序列化和反序列化时执行的操作，<br>
 * 实际上只要是java.io.Serializable即可托管自动序列化，只是java.io.Serializable无法在序列化或反序列化时执行操作。
 */
public interface AutoSerializable {
	/**
	 * 对象被从序列化列表中拉取时需要执行的操作
	 */
	default public void onPull(String ref_name) {

	}

	default public void onPush(String ref_name) {

	}

	/**
	 * 反序列化时要执行的操作
	 * 
	 * @param ref_name 该对象（或对象的成员、成员的成员...）对应的引用名称
	 */
	default public void onDeserialize(String ref_name) {

	}
}
