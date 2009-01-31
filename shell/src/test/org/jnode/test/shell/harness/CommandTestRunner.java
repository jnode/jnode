/*
 * $Id: NameSpace.java 4564 2008-09-18 22:01:10Z fduminy $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandShell;


/**
 * This TestRunner runs a class by calling its 'static void main(String[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class CommandTestRunner extends JNodeTestRunnerBase implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
        
    public CommandTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
        StringBuffer sb = new StringBuffer();
        CommandShell shell = getShell();
        sb.append(shell.escapeWord(spec.getCommand()));
        for (String arg : spec.getArgs()) {
            sb.append(" ").append(shell.escapeWord(arg));
        }
        int rc = shell.runCommand(sb.toString());
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
    }

    @Override
    public void setup() {
        System.setIn(new ByteArrayInputStream(spec.getInputContent().toString().getBytes()));
        outBucket = new ByteArrayOutputStream();
        errBucket = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBucket));
        System.setErr(new PrintStream(errBucket));
    }
    
}