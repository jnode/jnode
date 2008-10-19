package org.jnode.test.mauve.compare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.jnode.test.mauve.CheckResult;

/**
 * Abstract class for writing a {@link Comparison}
 * 
 * @author fabien
 *
 */
public abstract class ComparisonWriter {
    
    /**
     * Write the given comparison
     * 
     * @param comp
     * @param output
     * @throws FileNotFoundException
     */
    public final void write(RunComparison comp, File output) throws FileNotFoundException {
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
    public final void write(RunComparison run, PrintWriter pw) {
        final Visitor v = createVisitor(pw);
        
        v.writeBegin();
        run.accept(v);
        v.writeEnd();
        
        pw.flush();
    }
    
    protected abstract Visitor createVisitor(PrintWriter pw);
    
    protected static enum Level {
        RUN,
        PACKAGE,
        CLASS,
        TEST;

        public static final Level MAX = values()[values().length - 1];
        
        public int getValue() {
            return ordinal();
        }
    }
    
    protected abstract static class Visitor implements ComparisonVisitor { 
        protected final PrintWriter pw;

        protected Visitor(PrintWriter pw) {
            this.pw = pw;
        }
        
        public void writeBegin() {
        }

        public void writeEnd() {
        }

        @Override
        public final void visit(RunComparison run) {
            write(Level.RUN, run, true);
        }
    
        @Override
        public final void visit(PackageComparison pkg) {
            write(Level.PACKAGE, pkg, true);
        }
    
        @Override
        public final void visit(ClassComparison cls) {
            write(Level.CLASS, cls, true);
        }
    
        @Override
        public final void visit(TestComparison test) {
            write(Level.TEST, test, false);
            
            CheckResult cr = test.getCheckResult();
            String result;
            if (cr == null) {
                result = "<no checkpoint>";
            } else {
                result = Integer.toString(cr.getNumber()) + ':';
                
                if (cr.getCheckPoint() == null) {
                    result += "<no name>";
                } else {
                    result += cr.getCheckPoint();
                }
            }
            writeCheckResult(result);
            
            writeEndLine();
        }

        protected abstract void writeBeginLine(Level level);
        protected abstract void writeName(Level level, String name);
        protected abstract void writeProgression(int progression);
        protected abstract void writeEndLine();
        
        protected abstract void writeCheckResult(String result);

        protected final void writeIndent(Level level) {
            final int indent = level.getValue() * 4;
            for (int i = 0; i < indent; i++) {
                pw.append(' ');
            }
        }

        private void write(Level level, Comparison<?> comp, boolean endLine) {
            writeBeginLine(level);
            
            writeName(level, comp.getName());
            writeProgression(comp.getProgression());
            
            if (endLine) {
                writeEndLine();
            }
        }
    };
}
