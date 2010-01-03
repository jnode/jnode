/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * This TestRunner runs a a class by calling its 'static void main(Sting[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 *
 * @author crawley@jnode.org
 */
class ClassTestRunner extends TestRunnerBase implements TestRunnable {

    public ClassTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
        Class<?> commandClass = Class.forName(spec.getCommand());
        Method method = commandClass.getMethod("main", String[].class);
        String[] args = spec.getArgs().toArray(new String[0]);
        try {
            method.invoke(null, (Object) args);
        } catch (Throwable ex) {
            Class<? extends Throwable> exception = spec.getException();
            if (exception != null && exception.isInstance(ex)) {
                // continue
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                throw (Exception) ex;
            }
        }
        flush();
        return check() ? 0 : 1;
    }

    private boolean check() throws IOException {
        // When a class is run this way we cannot capture the RC.
        return
            harness.expect(outBucket.toString(), spec.getOutputContent(), "output content") &
                harness.expect(errBucket.toString(), spec.getErrorContent(), "err content") &
                checkFiles();
    }
}
