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
package org.jtestserver.client;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.jtestserver.server.TestServer;

public class NewProcessLauncher implements TestServerLauncher {
    private static final Logger LOGGER = Logger.getLogger(NewProcessLauncher.class.getName());
    
    @Override
    public Process launch() throws IOException {
        String javaHome = "/home/fabien/apps/java/";
        String java = javaHome + "bin/java";
        String jnodeCore = "/home/fabien/data/Projets/JNode/jnode/core/";
        String classesDir = jnodeCore + "build/classes";
        String mainClass = TestServer.class.getName();
        String classpath = "." + File.pathSeparatorChar + jnodeCore + "lib/mauve.jar";
        String command = java + " -cp " + classpath + " " + mainClass;
        
        LOGGER.finer("command: " + command);
        return Runtime.getRuntime().exec(command, new String[0], new File(classesDir));
    }

}
