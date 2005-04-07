/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2CommandResponse implements Prism2Constants {

    private int status;

    private int response0;

    private int response1;

    private int response2;

    /**
     * Read the status from the device.
     * 
     * @param core
     */
    final void initialize(Prism2IO core) {
        this.status = core.getReg(REG_STATUS);
        this.response0 = core.getReg(REG_RESP0);
        this.response1 = core.getReg(REG_RESP1);
        this.response2 = core.getReg(REG_RESP2);
    }

    /**
     * @return Returns the response0.
     */
    public final int getResponse0() {
        return response0;
    }

    /**
     * @return Returns the response1.
     */
    public final int getResponse1() {
        return response1;
    }

    /**
     * @return Returns the response2.
     */
    public final int getResponse2() {
        return response2;
    }

    /**
     * Gets the full status value 
     * @return Returns the status.
     */
    public final int getStatus() {
        return status;
    }
    
    /**
     * Gets the result code.
     * @return
     */
    public final int getResult() {
        return (status & STATUS_RESULT) >> 8;
    }

}
