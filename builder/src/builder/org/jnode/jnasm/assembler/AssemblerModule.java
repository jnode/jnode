/*
 * $Id$
 */
package org.jnode.jnasm.assembler;

import org.jnode.assembler.NativeStream;

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

    public abstract boolean emmit(String mnemonic, List operands, int oprandSize);
    public abstract void setNativeStream(NativeStream stream);
}
