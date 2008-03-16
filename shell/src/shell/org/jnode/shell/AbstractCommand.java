/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.vm.VmExit;

/**
 * This base class for Command objects just provides some convenience methods.
 * 
 * @author crawley@jnode.org
 * 
 */
public abstract class AbstractCommand implements Command {
    
    private ArgumentBundle bundle;
    
    public AbstractCommand() {
        this.bundle = null;
    }

    public AbstractCommand(String description) {
        this.bundle = new ArgumentBundle(description);
    }

    @SuppressWarnings("deprecation")
    public final void execute(String[] args) throws Exception {
        execute(new CommandLine(args), System.in, System.out, System.err);
    }

    /**
     * Exit this command with the given return code.
     * 
     * @param rc
     */
    protected void exit(int rc) {
        throw new VmExit(rc);
    }

    public final ArgumentBundle getArgumentBundle() {
		return bundle;
	}
    
    protected final void registerArguments(Argument<?> ... args) {
        if (bundle == null) {
            bundle = new ArgumentBundle();
        }
        for (Argument<?> arg : args) {
            bundle.addArgument(arg);
        }
    }
}
