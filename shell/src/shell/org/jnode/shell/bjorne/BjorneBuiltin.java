/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellInvocationException;
import org.jnode.vm.VmExit;

/**
 * Bjorne builtin commands must extend this class.  Each one should define a static final 
 * FACTORY object that assembles a BjorneBuiltinCommandInfo instance including an 
 * instance of the command object and the command's syntax bundle.
 * 
 * @author crawley@jnode.org
 *
 */
abstract class BjorneBuiltin extends AbstractCommand {
    
    static interface Factory {
        BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context);
    }
    
    private BjorneContext parentContext;
    
    public BjorneBuiltin(String description) {
        super(description);
    }
    
    /**
     * Temporary adapter method.  Unconverted builtin classes override this.
     */
    int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        setParentContext(context.getParent());
        initialize(command, command.getStreams());
        try {
            execute();
        } catch (VmExit ex) {
            return ex.getStatus();
        } catch (Exception ex) {
            throw new ShellInvocationException("Exception in bjorne builtin", ex);
        }
        return 0;
    }

    void error(String msg, BjorneContext context) {
        context.resolvePrintStream(context.getIO(Command.STD_ERR)).println(msg);
    }

    void setParentContext(BjorneContext parentContext) {
        this.parentContext = parentContext;
    }

    BjorneContext getParentContext() {
        return parentContext;
    }
}
