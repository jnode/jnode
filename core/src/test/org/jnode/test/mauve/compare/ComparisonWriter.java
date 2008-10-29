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
        v.writeSummary(run);
        
        write(run, pw, v, EvolutionType.REGRESSION, "Regressions");
        write(run, pw, v, EvolutionType.PROGRESSION, "Progressions");
        write(run, pw, v, EvolutionType.STAGNATION, "Stagnations");
        
        v.writeEnd();
        
        pw.flush();
    }

    private void write(RunComparison run, PrintWriter pw, Visitor v, EvolutionType type, String typeLabel) {
        v.setType(type);
        v.setEvolutionLabel(typeLabel);
        
        v.writeBeginTable();
        run.accept(v);
        v.writeEndTable();
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
        protected EvolutionType type; 
        protected String evolutionLabel;
        
        protected Visitor(PrintWriter pw) {
            this.pw = pw;
        }
        
        public void setType(EvolutionType type) {
            this.type = type;
        }
        
        public void setEvolutionLabel(String label) {
            this.evolutionLabel = label;
        }

        abstract protected void writeSummary(int nbRegressions, int nbProgressions, int nbStagnations);
        
        private void writeSummary(RunComparison run) {
            EvolutionTypeVisitor evolTypeVisitor = new EvolutionTypeVisitor();
            run.accept(evolTypeVisitor);
            
            writeSummary(evolTypeVisitor.getCounter(EvolutionType.REGRESSION), 
                    evolTypeVisitor.getCounter(EvolutionType.PROGRESSION),
                    evolTypeVisitor.getCounter(EvolutionType.STAGNATION));
        }
        
        public void writeBegin() {
        }

        public void writeEnd() {
        }

        public void writeBeginTable() {
        }

        public void writeEndTable() {
        }
        
        @Override
        public final void visit(RunComparison run) {
            if (shouldWrite(run)) {
                write(Level.RUN, run, true);
            }
        }
    
        @Override
        public final void visit(PackageComparison pkg) {
            if (shouldWrite(pkg)) {
                write(Level.PACKAGE, pkg, true);
            }
        }
    
        @Override
        public final void visit(ClassComparison cls) {
            if (shouldWrite(cls)) {
                write(Level.CLASS, cls, true);
            }
        }
    
        @Override
        public final void visit(TestComparison test) {
            if (shouldWrite(test)) {
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
        }

        protected abstract void writeBeginLine(Level level);
        protected abstract void writeName(Level level, String name);
        protected abstract void writeEndLine();
        
        protected abstract void writeCheckResult(String result);

        protected final void writeIndent(Level level) {
            final int indent = level.getValue() * 4;
            for (int i = 0; i < indent; i++) {
                pw.append(' ');
            }
        }
        
        private boolean shouldWrite(Comparison<?> comp) {
            EvolutionTypeVisitor v = new EvolutionTypeVisitor();
            comp.accept(v);
            return (v.getCounter(type) > 0);
        }
        
        private void write(Level level, Comparison<?> comp, boolean endLine) {
            writeBeginLine(level);
            
            writeName(level, comp.getName());
            
            if (endLine) {
                writeEndLine();
            }
        }
    };
}
