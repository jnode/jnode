/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;
import org.vmmagic.pragma.Uninterruptible;

/**
 * Base class for the internal representation of methods and fields.
 * 
 * @author epr
 */
abstract class VmMember extends VmSystemObject implements Uninterruptible {

	/** Name of this member */
	protected final String name;
	/** Signature of this member */
	protected final String signature;
	/** Modifiers of this member */
	private int modifiers;
	/** Declaring class of this member */
	protected final VmType declaringClass;
	/** Hashcode of name+signature */
	private final int cachedHashCode;

	/**
	 * Create a new instance
	 * 
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 */
	protected VmMember(String name, String signature, int modifiers, VmType declaringClass) {
		if (name.equals("<clinit>")) {
			modifiers |= Modifier.ACC_INITIALIZER;
		} else if (name.equals("<init>")) {
			modifiers |= Modifier.ACC_CONSTRUCTOR;
		}
        if (Modifier.isWide(signature)) {
            modifiers |= Modifier.ACC_WIDE;
        }
		this.name = name;
		this.signature = signature;
		this.modifiers = modifiers;
		this.declaringClass = declaringClass;
		this.cachedHashCode = calcHashCode(name, signature);
	}

	/**
	 * Returns the accessFlags.
	 * 
	 * @return int
	 */
	public final int getModifiers() {
		return modifiers;
	}

	/**
	 * Set/Reset a modifier flag
	 * 
	 * @param on
	 * @param modifier
	 */
	protected final void setModifier(boolean on, int modifier) {
		if (on) {
			modifiers |= modifier;
		} else {
			modifiers &= ~modifier;
		}
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is my name equal to the given name?
	 * 
	 * @param otherName
	 * @return boolean
	 */
	public boolean nameEquals(String otherName) {
		return name.equals(otherName);
	}

	/**
	 * Returns the signature.
	 * 
	 * @return String
	 */
	public String getSignature() {
		return signature;
	}

	public boolean signatureEquals(String otherSignature) {
		return signature.equals(otherSignature);
	}

	/**
	 * Gets the Class i'm declared in.
	 * 
	 * @return VmClass
	 */
	public VmType<?> getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Is this member public?
	 * @return boolean
	 */
	public boolean isPublic() {
		return Modifier.isPublic(modifiers);
	}
	/**
	 * Is this member protected?
	 * @return boolean
	 */
	public boolean isProtected() {
		return Modifier.isProtected(modifiers);
	}
	/**
	 * Is this member private?
	 * @return boolean
	 */
	public boolean isPrivate() {
		return Modifier.isPrivate(modifiers);
	}
	/**
	 * Is this member static?
	 * @return boolean
	 */
	public boolean isStatic() {
		return Modifier.isStatic(modifiers);
	}
	/**
	 * Is this member final?
	 * @return boolean
	 */
	public boolean isFinal() {
		return Modifier.isFinal(modifiers);
	}

	/**
	 * Calculate a method hashcode, based on its name & signature
	 * 
	 * @param name
	 * @param signature
	 * @return int
	 */
	protected static int calcHashCode(String name, String signature) {
		return (name.hashCode() ^ signature.hashCode());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public int getMemberHashCode() {
		return cachedHashCode;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public int hashCode() {
		return cachedHashCode;
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#getExtraInfo()
	 * @return String
	 */
	public String getExtraInfo() {
		return "Modifiers: " + Modifier.toString(modifiers);
	}

    /**
     * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
     */
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        if (!declaringClass.isCompiled()) {
            throw new RuntimeException("emit before compile in " + this);
        }
    }
}
