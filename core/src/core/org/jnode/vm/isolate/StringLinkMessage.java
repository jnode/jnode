/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.vm.isolate;


final class StringLinkMessage extends LinkMessageImpl {

    private final String value;

    /**
     * Message constructor
     *
     * @param value
     */
    StringLinkMessage(String value) {
        this.value = value;
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        return new StringLinkMessage(new String(value));
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractString();
    }

    /**
     * @see javax.isolate.LinkMessage#containsString()
     */
    @Override
    public boolean containsString() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extractString()
     */
    @Override
    public String extractString() {
        return value;
    }
}
