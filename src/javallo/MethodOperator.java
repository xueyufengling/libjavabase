package javallo;

import org.objectweb.asm.MethodVisitor;

import javallo.ByteCodeManipulator.MethodInfo;

public interface MethodOperator {
	public void modify(MethodInfo method_info, MethodVisitor method_visitor);
}
