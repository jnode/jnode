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

package java.nio;

import gnu.classpath.Pointer;

public final class VMDirectByteBuffer {

	native static Pointer allocate(int capacity);

    native static void free(Pointer address);

	native static byte get(Pointer address, int index);

	native static void get(Pointer address, int index, byte[] dst, int offset, int length);

	native static void put(Pointer address, int index, byte value);

    native static void put(Pointer address, int index, byte[] src, int offset, int length);
    
	native static Pointer adjustAddress(Pointer address, int offset);

	native static void shiftDown(Pointer address, int dst_offset, int src_offset, int count);
}
