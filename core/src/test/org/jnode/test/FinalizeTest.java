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
 
package org.jnode.test;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FinalizeTest {

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new FinalizeTest(i);
        }
        Runtime.getRuntime().gc();
        System.out.println("Done");
    }
    
    private final int i;
    
    public FinalizeTest(int i) {
        this.i = i;
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    public void finalize() {
        System.out.println("Finalize called on " + i);
    }
    
}
