/* SocketPermission.java -- Class modeling permissions for socket operations
 Copyright (C) 1998, 2000, 2001, 2002 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package java.net;

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class models a specific set of permssions for connecting to a host.
 * There are two elements to this, the host/port combination and the permission
 * list.
 * <p>
 * The host/port combination is specified as followed
 * <p>
 * 
 * <pre>
 *  hostname[:[-]port[-[port]]]
 * </pre>
 * 
 * <p>
 * The hostname portion can be either a hostname or IP address. If it is a
 * hostname, a wildcard is allowed in hostnames. This wildcard is a "*" and
 * matches one or more characters. Only one "*" may appear in the host and it
 * must be the leftmost character. For example, "*.urbanophile.com" matches all
 * hosts in the "urbanophile.com" domain.
 * <p>
 * The port portion can be either a single value, or a range of values treated
 * as inclusive. The first or the last port value in the range can be omitted
 * in which case either the minimum or maximum legal value for a port
 * (respectively) is used by default. Here are some examples:
 * <p>
 * <ul>
 * <li>8080 - Represents port 8080 only
 * <li>2000-3000 - Represents ports 2000 through 3000 inclusive
 * <li>-4000 - Represents ports 0 through 4000 inclusive
 * <li>1024- - Represents ports 1024 through 65535 inclusive
 * </ul>
 * <p>
 * The permission list is a comma separated list of individual permissions.
 * These individual permissions are:
 * <p>
 * accept <br>
 * connect <br>
 * listen <br>
 * resolve <br>
 * <p>
 * The "listen" permission is only relevant if the host is localhost. If any
 * permission at all is specified, then resolve permission is implied to exist.
 * <p>
 * Here are a variety of examples of how to create SocketPermission's
 * <p>
 * 
 * <pre>
 *  SocketPermission(&quot;www.urbanophile.com&quot;, &quot;connect&quot;);
 *  Can connect to any port on www.urbanophile.com
 *  SocketPermission(&quot;www.urbanophile.com:80&quot;, &quot;connect,accept&quot;);
 *  Can connect to or accept connections from www.urbanophile.com on port 80
 *  SocketPermission(&quot;localhost:1024-&quot;, &quot;listen,accept,connect&quot;);
 *  Can connect to, accept from, an listen on any local port number 1024
 *  and up.
 *  SocketPermission(&quot;*.edu&quot;, &quot;connect&quot;);
 *  Can connect to any host in the edu domain
 *  SocketPermission(&quot;197.197.20.1&quot;, &quot;accept&quot;);
 *  Can accept connections from 197.197.20.1
 * </pre>
 * 
 * <p>
 * 
 * @since 1.2
 * 
 * @author Aaron M. Renn (arenn@urbanophile.com)
 */
public final class SocketPermission extends Permission implements Serializable {

    static final long serialVersionUID = -7204263841984476862L;

    // FIXME: Needs serialization work, including readObject/writeObject
    // methods.
    /**
     * A hostname/port combination as described above
     */
    private transient String hostport;

    private transient int mask;

    /**
     * A comma separated list of actions for which we have permission
     */
    private String actions;

    private static final int CONNECT = 0x01;

    private static final int ACCEPT = 0x02;

    private static final int LISTEN = 0x04;

    private static final int RESOLVE = 0x08;

    /**
     * Initializes a new instance of <code>SocketPermission</code> with the
     * specified host/port combination and actions string.
     * 
     * @param hostport
     *            The hostname/port number combination
     * @param actions
     *            The actions string
     */
    public SocketPermission(String hostport, String actions) {
        super(hostport);
        this.hostport = hostport;
        this.mask = getMask(actions);
        this.actions = actions;
    }

    /**
     * Tests this object for equality against another. This will be true if and
     * only if the passed object is an instance of <code>SocketPermission</code>
     * and both its hostname/port combination and permissions string are
     * identical.
     * 
     * @param obj
     *            The object to test against for equality
     * 
     * @return <code>true</code> if object is equal to this object, <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return (false);

        if (!(obj instanceof SocketPermission)) return (false);

        if (((SocketPermission) obj).hostport.equals(hostport))
                if (((SocketPermission) obj).actions.equals(actions))
                        return (true);

        return (false);
    }

    /**
     * Returns a hash code value for this object. Overrides the
     * Permission.hashCode()
     * 
     * @return A hash code
     */
    public int hashCode() {
        int hash = 100;
        if (hostport != null) hash += hostport.hashCode();
        if (actions != null) hash += actions.hashCode();
        return hash;
    }

    /**
     * Returns the list of permission actions in this object in canonical
     * order. The canonical order is "connect,listen,accept,resolve"
     * 
     * @return The permitted action string.
     */
    public String getActions() {
        final StringBuffer sb = new StringBuffer();
        boolean comma = false;

        if ((mask & CONNECT) != 0) {
            sb.append("connect");
            comma = true;
        }

        if ((mask & LISTEN) != 0) {
            if (comma) sb.append(',');
            sb.append("listen");
            comma = true;
        }

        if ((mask & ACCEPT) != 0) {
            if (comma) sb.append(',');
            sb.append("accept");
            comma = true;
        }

        if ((mask & RESOLVE) != 0) {
            if (comma) sb.append(',');
            sb.append("resolve");
            comma = true;
        }

        return (sb.toString());
    }

    /**
     * Returns a new <code>PermissionCollection</code> object that can hold
     * <code>SocketPermission</code> 's.
     * 
     * @return A new <code>PermissionCollection</code>.
     */
    public PermissionCollection newPermissionCollection() {
        return new SocketPermissionCollection();
    }

