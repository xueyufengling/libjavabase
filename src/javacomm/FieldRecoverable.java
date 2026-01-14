package javacomm;

import java.util.ArrayList;

import javallo.BaseClass;

public interface FieldRecoverable<Derived> extends Recoverable<Derived>, BaseClass<FieldRecoverable.References<Derived>> {

	public default void construct(Object obj, String fieldName, Derived target) {
		this.construct(References.class, new Class<?>[] { FieldRecoverable.class }, this);
	}

	@SuppressWarnings("unchecked")
	public default Derived link(Object obj, String fieldName, Derived dest) {
		this.definition().link(obj, fieldName, dest);
		return (Derived) this;
	}

	public class References<Derived> extends BaseClass.Definition<FieldRecoverable<Derived>> implements Recoverable<Derived> {
		protected FieldRecoverable<Derived> this_;

		ArrayList<FieldReference> references = new ArrayList<>();

		public References(FieldRecoverable<Derived> this_) {
			this.this_ = this_;
		}

		@SuppressWarnings("unchecked")
		public final Derived link(Object obj, String fieldName, Object target) {
			FieldReference ref = FieldReference.of(obj, fieldName, target);
			if (ref != null)
				references.add(ref);
			return (Derived) this_;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Derived redirect() {
			for (FieldReference ref : references)
				ref.redirect();
			return (Derived) this_;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Derived recovery() {
			for (FieldReference ref : references)
				ref.recovery();
			return (Derived) this_;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Derived asPrimary() {
			for (FieldReference ref : references)
				ref.asPrimary();
			return (Derived) this_;
		}
	}

	public default Derived redirect() {
		return this.definition().redirect();
	}

	public default Derived recovery() {
		return this.definition().recovery();
	}

	public default Derived asPrimary() {
		return this.definition().asPrimary();
	}
}
