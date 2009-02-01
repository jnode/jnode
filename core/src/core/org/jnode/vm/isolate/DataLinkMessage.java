/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.vm.isolate;


final class DataLinkMessage extends LinkMessageImpl {

    private final byte[] bytes;

    private final int offset;

    private final int length;

    DataLinkMessage(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        final byte[] data = new byte[length];
        System.arraycopy(bytes, offset, data, 0, length);
        return new DataLinkMessage(data, 0, length);
    }

    /**
     * @see javax.isolate.LinkMessage#containsData()
     */
    @Override
    public boolean containsData() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractData();
    }

    /**
     * @see javax.isolate.LinkMessage#extractData()
     */
    @Override
    public byte[] extractData() {
        if ((offset == 0) && (length == bytes.length)) {
            return bytes;
        } else {
            byte[] data = new byte[length];
            System.arraycopy(bytes, offset, data, 0, length);
            return data;
        }
    }
}
