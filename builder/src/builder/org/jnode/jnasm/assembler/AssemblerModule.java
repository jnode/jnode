/*
 * $Id$
 */
package org.jnode.jnasm.assembler;

import org.jnode.assembler.x86.X86Assembler;

import java.util.Map;
import java.util.List;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class AssemblerModule {
    protected final Map labels;

    protected AssemblerModule(final Map labels) {
        this.labels = labels;
    }

    protected abstract boolean emmit(String mnemonic, List operands, X86Assembler asm);
}
