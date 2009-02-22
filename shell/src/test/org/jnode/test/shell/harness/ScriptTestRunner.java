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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;


/**
 * This TestRunner runs a script.  Typically the first line of the script will
 * be a "#!" line that says what interpreter to use.
 * 
 * @author crawley@jnode.org
 */
class ScriptTestRunner extends TestRunnerBase implements TestRunnable {

    private File tempScriptFile;
    
    public ScriptTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
        Properties props = new Properties();
        String tempDir = System.getProperty("java.io.tmpdir");
        props.setProperty("TEMP_DIR", tempDir);
        tempScriptFile = new File(tempDir, spec.getCommand());
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(tempScriptFile));
            expand(props, spec.getScriptContent().toString(), bw, '@');
            bw.write('\n');
        } finally {
            bw.close();
        }
        int rc = getShell().runCommandFile(tempScriptFile, 
                spec.getCommand(), spec.getArgs().toArray(new String[0]));
        flush();
        return check(rc) ? 0 : 1;
    }

    private boolean check(int rc) throws IOException {
        return 
            harness.expect(rc, spec.getRc(), "return code") &
            harness.expect(outBucket.toString(), spec.getOutputContent(), "output content") &
            harness.expect(errBucket.toString(), spec.getErrorContent(), "err content") &
            checkFiles();
    }
    
    @Override
    public void cleanup() {
        if (tempScriptFile != null) {
            tempScriptFile.delete();
        }
        super.cleanup();
    }

    /**
     * Expand a '@...@' sequences in a template, writing the result to an
     * Writer. A sequence '@@' turns into a single '@'. A sequence
     * '@name@' expands to the value of the named property if it is defined in
     * the property set, or the sequence '@name@' if it does not. A CR, NL or
     * EOF in an '@...@' sequence is an error.
     * 
     * @param props the properties to be expanded
     * @param template is the template string
     * @param w the sink for the expanded template
     * @param marker the sequence marker character(defaults to '@')
     * @throws IOException
     * @throws TestSpecificationException 
     */
    private void expand(Properties props, String template, BufferedWriter w, char marker) 
        throws IOException, TestSpecificationException {
        int ch;
        Reader r = new StringReader(template);
        while ((ch = r.read()) != -1) {
            if (ch == marker) {
                StringBuffer sb = new StringBuffer(20);
                while ((ch = r.read()) != marker) {
                    switch (ch) {
                    case -1:
                        throw new TestSpecificationException("Encountered EOF in a " + marker +
                                "..." + marker + " sequence in script template");
                    case '\n':
                        throw new TestSpecificationException("Encountered newline in a " +
                                marker + "..." + marker + " sequence in script template");
                    default:
                        sb.append((char) ch);
                    }
                }
                if (sb.length() == 0) {
                    w.write(marker);
                } else {
                    String name = sb.toString();
                    String value = props.getProperty(name);
                    if (value == null) {
                        w.write(marker);
                        w.write(sb.toString().toCharArray());
                        w.write(marker);
                    } else {
                        w.write(value.toCharArray());
                    }
                }
            } else {
                w.write((char) ch);
            }
        }
    }
}
