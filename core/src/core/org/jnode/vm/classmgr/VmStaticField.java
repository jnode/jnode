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

/**
 * VM representation of a special (&lt;init&gt;) method.
 * 
 * @author epr
 */
public final class VmStaticField extends VmField implements VmStaticsEntry {

	/** The index in the statics table */
	private final int staticsIndex;

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param staticsIndex
	 * @param declaringClass
	 * @param slotSize
	 */
	public VmStaticField(
		String name,
		String signature,
		int modifiers,
		int staticsIndex,
		VmType declaringClass,
		int slotSize) {
		super(name, signature, modifiers, declaringClass, slotSize);
		if (!Modifier.isStatic(modifiers)) {
			throw new IllegalArgumentException("Instance field in VmStaticField");
		}
		this.staticsIndex = staticsIndex;
	}

	/**
	 * Gets the indexe of this field in the statics table.
	 * @return Returns the staticsIndex.
	 */
	public final int getStaticsIndex() {
		return this.staticsIndex;
	}
}
