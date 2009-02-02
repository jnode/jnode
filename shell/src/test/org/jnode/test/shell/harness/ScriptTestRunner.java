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
 
package org.jnode.test.shell.harness;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;


/**
 * This TestRunner runs a script.  Typically the first line of the script will
 * be a "#!" line that says what interpreter to use.
 * 
 * @author crawley@jnode.org
 */
class ScriptTestRunner extends JNodeTestRunnerBase implements TestRunnable {

    private File tempScriptFile;
    
    public ScriptTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
//      String[] args = spec.getArgs().toArray(new String[0]);
//      CommandLine cmdLine = new CommandLine(spec.getCommand(), args);
        tempScriptFile = new File(System.getProperty("java.io.tmpdir"), spec.getCommand());
        Writer w = null;
        try {
            w = new FileWriter(tempScriptFile);
            w.write(spec.getScriptContent().toString());
            w.write('\n');
        } finally {
            w.close();
        }
        int rc = getShell().runCommandFile(tempScriptFile);
        return check(rc) ? 0 : 1;
    }

    private boolean check(int rc) {
        return 
            harness.expect(rc, spec.getRc(), "return code") &
            harness.expect(outBucket.toString(), spec.getOutputContent(), "output content") &
            harness.expect(errBucket.toString(), spec.getErrorContent(), "err content");
    }
    
    @Override
    public void cleanup() {
        if (tempScriptFile != null) {
            tempScriptFile.delete();
        }
        super.cleanup();
    }
    
}
