/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.facade;

/**
 * An {@link ObjectFilter} that accepts all objects.
 * Call {@link NoObjectFilter#INSTANCE} to get the singleton.  
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class NoObjectFilter implements ObjectFilter {
    public static final NoObjectFilter INSTANCE = new NoObjectFilter();
    
    private NoObjectFilter() {        
    }
    
    /**
     * {@inheritDoc}
     * <br>This implementation always returns true.
     */
    @Override
    public final boolean accept(String className) {
        return true;
    }
}
