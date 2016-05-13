/*
 * $Id$
 *
 * Copyright (C) 2003-2016 JNode.org
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
 
package org.jnode.jnasm.util;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86TextAssembler;
import org.jnode.util.NumberUtils;

/**
 * X86Assembler factory for creating an composed assembler stream which dispatches to a {@code X86TextAssembler} and a
 * {@code X86BinaryAssembler} instance.
 *
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86DualAssemblerFactory implements InvocationHandler {
    private X86TextAssembler textAssembler;
    private X86BinaryAssembler binaryAssembler;
    private boolean listing = false;

    private X86DualAssemblerFactory(X86TextAssembler textAssembler, X86BinaryAssembler binaryAssembler) {
        this.textAssembler = textAssembler;
        this.binaryAssembler = binaryAssembler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret;
        if (method.getName().startsWith("write")) {
            int pos1 = binaryAssembler.getLength();
            ret = method.invoke(binaryAssembler, args);
            int pos2 = binaryAssembler.getLength();
            if (listing) {
                textAssembler.print(NumberUtils.hex(pos1));
            }
            textAssembler.print("  ");
            int ln = pos2 - pos1;
            String msg;
            if (ln < 8) {
                if (listing) {
                    msg = NumberUtils.hexCompact(binaryAssembler.getBytes(), pos1, pos2 - pos1);
                    textAssembler.print(msg);

                    if (msg.length() < 18) {
                        for (int i = 0; i < 17 - msg.length(); i++) {
                            textAssembler.print(" ");
                        }
                    } else {
                        textAssembler.print("  ");
                    }
                }
                method.invoke(textAssembler, args);

            } else {
                if (listing) {
                    msg = NumberUtils.hexCompact(binaryAssembler.getBytes(), pos1, 8);
                    textAssembler.print(msg);
                }
                method.invoke(textAssembler, args);
                if (listing) {
                    msg = "         -" + NumberUtils.hexCompact(binaryAssembler.getBytes(), pos1 + 8, ln - 8);
                    textAssembler.println(msg);
                }
            }
        } else {
            method.invoke(textAssembler, args);
            ret = method.invoke(binaryAssembler, args);
        }
        if (textAssembler.getLength() % 100000 > 80000) {
            textAssembler.flush();
        }
        if (method.getName().equals("getBytes")) {
            textAssembler.flush();
        }
        return ret;
    }

    public static X86Assembler create(X86TextAssembler textAssembler,X86BinaryAssembler binaryAssembler) {
        return (X86Assembler) Enhancer.create(X86Assembler.class, new X86DualAssemblerFactory(textAssembler,
            binaryAssembler));
    }
}
