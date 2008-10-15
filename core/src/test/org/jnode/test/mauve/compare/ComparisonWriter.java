package org.jnode.test.mauve.compare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.jnode.test.mauve.CheckResult;

public class ComparisonWriter {
    /**
     * Write the given comparison
     * 
     * @param comp
     * @param output
     * @throws FileNotFoundException
     */
    public void write(RunComparison comp, File output) throws FileNotFoundException {
        PrintWriter ps = null;

        try {
            ps = new PrintWriter(new FileOutputStream(output));
            write(comp, ps);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    /**
     * Write the given comparison
     * 
     * @param run
     * @param pw
     */
    public void write(RunComparison run, PrintWriter pw) {

        pw.append(run.getName()).append('\n');
        
        // package
        for (Comparison<?> pkg : run) {
            write(pw, 4, pkg, true);
            
            // class
            for (Comparison<?> cls : pkg) {
                write(pw, 8, cls, true);
                
                // test
                for (Comparison<?> test : cls) {
                    write(pw, 12, test, false);
                    
                    TestComparison tc = (TestComparison) test;
                    CheckResult cr = tc.getCheckResult();
                    pw.append('\t');
                    
                    if (cr == null) {
                        pw.append("<no checkpoint>");
                    } else {
                        pw.append(Integer.toString(cr.getNumber())).append(':');
                        pw.append(cr.getCheckPoint());
                    }
                    
                    pw.append('\n');
                }
            }
        }

        pw.flush();
    }
    
    private void write(PrintWriter pw, int indent, Comparison<?> comp, boolean endLine) {
        for (int i = 0; i < indent; i++) {
            pw.append(' ');
        }
        
        pw.append(comp.getName()).append('\t');
        pw.append(Integer.toString(comp.getProgression()));
        
        if (endLine) {
            pw.append('\n');
        }
    }
}
