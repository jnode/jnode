/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author epr
 */
public final class VmNormalClass extends VmClassType {

	/** The offsets of all reference variables in this class */
	private int[] referenceOffsets;
	/** The size (in bytes) of an instance of this class */
	private int objectSize = 0;

	/**
	 * @param name
	 * @param superClass
	 * @param loader
	 * @param primitive
	 * @param typeSize
	 */
	protected VmNormalClass(
		String name,
		VmNormalClass superClass,
		AbstractVmClassLoader loader,
		boolean primitive,
		int typeSize) {
		super(name, superClass, loader, primitive, typeSize);
		testClassType();
	}

	/**
	 * @param name
	 * @param superClassName
	 * @param loader
	 * @param accessFlags
	 */
	public VmNormalClass(
		String name,
		String superClassName,
		AbstractVmClassLoader loader,
		int accessFlags) {
		super(name, superClassName, loader, accessFlags);
		testClassType();
	}

	/**
	 * Return the size in bytes of instantiations of this class
	 * @return The object size
	 */
	public final int getObjSize() {
		return objectSize;
	}
	
	/**
	 * Sets the objectSize.
	 * @param objectSize The objectSize to set
	 */
	protected void setObjectSize(int objectSize) {
		if (this.objectSize == 0) {
			this.objectSize = objectSize;
		} else {
			throw new IllegalArgumentException("Cannot overwrite object size");
		}
	}

	/**
	 * Gets the offsets within an instance of this class of all
	 * reference non-static member variables.
	 * @return The reference offsets
	 */
	public final int[] getReferenceOffsets() {
		return referenceOffsets;
	}

	/**
	 * Do the prepare action required to instantiate this object
	 */
	protected void prepareForInstantiation() {
		// Step 3: Calculate the object size
		final VmNormalClass superCls = getSuperClass();
		int sc_size = (superCls != null) ? superCls.objectSize : 0;
		objectSize += sc_size;

		// Step 4a: Fix the offset for all declared non-static fields
		final int cnt = getNoDeclaredFields();
		int refOffsetsSize = (superCls != null) ? superCls.referenceOffsets.length : 0;
		int startRefIdx = refOffsetsSize; 
		for (int i = 0; i < cnt; i++) {
			final VmField field = getDeclaredField(i);
			//fs.resolve(loader);
			if (!field.isStatic()) {
				final VmInstanceField inf = (VmInstanceField)field;
				inf.resolveOffset(sc_size);
				if (!field.isPrimitive()) {
					if (!field.isAddressType()) {
						refOffsetsSize++;
					} else {
						//System.out.println("Found address in field " + fs.getName());
					}
				}
			}
		}
		
		// Step 4b: Create the referenceOffsets field
		referenceOffsets = new int[refOffsetsSize];
		if (superCls != null) {
			System.arraycopy(superCls.referenceOffsets, 0, referenceOffsets, 0, startRefIdx);
		}
		for (int i = 0; i < cnt; i++) {
			final VmField field = getDeclaredField(i);
			if (!field.isStatic()) {
				final VmInstanceField inf = (VmInstanceField)field;
				if (!field.isPrimitive()) {
					if (!field.isAddressType()) {
						referenceOffsets[startRefIdx++] = inf.getOffset();
					}
				}
			}
		}		 

	}

	/**
	 * Test if this class is using the right modifiers
	 * @throws RuntimeException
	 */
	private final void testClassType() 
	throws RuntimeException {
		if (isArray()) {
			throw new RuntimeException("Not a normal class (array-class)");
		}
		if (isInterface()) {
			throw new RuntimeException("Not a normal class (interface-class)");
		}
	}
}
