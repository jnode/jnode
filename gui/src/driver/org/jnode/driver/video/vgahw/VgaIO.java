/*
 * $Id$
 */
package org.jnode.driver.video.vgahw;

import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VgaIO {

	void setATTIndex(int index);
	void setATT(int index, int val);
	int getATT(int index);

	void setMISC(int val);
	int getMISC();

	void setSEQ(int index, int val);
	int getSEQ(int index);

	int getSTAT();

	void setGRAF(int index, int val);
	int getGRAF(int index);

	void setCRT(int index, int val);
	int getCRT(int index);

	void setDACReadIndex(int index);
	void setDACWriteIndex(int index);
	void setDACData(int data);
	int getDACData();
	
	MemoryResource getVideoMem();
}
