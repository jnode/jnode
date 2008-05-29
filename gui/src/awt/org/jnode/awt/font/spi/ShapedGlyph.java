package org.jnode.awt.font.spi;

import java.awt.geom.GeneralPath;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public interface ShapedGlyph {
    public GeneralPath getShape();
}
