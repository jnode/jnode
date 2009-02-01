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
 
package org.jnode.net.ipv4;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;

/**
 * Class used to reconstruct fragmented IP packets
 * 
 * @author epr
 */
public class IPv4FragmentList implements IPv4Constants {

    /** My logger */
    private static final Logger log = Logger.getLogger(IPv4FragmentList.class);
    /** When was this object created */
    private final long creationTime;
    /** List of fragments */
    private final ArrayList<SocketBuffer> fragments;
    /** The key of this fragment list */
    private final Object key;
    /** Is the first fragment in the list? */
    private boolean haveFirstFragment;
    /** Is the last fragment in the list? */
    private boolean haveLastFragment;

    /**
     * Create a new instance
     * 
     * @param firstFragment
     */
    public IPv4FragmentList(SocketBuffer firstFragment) {
        this.creationTime = System.currentTimeMillis();
        this.fragments = new ArrayList<SocketBuffer>();
        this.haveFirstFragment = false;
        this.haveLastFragment = false;
        final IPv4Header hdr = (IPv4Header) firstFragment.getNetworkLayerHeader();
        this.key = hdr.getFragmentListKey();
        add(firstFragment);
    }

    /**
     * Add a packet to this object.
     * 
     * @param skbuf
     */
    public void add(SocketBuffer skbuf) {
        final IPv4Header hdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        if (!hdr.isFragment()) {
            throw new IllegalArgumentException("Buffer does not contain a fragment");
        }
        final int myFrOfs = hdr.getFragmentOffset();
        final int mySize = hdr.getDataLength();

        // Fixup some member variables
        this.haveFirstFragment |= (myFrOfs == 0);
        this.haveLastFragment |= !hdr.hasMoreFragments();

        // Insert the fragment at the correct index in the list
        for (Iterator<SocketBuffer> i = fragments.iterator(); i.hasNext();) {
            final SocketBuffer f = (SocketBuffer) i.next();
            final IPv4Header fhdr = (IPv4Header) f.getNetworkLayerHeader();
            final int fOfs = fhdr.getFragmentOffset();
            final int fSize = f.getSize();

            if (myFrOfs == (fOfs + fSize)) {
                // skbuf directly follows f, attach it.
                f.append(skbuf);

                // See if we can attach the following fragment directly to me
                if (i.hasNext()) {
                    final SocketBuffer f2 = (SocketBuffer) i.next();
                    final IPv4Header f2hdr = (IPv4Header) f2.getNetworkLayerHeader();
                    final int f2Ofs = f2hdr.getFragmentOffset();
                    if (f2Ofs == (myFrOfs + skbuf.getSize())) {
                        // Yes we can attach it
                        skbuf.append(f2);
                        fragments.remove(f2);
                    }
                }
                return;
            } else if (myFrOfs < fOfs) {
                // skbuf is before f, insert it here
                fragments.add(fragments.indexOf(f), skbuf);
                return;
            } else if (myFrOfs < (fOfs + fSize)) {
                // Fragment offset in the middle of an existing fragment: this is an error!
                log.debug("Fragment offset(" + myFrOfs + mySize + "," +
                        ") falls in another fragment (" + fOfs + "," + fSize + ").");
                return;
            }
        }
        // The fragment has not been added, so it must be the last on the list
        // with a gap before it.
        fragments.add(skbuf);
    }

    /**
     * Is this fragmentlist still alive. A fragmentlist is alive when is was
     * created no more then IP_FRAGTIMEOUT milliseconds ago.
     */
    public boolean isAlive() {
        final long now = System.currentTimeMillis();
        return ((now - creationTime) <= IP_FRAGTIMEOUT);
    }

    /**
     * Do we have all fragments?
     */
    public boolean isComplete() {
        if (haveFirstFragment && haveLastFragment) {
            if (fragments.size() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the complete packet
     * This method can only be called when <code>isComplete</code> returns true.
     */
    public SocketBuffer getPacket() {
        return (SocketBuffer) fragments.get(0);
    }

    /**
     * Gets the key of this fragmentlist.
     * @see IPv4Header#getFragmentListKey()
     */
    public Object getKey() {
        return key;
    }
}
