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
