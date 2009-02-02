/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import org.jnode.vm.annotation.SharedStatics;

/**
 * Represents isolate status, capturing such information as isolate state, exit code, and exit reason.
 * State transitions in the isolate life cycle generate LinkMessages containing instances of this class
 * which are sent over status links (but can be then forwarded over any link). If the isolate state is EXITED,
 * the corresponding exit code and exit reason are contained in IsolateStatus.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public class IsolateStatus {

    /**
     * Defines the state of an Isolate.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    public static enum State {
        /**
         * Isolate is starting
         */
        STARTING,

        /**
         * Isolate is started
         */
        STARTED,

        /**
         * Isolate is exiting
         */
        EXITING,

        /**
         * Isolate has exited
         */
        EXITED,

        /**
         * State is not known
         */
        UNKNOWN
    }

    /**
     * Provides the reason for the termination of an isolate.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    public static enum ExitReason {
        /**
         * The last non-deamon thread exited
         */
        IMPLICIT_EXIT,

        /**
         * Another isolate invoked Isolate.exit
         */
        OTHER_EXIT,

        /**
         * Another isolate invoed Isolated.halt
         */
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

        /**
         * The last non-deamon thread exited due to an uncaught exception
         */
        UNCAUGHT_EXCEPTION
    }

    private final ExitReason exitReason;
    private final State state;
    private final int exitCode;

    /**
     * Initialize this instance.
     *
     * @param state
     * @param exitReason
     * @param exitCode
     */
    protected IsolateStatus(State state, ExitReason exitReason, int exitCode) {
        if (state == null)
            throw new IllegalArgumentException();

        if (state.equals(State.EXITED) && exitReason == null)
            throw new IllegalArgumentException();

        this.state = state;
        this.exitReason = exitReason;
        this.exitCode = exitCode;
    }

    /**
     * Gets the exit reason.
     *
     * @return
     */
    public ExitReason getExitReason() {
        return exitReason;
    }

    /**
     * Gets the exit code.
     *
     * @return
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets the state of the isolate.
     *
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IsolateStatus)) return false;

        IsolateStatus that = (IsolateStatus) o;

        if (!state.equals(that.state)) return false;

        if (state.equals(State.EXITED)) {
            if (exitCode != that.exitCode) return false;
            if (exitReason != that.exitReason) return false;
        }

        return true;
    }

    /**
     * @return
     */
    public int hashCode() {
        int result = state.hashCode();
        if (state.equals(State.EXITED)) {
            result = 31 * result + exitReason.hashCode();
            result = 31 * result + exitCode;
        }
        return result;
    }
}
