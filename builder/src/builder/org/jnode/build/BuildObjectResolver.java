/*
 * $Id$
 */
package org.jnode.build;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.vm.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BuildObjectResolver extends ObjectResolver {

	private final NativeStream os;
	private final AbstractBootImageBuilder builder;

	public BuildObjectResolver(NativeStream os, AbstractBootImageBuilder builder) {
		this.os = os;
		this.builder = builder;
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#add(org.jnode.vm.Address, int)
	 */
	public Address add(Address address, int offset) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf(java.lang.Object)
	 */
	public Address addressOf(Object object) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf32(java.lang.Object)
	 */
	public int addressOf32(Object object) {
		final NativeStream.ObjectRef ref = os.getObjectRef(object);
		try {
			if (!ref.isResolved()) {
				builder.emitObject(os, object);
			}
			final int offset = (int) os.getBaseAddr() + ref.getOffset();
			return offset;
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Unresolved object ref", ex);
		} catch (UnresolvedObjectRefException ex) {
			throw new RuntimeException("Unresolved object ref", ex);
		}
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf64(java.lang.Object)
	 */
	public long addressOf64(Object object) {
		final NativeStream.ObjectRef ref = os.getObjectRef(object);
		try {
			if (!ref.isResolved()) {
				builder.emitObject(os, object);
			}
			final long offset = os.getBaseAddr() + ref.getOffset();
			return offset;
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Unresolved object ref", ex);
		} catch (UnresolvedObjectRefException ex) {
			throw new RuntimeException("Unresolved object ref", ex);
		}
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOfArrayData(java.lang.Object)
	 */
	public Address addressOfArrayData(Object array) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt(org.jnode.vm.Address)
	 */
	public Object objectAt(Address ptr) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt32(int)
	 */
	public Object objectAt32(int ptr) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt64(long)
	 */
	public Object objectAt64(long ptr) {
		throw new RuntimeException("Not implemented");
	}
}
