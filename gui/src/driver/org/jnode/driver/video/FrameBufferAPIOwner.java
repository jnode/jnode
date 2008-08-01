package org.jnode.driver.video;

public interface FrameBufferAPIOwner {
    /**
     * Ask the owner to free the ownership 
     */
    void ownershipLost();    
    
    /**
     * Called by the {@link FrameBufferAPI} when the ownership is given again to the {@link FrameBufferAPIOwner} 
     */
    void ownershipGained();
}
