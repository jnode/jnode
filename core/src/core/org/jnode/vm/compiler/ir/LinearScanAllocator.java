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
 
package org.jnode.vm.compiler.ir;

import java.util.Collections;
import java.util.Comparator;

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class LinearScanAllocator {
	private LiveRange[] liveRanges;
	private BootableArrayList active;
	private RegisterPool registerPool;
	private EndPointComparator endPointComparator;
	private BootableArrayList spilledVariableList;
	private Variable[] spilledVariables;

	public LinearScanAllocator(LiveRange[] liveRanges) {
		this.liveRanges = liveRanges;
		this.registerPool = CodeGenerator.getInstance().getRegisterPool();
		this.active = new BootableArrayList();
		this.endPointComparator = new EndPointComparator();
		this.spilledVariableList = new BootableArrayList();
	}
	
	public void allocate() {
		int n = liveRanges.length;
		for (int i=0; i<n; i+=1) {
			LiveRange lr = liveRanges[i];
			Variable var = lr.getVariable();
			if (!(var instanceof MethodArgument)) {
				// don't allocate method arguments to registers
				expireOldRange(lr);
				Object reg = registerPool.request(var.getType());
				if (reg == null) {
					spillRange(lr);
				} else {
					lr.setLocation(new RegisterLocation(reg));
					active.add(lr);
					Collections.sort(active, endPointComparator);
				}
			}
		}
		// This sort is probably not necessary...
		Collections.sort(spilledVariableList, new StorageSizeComparator());
		n = spilledVariableList.size();
		spilledVariables = new Variable[n];
		for (int i=0; i<n; i+=1) {
			spilledVariables[i] = (Variable) spilledVariableList.get(i);
		}
	}

	public Variable[] getSpilledVariables() {
		return spilledVariables;
	}

	/**
	 * @param lr
	 */
	private void expireOldRange(LiveRange lr) {
		for (int i=0; i<active.size(); i+=1) {
			LiveRange l = (LiveRange) active.get(i);
			if (l.getLastUseAddress() >= lr.getAssignAddress()) {
				return;
			}
			active.remove(l);
			RegisterLocation regLoc = (RegisterLocation) l.getLocation();
			registerPool.release(regLoc.getRegister());
		}
	}

	/**
	 * @param lr
	 */
	private void spillRange(LiveRange lr) {
		LiveRange spill = (LiveRange) active.get(active.size() - 1);
		if (spill.getLastUseAddress() > lr.getLastUseAddress()) {
			lr.setLocation(spill.getLocation());
			spill.setLocation(new StackLocation());
			this.spilledVariableList.add(spill.getVariable());
			active.remove(spill);
			active.add(lr);
			Collections.sort(active);
		} else {
			lr.setLocation(new StackLocation());
			this.spilledVariableList.add(lr.getVariable());
		}
	}
}

class EndPointComparator implements Comparator {
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		LiveRange lr1 = (LiveRange) o1;
		LiveRange lr2 = (LiveRange) o2;
		return lr1.getLastUseAddress() - lr2.getLastUseAddress();
	}
}

class StorageSizeComparator implements Comparator {
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		Variable lr1 = (Variable) o1;
		Variable lr2 = (Variable) o2;
		int size1 = 0;
		int size2 = 0;
		// These are defined in the order on the stack
		switch (lr1.getType()) {
			case Operand.BYTE: size1 = 1; break;
			case Operand.SHORT: size1 = 2; break;
			case Operand.CHAR: size1 = 3; break;
			case Operand.INT: size1 = 4; break;
			case Operand.FLOAT: size1 = 5; break;
			// this could be 32 or 64 bits, in between FLOAT and LONG is best
			case Operand.REFERENCE: size1 = 6; break;
			case Operand.LONG: size1 = 7; break;
			case Operand.DOUBLE: size1 = 8; break;
		}
		switch (lr2.getType()) {
			case Operand.BYTE: size2 = 1; break;
			case Operand.SHORT: size2 = 2; break;
			case Operand.CHAR: size2 = 3; break;
			case Operand.INT: size2 = 4; break;
			case Operand.FLOAT: size2 = 5; break;
			case Operand.REFERENCE: size2 = 6; break;
			case Operand.LONG: size2 = 7; break;
			case Operand.DOUBLE: size2 = 8; break;
		}
		return size1 - size2;
	}
}