    /**
     * Returns true if the permission object passed it is implied by the this
     * permission. This will be true if
     * <p>
     * <ul>
     * <li>The argument is of type SocketPermission
     * <li>The actions list of the argument are in this object's actions
     * <li>The port range of the argument is within this objects port range
     * <li>The hostname is equal to or a subset of this objects hostname
     * </ul>
     * <p>
     * The argument's hostname will be a subset of this object's hostname if:
     * <p>
     * <ul>
     * <li>The argument's hostname or IP address is equal to this object's.
     * <li>The argument's canonical hostname is equal to this object's.
     * <li>The argument's canonical name matches this domains hostname with
     * wildcards
     * </ul>
     * 
     * @param perm
     *            The Permission to check against
     * 
     * @return <code>true</code> if the <code>Permission</code> is implied
     *         by this object, <code>false</code> otherwise.
     */
    public boolean implies(Permission perm) {
        final SocketPermission that;
        //System.out.println("implies");

        // First make sure we are the right object type
        if (perm instanceof SocketPermission) {
            that = (SocketPermission) perm;
        } else {
            return false;
        }

        // Next check the actions
        if ((this.mask & that.mask) != that.mask) {
            return false;
        }

        // Now check ports
        final int[] ourPorts = getPorts(hostport);
        final int[] theirPorts = getPorts(that.hostport);
        if ((theirPorts[ 0] < ourPorts[ 0]) || (theirPorts[ 1] > ourPorts[ 1])) {
            return (false);
        }

        // Finally we can check the hosts
        final String ourhost = getHost(hostport);
        final String theirhost = getHost(that.hostport);

        // Are they equal?
        if (ourhost.equals(theirhost)) return true;
        if (ourhost.equals("*")) return true;

        // Try the canonical names
        String ourcanonical = null, theircanonical = null;
        try {
            ourcanonical = InetAddress.getByName(ourhost).getHostName();
            theircanonical = InetAddress.getByName(theirhost).getHostName();
        } catch (UnknownHostException e) {
            // Who didn't resolve? Just assume current address is canonical
            // enough
            // Is this ok to do?
            if (ourcanonical == null) ourcanonical = ourhost;
            if (theircanonical == null) theircanonical = theirhost;
        }

        if (ourcanonical.equals(theircanonical)) return (true);

        // Well, last chance. Try for a wildcard
        if (ourhost.indexOf("*.") != -1) {
            String wild_domain = ourhost.substring(ourhost.indexOf("*" + 1));
            if (theircanonical.endsWith(wild_domain)) return (true);
        }

        // Didn't make it
        return false;
    }

    private final String getHost(String hostPort) {
        final int idx = hostPort.indexOf(':');
        if (idx < 0) {
            return hostPort;
        } else {
            return hostPort.substring(0, idx);
        }
    }

    /**
     * Gets the lowest-highest port from the combined host+port
     * 
     * @param hostPort
     * @return { low, high }
     */
    private final int[] getPorts(String hostPort) {
        final int colonIndex = hostPort.indexOf(':');
        if (colonIndex < 0) { 
        // No ports
        return new int[] { 0, 65535}; }

        final String ports = hostPort.substring(colonIndex + 1).trim();
        final int minIndex = ports.indexOf('-');
        if (minIndex == 0) {
            // -port
            return new int[] { 0, Integer.parseInt(ports.substring(1))};
        } else if (minIndex == ports.length() - 1) {
            // port-
            return new int[] { Integer.parseInt(ports.substring(0, minIndex)),
                    65535};
        } else if (minIndex > 0) {
            // port-port
            final int p1 = Integer.parseInt(ports.substring(0, minIndex));
            final int p2 = Integer.parseInt(ports.substring(minIndex + 1));
            return new int[] { p1, p2};
        } else {
            // port
            final int p = Integer.parseInt(ports);
            return new int[] { p, p};
        }
    }

    private final int getMask(String actions) {
        int mask = 0;
        final StringTokenizer tok = new StringTokenizer(actions.toLowerCase(),
                ",");
        while (tok.hasMoreTokens()) {
            final String t = tok.nextToken();
            if (t.equals("connect")) {
                mask |= CONNECT | RESOLVE;
            } else if (t.equals("accept")) {
                mask |= ACCEPT | RESOLVE;
            } else if (t.equals("listen")) {
                mask |= LISTEN | RESOLVE;
            } else if (t.equals("resolve")) {
                mask |= RESOLVE;
            }
        }
        return mask;
    }

    static class SocketPermissionCollection extends PermissionCollection {

        private final Vector list = new Vector();

        /**
         * @see java.security.PermissionCollection#add(java.security.Permission)
         */
        public void add(Permission perm) {
            if (isReadOnly()) { throw new SecurityException("ReadOnly"); }
            if (!(perm instanceof SocketPermission)) { throw new IllegalArgumentException(
                    "No SocketPermission"); }
            list.add(perm);
        }

        /**
         * @see java.security.PermissionCollection#elements()
         */
        public Enumeration elements() {
            return list.elements();
        }

        /**
         * @see java.security.PermissionCollection#implies(java.security.Permission)
         */
        public boolean implies(Permission perm) {
            for (Iterator i = list.iterator(); i.hasNext();) {
                final Permission mine = (Permission) i.next();
                if (mine.implies(perm)) { return true; }
            }
            return false;
        }
    }
}
