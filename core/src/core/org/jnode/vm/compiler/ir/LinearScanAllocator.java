
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.Collections;
import java.util.Comparator;

import org.jnode.util.BootableArrayList;
import org.jnode.util.BootableHashMap;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class LinearScanAllocator {
	private LiveRange[] liveRanges;
	private BootableArrayList active;
	private RegisterPool registerPool;
	private EndPointComparator endPointComparator;
	private int stackIndex;
	private BootableHashMap variableMap;

	public LinearScanAllocator(LiveRange[] liveRanges) {
		this.liveRanges = liveRanges;
		this.registerPool = RegisterPool.getInstance();
		this.active = new BootableArrayList();
		this.endPointComparator = new EndPointComparator();
	}
	
	public void allocate() {
		int n = liveRanges.length;
		for (int i=0; i<n; i+=1) {
			LiveRange lr = liveRanges[i];
			expireOldRange(lr);
			Object reg = registerPool.request(lr.getVariable().getType());
			if (reg == null) {
				spillRange(lr);
			} else {
				lr.setLocation(reg);
				active.add(lr);
				Collections.sort(active, endPointComparator);
			}
		}
		this.variableMap = new BootableHashMap();
		for (int i=0; i<n; i+=1) {
			LiveRange lr = liveRanges[i];
			variableMap.put(lr.getVariable(), lr.getLocation());
		}
	}

	public BootableHashMap getVariableMap() {
		return this.variableMap;
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
			registerPool.release(l.getLocation());
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
