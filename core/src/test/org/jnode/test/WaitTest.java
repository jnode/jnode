/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class WaitTest {

	public static void main(String[] args) 
	throws Exception {
		
		for (int j = 0; j < 20; j++) {
			final WaitTest wt = new WaitTest();
		
			for (int i = 0; i < 10; i++) {
				final int k = i;
				new Thread(new Runnable() {
					public void run() {
						wt.test(k);
					}}).start();
			}
		
			Thread.sleep(2000);
		
			wt.trigger();
		
			// Now test the wait with timeout
		
			wt.testTimeout();
		}		
	}
	
	private boolean trigger = false;
	
	public synchronized void test(int i) {
		if (trigger) {
			//System.out.println("Skipping " + i);
		} else {
			try {
				//System.out.println("Before wait " + i);
				wait();
				//System.out.println("After wait " + i);
			} catch (InterruptedException ex) {
				System.out.println("Interrupted " + i);
			}
			//System.out.println("Ready " + i);
		}
	}
	
	public synchronized void testTimeout() {
		try {
			//System.out.println("Before waitTimeout");
			final long start = System.currentTimeMillis();
			wait(500);
			final long end = System.currentTimeMillis();
			System.out.println("After waitTimeout: it took " + (end-start) + "ms");
		} catch (InterruptedException ex) {
			System.out.println("Interrupted in waitTimeout");
		}
		//System.out.println("Ready waitTimeout");
	}
	
	public synchronized void trigger() {
		//System.out.println("Before notifyAll");
		trigger = true;
		notifyAll();
		//System.out.println("After notifyAll");
	}

}
