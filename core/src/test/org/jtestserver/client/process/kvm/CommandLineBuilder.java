/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.process.kvm;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to build a command line.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class CommandLineBuilder {
    /**
     * The list of argument used to build the command line.
     */
    private final List<String> line = new ArrayList<String>();
    
    /**
     * 
     * @param arg first argument of the command line.
     */
    public CommandLineBuilder(String arg) {
        append(arg);
    }

    /**
     * Append an argument to the command line.
     * @param arg a {@link String} argument
     * @return
     */
    public CommandLineBuilder append(String arg) {
        if (arg.indexOf(' ') >= 0) {
            append(arg.split(" "));
        } else {
            line.add(arg);
        }
        
        return this;
    }
    
    /**
     * Append a list of arguments to the command line.
     * @param arg a list of {@link String} arguments
     * @return
     */
    public CommandLineBuilder append(String... args) {
        for (int i = 0; i < args.length; i++) {
            line.add(args[i]);
        }
        return this;
    }

    /**
     * Append an integer argument to the command line.
     * 
     * @param arg an integer argument
     * @return
     */
    public CommandLineBuilder append(int arg) {
        return append(Integer.toString(arg));
    }

    /**
     * Convert the command line to a {@link String} array ready to be used by 
     * {@link Runtime#exec(String[])} or one of its alternatives.
     * @return
     */
    public String[] toArray() {
        return line.toArray(new String[line.size()]);
    }

}
