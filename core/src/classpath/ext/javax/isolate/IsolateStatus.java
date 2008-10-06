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
 
package javax.isolate;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IsolateStatus {

    /**
     * Defines the state of an Isolate.
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static enum State {
        /** Isolate is starting */
        STARTING,

        /** Isolate is started */
        STARTED,

        /** Isolate is exiting */
        EXITING,

        /** Isolate has exited */
        EXITED,

        /** State is not known */
        UNKNOWN;
    }

    /**
     * Provides the reason for the termination of an isolate.
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static enum ExitReason {
        /** The last non-deamon thread exited */
        IMPLICIT_EXIT,

        /** Another isolate invoked Isolate.exit */
        OTHER_EXIT,

        /** Another isolate invoed Isolated.halt */
        OTHER_HALT,

        /**
         * The isolate invoked System.exit, Runtime.exit or Isolate.exit by
         * itself.
         */
        SELF_EXIT,

        /**
         * The isolate invoked Runtime.halt or Isolate.halt by itself.
         */
        SELF_HALT,

        /** The last non-deamon thread exited due to an uncaught exception */
        UNCAUGHT_EXCEPTION;
    }
    
    private final ExitReason exitReason;
    private final State state;
    private final int exitCode;
    
    /**
     * Initialize this instance.
     * @param state
     * @param exitReason
     * @param exitCode
     */
    protected IsolateStatus(State state, ExitReason exitReason, int exitCode) {
        this.state = state;
        this.exitReason = exitReason;
        this.exitCode = exitCode;
    }
    
    /**
     * Are this and the given object equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        return super.equals(other);
    }
    
    /**
     * Gets the exit reason.
     * @return
     */
    public ExitReason getExitReason() {
        return exitReason;
    }
    
    /**
     * Gets the exit code.
     * @return
     */
    public int getExitCode() {
        return exitCode;
    }
    
    /**
     * Gets the state of the isolate.
     * @return
     */
    public State getState() {
        return state;
    }
}
