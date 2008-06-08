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

package org.jnode.net.ipv4.util;

import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.Resolver;
import org.jnode.net.ipv4.IPv4Address;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

/**
 * @author Martin Hartvig
 */
public class ResolverImpl implements Resolver {
    // FIXME ... upgrade to a more recent version of xbill?
    
    // FIXME ... this class looks like it is supposed to implement
    // the Singleton pattern.  So how come the management methods
    // and a lot of the state is 'static'?
    private static ExtendedResolver resolver;

    private static Map<String, org.xbill.DNS.Resolver> resolvers;

    private static Map<String, ProtocolAddress[]> hosts;

    private static Resolver res = null;

    static {
        // FIXME should this come from a hosts file?
        hosts = new HashMap<String, ProtocolAddress[]>();
        final String localhost = "localhost";
        ProtocolAddress[] protocolAddresses = 
            new ProtocolAddress[] { new IPv4Address("127.0.0.1") };
        hosts.put(localhost, protocolAddresses);
        
        resolvers = new HashMap<String, org.xbill.DNS.Resolver>();
    }

    private ResolverImpl() {
    }

    /**
     * Singleton
     *
     * @return the singleton of the resolver
     */
    public static Resolver getInstance() {
        if (res == null) {
            // FIXME ... do we REALLY have to do this???
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    System.setProperty("dns.server", "127.0.0.1");
                    System.setProperty("dns.search", "localdomain");
                    return null;
                }
            });
            res = new ResolverImpl();
        }
        return res;
    }

    /**
     * Get list all the dns servers
     */
    public static Collection<String> getDnsServers() {
        return resolvers.keySet();
    }

    /**
     * Add a dns server
     *
     * @param _dnsserver
     * @throws NetworkException
     */

    public static void addDnsServer(ProtocolAddress _dnsserver)
            throws NetworkException {
        try {
            if (resolver == null) {
                final String[] server = new String[]{_dnsserver.toString()};
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            resolver = new ExtendedResolver(server);
                            Lookup.setDefaultResolver(resolver);
                            return null;
                        }
                    });
                } catch (PrivilegedActionException x) {
                    Exception ee = x.getException();
                    if (ee instanceof UnknownHostException) {
                        throw (UnknownHostException) ee;
                    }
                    else {
                        throw new RuntimeException(ee);
                    }
                }
                resolvers.put(_dnsserver.toString(), resolver);
            }
        } catch (UnknownHostException e) {
            throw new NetworkException("Can't add DNS server", e);
        }

        try {
            String key = _dnsserver.toString();

            if (!resolvers.containsKey(key)) {
                SimpleResolver simpleResolver = new SimpleResolver(key);

                resolver.addResolver(simpleResolver);
                resolvers.put(key, simpleResolver);
            }
        } catch (UnknownHostException e) {
            throw new NetworkException("Can't add DNS server", e);
        }
    }

    /**
     * removes a dns server
     *
     * @param _dnsserver
     */
    public static void removeDnsServer(ProtocolAddress _dnsserver) {

        if (resolver == null) {
            return;
        }

        String key = _dnsserver.toString();
        if (resolvers.containsKey(key)) {
            org.xbill.DNS.Resolver resolv = resolvers.remove(key);
            if (resolver.getResolvers().length == 1) {
                resolver = null;
            } else {
                resolver.deleteResolver(resolv);
            }
        }
    }

    /**
     * Get from hosts file.
     *
     * @param _hostname
     * @return
     */
    private ProtocolAddress[] getFromHostsFile(String _hostname) {
        // FIXME ... check for changes to the hosts file?
        return (ProtocolAddress[]) hosts.get(_hostname);
    }

    /**
     * Gets the address(es) of the given hostname.
     * 
     * @param hostname
     * @return All addresses of the given hostname. The returned array is at
     *         least 1 address long.
     * @throws java.net.UnknownHostException
     */
    public ProtocolAddress[] getByName(final String hostname)
            throws UnknownHostException {
        if (hostname == null) {
            throw new UnknownHostException("null");
        }
        if (hostname.equals("*")) {
            // FIXME ... why is this a special case?  Comment please or fix it.
            throw new UnknownHostException("*");
        }
        if (resolver == null) {
            throw new UnknownHostException(hostname);
        }

        final PrivilegedExceptionAction action = new PrivilegedExceptionAction() {
            public Object run() throws UnknownHostException {
                ProtocolAddress[] protocolAddresses;

                // FIXME ... hard-wired policy that 'hosts' file would be consulted
                // first.  Should be configurable.
                protocolAddresses = getFromHostsFile(hostname);
                if (protocolAddresses != null) {
                    return protocolAddresses;
                }

                Lookup.setDefaultResolver(resolver);

                final Lookup lookup;
                try {
                    lookup = new Lookup(hostname);
                } catch (TextParseException e) {
                    throw new UnknownHostException(hostname);
                }

                lookup.run();

                if (lookup.getResult() == Lookup.SUCCESSFUL) {
                    final Record[] records = lookup.getAnswers();
                    final int recordCount = records.length;

                    protocolAddresses = new ProtocolAddress[recordCount];

                    for (int i = 0; i < recordCount; i++) {
                        final Record record = records[i];
                        protocolAddresses[i] = new IPv4Address(record
                                .rdataToString());
                    }
                } else {
                    throw new UnknownHostException(lookup.getErrorString());
                }

                return protocolAddresses;
            }
        };
        try {
            return (ProtocolAddress[]) AccessController.doPrivileged(action);
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof UnknownHostException) {
                throw (UnknownHostException) ex.getException();
            } else {
                throw (UnknownHostException) new UnknownHostException()
                        .initCause(ex.getException());
            }
        }
    }

    /**
     * Gets the hostname of the given address.
     *
     * @param address
     * @return All hostnames of the given hostname. The returned array is at
     *         least 1 hostname long.
     * @throws java.net.UnknownHostException
     */

    public String[] getByAddress(ProtocolAddress address)
            throws UnknownHostException {
        // FIXME ... implement this method properly.
        return new String[0];
    }

}
