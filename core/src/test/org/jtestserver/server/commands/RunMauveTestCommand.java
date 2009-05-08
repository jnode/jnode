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
package org.jtestserver.server.commands;



import gnu.testlet.runner.RunResult;
import gnu.testlet.runner.XMLReportWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.message.Descriptors;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.server.TestFailureException;

public class RunMauveTestCommand extends AbstractTestServerCommand<String> {
    private static final Logger LOGGER = Logger.getLogger(RunMauveTestCommand.class.getName());
    
    public RunMauveTestCommand() {
        super(Descriptors.RUN_MAUVE_TEST);
    }

    @Override
    protected String execute(Object[] params) throws ProtocolException, TimeoutException {
        String result = "";
        String test = (String) params[0];
        LOGGER.finer("running test " + test);
        
        MauveTestRunner runner = MauveTestRunner.getInstance();
        try {
            RunResult runResult = runner.runTest(test);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.write("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
            new XMLReportWriter(true).write(runResult, pw);
            result = sw.getBuffer().toString();
            LOGGER.log(Level.FINEST, "result=" + result);
        } catch (TestFailureException e) {
            LOGGER.log(Level.SEVERE, "error in execute", e);            
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "error in execute", t);            
        }
        
        return result;
    }
}
