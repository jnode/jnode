/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface RegisterVisitor {

    public void visit(Register reg);
    
}
