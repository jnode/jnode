
/*
 * $Id$
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
	private int availableRegisters;
	private BootableArrayList active;
	private BootableArrayList registerPool;
	private EndPointComparator endPointComparator;
	private int stackIndex;

	public LinearScanAllocator(LiveRange[] liveRanges, int availableRegisters) {
		this.liveRanges = liveRanges;
		this.availableRegisters = availableRegisters;
		this.registerPool = new BootableArrayList();
		this.active = new BootableArrayList();
		for (int i=availableRegisters-1; i>=0; i-=1) {
			String name = "r" + i;
			registerPool.add(name);
		}
		endPointComparator = new EndPointComparator();
	}
	
	public void allocate() {
		int n = liveRanges.length;
		for (int i=0; i<n; i+=1) {
			LiveRange lr = liveRanges[i];
			expireOldRange(lr);
			if (active.size() >= availableRegisters) {
				spillRange(lr);
			} else {
				lr.setLocation((String) registerPool.remove(registerPool.size()-1));
				active.add(lr);
				Collections.sort(active, endPointComparator);
			}
		}
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
			registerPool.add(l.getLocation());
		}
	}

	/**
	 * @param lr
	 */
	private void spillRange(LiveRange lr) {
		LiveRange spill = (LiveRange) active.get(active.size() - 1);
		if (spill.getLastUseAddress() > lr.getLastUseAddress()) {
			lr.setLocation(spill.getLocation());
			spill.setLocation("local" + stackIndex);
			stackIndex += 1;
			active.remove(spill);
			active.add(lr);
			Collections.sort(active);
		} else {
			lr.setLocation("local" + stackIndex);
			stackIndex += 1;
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
