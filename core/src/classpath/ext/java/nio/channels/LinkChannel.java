/*
 * $Id$
 */
package java.nio.channels;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class LinkChannel extends AbstractSelectableChannel {

    /**
     * @param provider
     */
    LinkChannel(SelectorProvider provider) {
        super(provider);
    }

}
