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
package org.jtestserver.client.process.jvm;

import java.io.File;
import java.io.IOException;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.server.TestServer;

/**
 * Implementation of {@link ServerProcess} that starts a new JVM process.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class NewJVMServerProcess implements ServerProcess {
    /**
     * The {@link ProcessRunner} used to manage the new JVM process.
     */
    private ProcessRunner runner = new ProcessRunner();
    
    /**
     * 
     */
    public NewJVMServerProcess() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws IOException {
        String javaHome = "/home/fabien/apps/java/";
        String java = javaHome + "bin/java";
        String jnodeCore = "/home/fabien/data/Projets/JNode/jnode/core/";
        String classesDir = jnodeCore + "build/classes";
        String mainClass = TestServer.class.getName();
        String classpath = "." + File.pathSeparatorChar + jnodeCore + "lib/mauve.jar";
        String command = java + " -cp " + classpath + " " + mainClass;
        
        runner.setWorkDir(new File(classesDir));
        runner.execute(command);        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws IOException {
        runner.getProcess().destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {
        boolean alive = false;
        
        try {
            runner.getProcess().exitValue();
        } catch (IllegalThreadStateException e) {
            alive = true;
        }
        return alive;
    }
}
