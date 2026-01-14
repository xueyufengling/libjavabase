package javacomm;

public interface Recoverable<Derived> {

	public Derived redirect();

	public Derived recovery();

	public Derived asPrimary();
}
