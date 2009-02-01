/*
 * $Id$
 *
 * JNode.org
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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IsolateStartupException extends Exception {

    private final String remoteName;

    private final String remoteMessage;

    private final StackTraceElement[] remoteTrace;

    /**
     * Initialize this instance.
     */
    public IsolateStartupException() {
        this.remoteName = null;
        this.remoteMessage = null;
        this.remoteTrace = null;
    }

    /**
     * Initialize this instance.
     * 
     * @param detail
     * @param cause
     */
    public IsolateStartupException(String detail, Throwable cause) {
        super(detail, cause);
        this.remoteName = null;
        this.remoteMessage = null;
        this.remoteTrace = null;
    }

    /**
     * Initialize this instance.
     * 
     * @param detail
     */
    public IsolateStartupException(String detail) {
        super(detail);
        this.remoteName = null;
        this.remoteMessage = null;
        this.remoteTrace = null;
    }

    /**
     * Initialize this instance.
     * 
     * @param s
     */
    public IsolateStartupException(String detail, String remoteName,
            String remoteMessage, StackTraceElement[] trace) {
        super(detail);
        this.remoteName = remoteName;
        this.remoteMessage = remoteMessage;
        this.remoteTrace = trace;
    }

    /**
     * Initialize this instance.
     * 
     * @param cause
     */
    public IsolateStartupException(Throwable cause) {
        super(cause);
        this.remoteName = null;
        this.remoteMessage = null;
        this.remoteTrace = null;
    }

    /**
     * @return Returns the remoteMessage.
     */
    public String getRemoteMessage() {
        return remoteMessage;
    }

    /**
     * @return Returns the remoteName.
     */
    public String getRemoteName() {
        return remoteName;
    }

    /**
     * @return Returns the remoteTrace.
     */
    public StackTraceElement[] getRemoteStackTrace() {
        return remoteTrace;
    }

    /**
     * Print the remote stacktrace.
     */
    public void printRemoteStackTrace() {
        // TODO implement me
    }

    /**
     * Print the remote stacktrace.
     */
    public void printRemoteStackTrace(PrintStream ps) {
        // TODO implement me
    }

    /**
     * Print the remote stacktrace.
     */
    public void printRemoteStackTrace(PrintWriter writer) {
        // TODO implement me
    }
}
