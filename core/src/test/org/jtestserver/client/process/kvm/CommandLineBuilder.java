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

public class CommandLineBuilder {
    private final List<String> line = new ArrayList<String>();
    
    public CommandLineBuilder(String arg) {
        append(arg);
    }

    public CommandLineBuilder append(String arg) {
        if (arg.indexOf(' ') >= 0) {
            append(arg.split(" "));
        } else {
            line.add(arg);
        }
        
        return this;
    }
    
    public CommandLineBuilder append(String... args) {
        for (int i = 0; i < args.length; i++) {
            line.add(args[i]);
        }
        return this;
    }

    public CommandLineBuilder append(int arg) {
        return append(Integer.toString(arg));
    }

    public String[] toArray() {
        return line.toArray(new String[line.size()]);
    }

}
