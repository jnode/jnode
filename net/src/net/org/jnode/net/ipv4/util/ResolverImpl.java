/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.net.ipv4.util;

import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

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
    private static ExtendedResolver resolver;

    private static Map<String, org.xbill.DNS.Resolver> resolvers;

    private static Map<String, ProtocolAddress[]> hosts;

    private static Resolver res = null;

    static {
        hosts = new HashMap<String, ProtocolAddress[]>();

        // this will have to come from hosts file
        final String localhost = "localhost";
        ProtocolAddress[] protocolAddresses = new ProtocolAddress[] { new IPv4Address(
                "127.0.0.1") };

        hosts.put(localhost, protocolAddresses);
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
            res = new ResolverImpl();
        }

        return res;
    }

    /**
     * List all the dns servers
     */

    public static void printDnsServers() {
        if (resolvers == null) {
            return;
        }

        for (String dnsServer : resolvers.keySet()) {
            System.out.println(dnsServer);
        }
    }

    /**
     * Add a dns server
     * 
     * @param _dnsserver
     * @throws NetworkException
     */

    public static void addDnsServer(ProtocolAddress _dnsserver)
            throws NetworkException {

        if (resolvers == null) {
            resolvers = new HashMap<String, org.xbill.DNS.Resolver>();
        }

        if (resolver == null) {
            try {
                if (resolver == null) {
                    String[] server = new String[] { _dnsserver.toString() };
                    resolver = new ExtendedResolver(server);

                    Lookup.setDefaultResolver(resolver);

                    resolvers.put(_dnsserver.toString(), resolver);
                }
            } catch (UnknownHostException e) {
                throw new NetworkException("Can't add DnsServer", e);
            }
        }

        try {
            String key = _dnsserver.toString();

            if (!resolvers.containsKey(key)) {
                SimpleResolver simpleResolver = new SimpleResolver(key);

                resolver.addResolver(simpleResolver);
                resolvers.put(key, simpleResolver);
            }
        } catch (UnknownHostException e) {
            throw new NetworkException("Can't add DnsServer", e);
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
            org.xbill.DNS.Resolver resolv = (org.xbill.DNS.Resolver) resolvers
                    .remove(key);

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
        // this should check for changes of the host file

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
        if (resolver == null) {
            throw new UnknownHostException(hostname);
        }

        final PrivilegedExceptionAction action = new PrivilegedExceptionAction() {
            public Object run() throws UnknownHostException {
                ProtocolAddress[] protocolAddresses;

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
        return new String[0]; // To change body of implemented methods use
        // Options | File Templates.
    }

}
