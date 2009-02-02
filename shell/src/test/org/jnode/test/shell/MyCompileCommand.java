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
 
package org.jnode.test.shell;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

public class MyCompileCommand extends AbstractCommand {

    private final int maxLevel = 5;

    private final ClassNameArgument ARG_CLASS =
        new ClassNameArgument("className", Argument.MANDATORY, "the class file to compile");
    private final IntegerArgument ARG_LEVEL =
        new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, "the optimization level");
    private final FlagArgument ARG_TEST =
        new FlagArgument("test", Argument.OPTIONAL, "when the test versions of the compilers will be used");

    public MyCompileCommand() {
        super("compile a Java class (bytecodes) to native code");
        registerArguments(ARG_CLASS, ARG_LEVEL, ARG_TEST);
    }

    @Override
    public void execute() throws Exception {
        // nothing to see here, move along
    }

}
