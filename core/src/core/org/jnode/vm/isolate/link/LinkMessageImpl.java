/*
 * $Id$
 */
package org.jnode.vm.isolate.link;

import javax.isolate.LinkMessage;

/**
 * Base class for all types of LinkMessage implementation classes.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class LinkMessageImpl extends LinkMessage {

    /**
     * Close this message in the current isolate.
     * @return
     */
    abstract LinkMessageImpl CloneMessage();
    
}
