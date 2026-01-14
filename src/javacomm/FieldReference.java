package javacomm;

import java.lang.reflect.Field;

import javallo.ObjectManipulator;
import javallo.Reflection;

/**
 * 用于重定向字段引用的封装工具类
 */
public class FieldReference implements Recoverable<FieldReference> {
	private Object obj;
	private Field field;
	private Object orig;
	private Object dest;

	private FieldReference(Object obj, String fieldName, Object dest) {
		this.obj = obj;
		this.field = Reflection.getField(obj, fieldName);
		this.dest = dest;
		this.asPrimary();
	}

	private FieldReference(Object obj, String fieldName) {
		this(obj, fieldName, null);
	}

	/**
	 * 将当前值设置为原先值，可提供recovery()恢复到该值
	 * 
	 * @return
	 */
	public FieldReference asPrimary() {
		orig = ObjectManipulator.access(obj, field);
		return this;
	}

	public FieldReference redirect(Object redirectRefValue) {
		ObjectManipulator.setObject(obj, field, redirectRefValue);
		return this;
	}

	public FieldReference redirect() {
		return redirect(dest);
	}

	/**
	 * 恢复到最开始的值
	 * 
	 * @return
	 */
	public final FieldReference recovery() {
		return redirect(orig);
	}

	public final FieldReference asPrimary(Object primaryValue) {
		this.orig = primaryValue;
		return this;
	}

	public final FieldReference redirectTo(Object redirectRefValue) {
		this.dest = redirectRefValue;
		return this;
	}

	public final Object realtimeValue() {
		return ObjectManipulator.access(obj, field);
	}

	public static final FieldReference of(Object refObjBase, String refName, Object redirectRefValue) {
		if (refObjBase != null && refName != null)
			return new FieldReference(refObjBase, refName, redirectRefValue);
		return null;
	}

	public static final FieldReference of(Object refObjBase, String refName) {
		return of(refObjBase, refName, null);
	}
}
