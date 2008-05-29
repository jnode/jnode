package org.jnode.awt.font.spi;

import java.io.IOException;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public interface FontData {
    public Glyph getGlyph(char c) throws IOException;
}
