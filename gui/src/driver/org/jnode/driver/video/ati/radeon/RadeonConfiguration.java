/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import java.awt.image.ColorModel;

import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.driver.video.FrameBufferConfiguration;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonConfiguration extends FrameBufferConfiguration {

    /**
     * @param width
     * @param height
     * @param colorModel
     */
    public RadeonConfiguration(int width, int height, ColorModel colorModel) {
        super(width, height, colorModel);
    }
    
    /**
     * @see org.jnode.driver.video.FrameBufferConfiguration#createCompatibleImage(int, int, int)
     */
    public JNodeBufferedImage createCompatibleImage(int w, int h,
            int transparency) {
        // TODO Auto-generated method stub
        return null;
    }
}
