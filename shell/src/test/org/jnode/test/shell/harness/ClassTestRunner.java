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
import java.lang.reflect.Method;

/**
 * This TestRunner runs a a class by calling its 'static void main(Sting[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class ClassTestRunner implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
    private final TestSpecification spec;
    private final TestHarness harness;
    
    public ClassTestRunner(TestSpecification spec, TestHarness harness) {
        this.spec = spec;
        this.harness = harness;
    }

    @Override
    public int run() throws Exception {
        Class<?> commandClass = Class.forName(spec.getCommand());
        Method method = commandClass.getMethod("main", String[].class);
        String[] args = spec.getArgs().toArray(new String[0]);
        method.invoke(null, (Object) args);
        return check() ? 0 : 1;
    }

    private boolean check() {
        // When a class is run this way we cannot capture the RC.
        return 
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