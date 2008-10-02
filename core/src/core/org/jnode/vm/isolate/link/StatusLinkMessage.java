/*
 * $Id: VmIsolate.java 4592 2008-09-30 12:00:11Z crawley $
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
package org.jnode.vm.isolate.link;

import javax.isolate.IsolateStatus;

/**
 * This class is use to transport status isolate information
 * @author crawley@jnode.org
 */
public final class StatusLinkMessage extends LinkMessageImpl {

    private final String state;
    private final String exitReason;
    private final int exitCode;
    
    /**
     * Internal message constructor used by cloneMessage
     *
     * @param value
     */
    private StatusLinkMessage(String state, String exitReason, int exitCode) {
        this.state = state;
        this.exitReason = exitReason;
        this.exitCode = exitCode;
    }

    /**
     * Message constructor used VmIsolate
     *
     * @param value
     */
    public StatusLinkMessage(IsolateStatus.State state, IsolateStatus.ExitReason exitReason,
            int exitCode) {
        this(state.toString(), exitReason.toString(), exitCode);
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        return new StatusLinkMessage(state, exitReason, exitCode);
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractStatus();
    }

    /**
     * @see javax.isolate.LinkMessage#containsString()
     */
    @Override
    public boolean containsStatus() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extractString()
     */
    @Override
    public IsolateStatus extractStatus() {
        return new IsolateStatus(
                IsolateStatus.State.valueOf(state),
                IsolateStatus.ExitReason.valueOf(exitReason),
                exitCode);
    }
}
