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
        protected void writeBeginLine(Level level) {
            writeIndent(level);
        }
        
        @Override
        protected void writeName(Level level, String name) {
            pw.append(name).append('\t');
        }
        
        @Override
        protected void writeProgression(int progression) {
            pw.append(Integer.toString(progression));            
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
