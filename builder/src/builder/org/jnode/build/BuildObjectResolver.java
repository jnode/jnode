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

package org.jnode.build;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.vmmagic.unboxed.UnboxedObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BuildObjectResolver extends ObjectResolver {

    private final NativeStream os;
    //private final AbstractBootImageBuilder builder;

    public BuildObjectResolver(NativeStream os, AbstractBootImageBuilder builder) {
        this.os = os;
        //this.builder = builder;
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOf32(java.lang.Object)
     */
    public int addressOf32(Object object) {
        if (object instanceof UnboxedObject) {
            return ((UnboxedObject) object).toInt();
        }
        final NativeStream.ObjectRef ref = os.getObjectRef(object);
        try {
            if (!ref.isResolved()) {
                throw new RuntimeException("Unresolved object " + object);
                //builder.emitObject(os, object);
            }
            final int offset = (int) os.getBaseAddr() + ref.getOffset();
            return offset;
            //} catch (ClassNotFoundException ex) {
            //throw new RuntimeException("Unresolved object ref", ex);
        } catch (UnresolvedObjectRefException ex) {
            throw new RuntimeException("Unresolved object ref", ex);
        }
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOf64(java.lang.Object)
     */
    public long addressOf64(Object object) {
        if (object instanceof UnboxedObject) {
            return ((UnboxedObject) object).toLong();
        }
        final NativeStream.ObjectRef ref = os.getObjectRef(object);
        try {
            if (!ref.isResolved()) {
                throw new RuntimeException("Unresolved object " + object);
                //builder.emitObject(os, object);
            }
            final long offset = os.getBaseAddr() + ref.getOffset();
            return offset;
            //} catch (ClassNotFoundException ex) {
            //throw new RuntimeException("Unresolved object ref", ex);
        } catch (UnresolvedObjectRefException ex) {
            throw new RuntimeException("Unresolved object ref", ex);
        }
    }
}
