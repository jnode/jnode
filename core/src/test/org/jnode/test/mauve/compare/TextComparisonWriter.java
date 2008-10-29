package org.jnode.test.mauve.compare;

import java.io.PrintWriter;

/**
 * Writer in Text format for a {@link Comparison}
 * @author fabien
 *
 */
public class TextComparisonWriter extends ComparisonWriter {
    @Override
    protected Visitor createVisitor(PrintWriter pw) {
        return new TextVisitor(pw);
    }
    
    protected static class TextVisitor extends Visitor { 
        private TextVisitor(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        protected void writeSummary(int nbRegressions, int nbProgressions, int nbStagnations) {
            pw.append(Integer.toString(nbRegressions)).append(" regressions. ");
            pw.append(Integer.toString(nbProgressions)).append(" progressions. ");
            pw.append(Integer.toString(nbStagnations)).append(" stagnations.\n");
        }
        
        public void writeBeginTable() {
            pw.append("\n").append(evolutionLabel).append("\n");
        }
        
        @Override
        protected void writeBeginLine(Level level) {
            writeIndent(level);
        }
        
        @Override
        protected void writeName(Level level, String name) {
            pw.append(name).append('\t');
        }
        
        @Override
        protected void writeEndLine() {
            pw.append('\n');
        }
        
        @Override
        protected void writeCheckResult(String result) {
            pw.append('\t').append(result);
        }
    };
}
