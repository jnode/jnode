/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jnode.vm.PragmaUninterruptible;

/**
 * Abstract class for VmType classes that represent classes.
 *  
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmClassType extends VmType {

	/** 
	 * The Virtual method table of this class. 
	 * It contains: VmClass, Method 0, Method 1...
	 */
	private Object[] tib;
	/** The number of instances of this class */
	private int instanceCount;
	/** The interface method tables (each imt is an entry within this array) */
	//private Object[] imtList;
	private VmInstanceMethod[] syntheticAbstractMethods;

	/**
	 * @param name
	 * @param superClassName
	 * @param loader
	 * @param accessFlags
	 */
	public VmClassType(
		String name,
		String superClassName,
		AbstractVmClassLoader loader,
		int accessFlags) {
		super(name, superClassName, loader, accessFlags);
	}

	/**
	 * @param name
	 * @param superClass
	 * @param loader
	 * @param primitive
	 * @param typeSize
	 */
	public VmClassType(
		String name,
		VmNormalClass superClass,
		AbstractVmClassLoader loader,
		boolean primitive,
		int typeSize) {
		super(name, superClass, loader, primitive, typeSize);
	}

	/**
	 * Returns the instanceCount.
	 * @return int
	 */
	public int getInstanceCount() {
		return instanceCount;
	}

	public final void incInstanceCount() 
	throws PragmaUninterruptible {
		instanceCount++;
	}

	/**
	 * Find a method from a given interface method reference.
	 * @param methodRef
	 */
	/*public final VmMethod getIMethod(VmConstIMethodRef methodRef) {
		 final VmType intf = methodRef.getConstClass().getResolvedVmClass();
		 final Object[] imtList = this.imtList;
		 final int imtListSize = imtList.length;
		 for (int i = 0; i < imtListSize; i++) {
			final Object[] imt = (Object[])imtList[i];
			if (imt[0] == intf) {
				return (VmMethod)imt[methodRef.getImtOffset()];
			}
		 }
		 // Not found
		 return null;
	}*/

	/**
	 * Gets the type information block
	 * @return VmSystemObject[]
	 */
	public final Object[] getTIB() {
		return tib;
	}
	
	/**
	 * Prepare the virtual method table
	 * @param allInterfaces
	 * @return The tib
	 */
	protected Object[] prepareTIB(HashSet allInterfaces) {
		final VmNormalClass superClass = getSuperClass();
		final TIBBuilder vmt;
		
		if (superClass != null) {
			// Initialize from 
			vmt = new TIBBuilder(this, superClass.getTIB());
		} else {
			vmt = new TIBBuilder(this);
		}
		
		final int tc_mtable_length = getNoDeclaredMethods();

		// Loop through the method table of this class
		// searching for virtual methods which are not in the method table
		// of the super class.
		for (int i = 0; i < tc_mtable_length; i++) {
			final VmMethod mts = getDeclaredMethod(i);
			if (!(mts.isStatic() || mts.isSpecial())) {
				final VmInstanceMethod method = (VmInstanceMethod)mts;
				final String name = mts.getName();
				final String signature = mts.getSignature();
				final int index = vmt.indexOf(name, signature);
				if (index >= 0) {
					// The method existed in the super class, overwrite it
					vmt.set(index, method);
				} else {
					// The method does not exist yet.
					vmt.add(method);
				}
			}
		}
		
		// Loop through all the implemented interfaces of this class,
		// searching for methods that don't have an implementation (and a
		// place in the VMT) yet.
		// This is only needed for abstract methods, since non-abstract 
		// methods must have implemented all methods already.
		ArrayList syntheticAbstractMethods = null;
		if (isAbstract()) {
			for (Iterator i = allInterfaces.iterator(); i.hasNext(); ) {
				final VmInterfaceClass icls = (VmInterfaceClass)i.next();
				final int cnt = icls.getNoDeclaredMethods();
				for (int j = 0; j < cnt; j++) {
					final VmMethod mts = icls.getDeclaredMethod(j);
					if (!mts.isStatic()) {  
						final VmInstanceMethod method = (VmInstanceMethod)mts;
						final String name = method.getName();
						final String signature = method.getSignature();
						final int index = vmt.indexOf(name, signature);
						if (index >= 0) {
							// The method already exist in the VMT
							// do nothing
						} else {
							// The method does not exist yet.
							// Create a clone to include in our vmt.
							// We must clone here, because the VMT offset
							// is set. Without a clone, the VMT offset of
							// an interface method could be overwritten 
							// in several abstract classes.
							final VmInstanceMethod clone = new VmInstanceMethod(method);
							vmt.add(clone);
							if (syntheticAbstractMethods == null) {
								syntheticAbstractMethods = new ArrayList();
							}
							syntheticAbstractMethods.add(clone);
						}
					}
				}
			}
		}
		if (syntheticAbstractMethods != null) {
			this.syntheticAbstractMethods = new VmInstanceMethod[syntheticAbstractMethods.size()];
			syntheticAbstractMethods.toArray(this.syntheticAbstractMethods);
		}
		
		
		// Use the new VMT
		this.tib = vmt.toArray();
		
		return tib;
	}

	/**
	 * Prepare the interface method tables
	 * @param allInterfaces
	 * @return The builder
	 */
	protected IMTBuilder prepareIMT(HashSet allInterfaces) {
		final IMTBuilder imtBuilder = new IMTBuilder();
		for (Iterator i = allInterfaces.iterator(); i.hasNext(); ) {
			final VmType intf = (VmType)i.next();
			final int max = intf.getNoDeclaredMethods();
			// Add all method of the current interface
			for (int m = 0; m < max; m++) {
				final VmMethod intfMethod = intf.getDeclaredMethod(m);
				if (!intfMethod.isStatic()) {
					final VmMethod clsMethod = getMethod(intfMethod.getName(), intfMethod.getSignature());
					if (clsMethod instanceof VmInstanceMethod) {
						imtBuilder.add((VmInstanceMethod)clsMethod);
					} else {
						throw new ClassFormatError("Interface method " + intfMethod.getName() + " in class " + getName() + " is static");
					}
				}
			}
		}
		return imtBuilder;
	}

	/**
	 * Search for an synthetic abstract class, that is not in this class,
	 * but is a method of one of the implemented interfaces.
	 * Synthetic abstract methods are added when the VMT is created.
	 * @param name
	 * @param signature
	 * @param hashCode
	 * @return The method
	 */
	protected VmMethod getSyntheticAbstractMethod(String name, String signature, int hashCode) {
		if (syntheticAbstractMethods != null) {
			final int len = syntheticAbstractMethods.length;
			for (int i = 0; i < len; i++) {
				final VmMethod mts = syntheticAbstractMethods[i];
				int mtsHashCode = mts.getMemberHashCode();
				if (mtsHashCode == hashCode) {
					if (mts.nameEquals(name)
						&& mts.signatureEquals(signature)) {
						return mts;
					}
				}
			}
		}
		// Not found
		return null;
	}

	/**
	 * Verify this object before it is written into the bootimage
	 * by the bootimage builder.
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
	public void verifyBeforeEmit() {
		if (tib == null) {
			throw new RuntimeException("TIB == null in " + getName());
		}
		super.verifyBeforeEmit();
	}
}
