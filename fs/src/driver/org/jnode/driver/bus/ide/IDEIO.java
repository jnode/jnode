/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.bus.ide;

import org.jnode.util.TimeoutException;

/**
 * IDE IO-port accessor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface IDEIO extends IDEConstants {
    
    /**
     * Stop this processor.
     */
    public void release();

    /**
     * Gets a word from the data register
     * @return a word from the data register
     */
    public int getDataReg();

    /**
     * Writes a word to the data register
     * 
     * @param dataWord
     */
    public void setDataReg(int dataWord);
    
    /**
     * Gets the contents of the error register
     * @return the contents of the error register
     */
    public int getErrorReg();

    /**
     * Sets the contents of the featureregister
     * @param features 
     */
    public void setFeatureReg(int features);

    /**
     * Gets the contents of the sector count register
     * @return the contents of the sector count register
     */
    public int getSectorCountReg();

    /**
     * Sets the sector count register
     * 
     * @param sectorCount
     */
    public void setSectorCountReg(int sectorCount);

    /**
     * Gets the contents of the sector register
     * @return the contents of the sector register
     */
    public int getSectorReg();

    /**
     * Gets the contents of the LBA low register
     * @return the contents of the LBA low register
     */
    public int getLbaLowReg();

    /**
     * Gets the contents of the LBA mid register
     * @return the contents of the LBA mid register
     */
    public int getLbaMidReg();

    /**
     * Gets the contents of the LBA high register
     * @return the contents of the LBA high register
     */
    public int getLbaHighReg();

    /**
     * Sets the contents of the LBA low register
     * @param value 
     */
    public void setLbaLowReg(int value);

    /**
     * Sets the contents of the LBA mid register
     * @param value 
     */
    public void setLbaMidReg(int value);

    /**
     * Sets the contents of the LBA high register
     * @param value 
     */
    public void setLbaHighReg(int value);

    /**
     * Gets the contents of the select register
     * @return the contents of the select register
     */
    public int getSelectReg();

    /**
     * Sets the select register
     * 
     * @param select
     */
    public void setSelectReg(int select);

    /**
     * Gets the status of the IDE controller. Any pending IRQ is reset.
     * @return the status of the IDE controller
     */
    public int getStatusReg();

    /**
     * Gets the alternative status of the IDE controller. Any pending IRQ is
     * NOT reset.
     * @return the alternative status of the IDE controller
     */
    public int getAltStatusReg();

    /**
     * Sets the command register. This also activates the IDE controller so
     * always set other registers first.
     * 
     * @param command
     */
    public void setCommandReg(int command);

    /**
     * Sets the control register.
     * 
     * @param control
     *            The new value for the control register
     */
    public void setControlReg(int control);

    /**
     * Is this channel busy.
     * @return if this channel is busy
     */
    public boolean isBusy();        
    
    /**
     * Block the current thread until the controller is not busy anymore.
     * @param timeout 
     * @throws TimeoutException 
     */
    public void waitUntilNotBusy(long timeout) throws TimeoutException;

    /**
     * @return Returns the irq.
     */    
    public int getIrq();
}
