package org.jnode.test.mauve.compare;

import java.io.PrintWriter;

import org.jnode.test.mauve.XMLReportWriter;

/**
 * Writer in HTML format for a {@link Comparison}
 * @author fabien
 *
 */
public class HTMLComparisonWriter extends ComparisonWriter {
    private static final String STAGNATION_COLOR = "green";
    private static final String PROGRESSION_COLOR = "cyan";
    private static final String REGRESSION_COLOR = "red";
    
    @Override
    protected Visitor createVisitor(PrintWriter pw) {
        return new TextVisitor(pw);
    }
    
    protected static class TextVisitor extends Visitor { 
        private TextVisitor(PrintWriter pw) {
            super(pw);
        }
        
        public void writeBegin() {
            pw.append("<html><head></head><body>");
            pw.append("<table border=\"1\" cellspacing=\"1\" cellpadding=\"1\"><tr>");
            
            writeCell("th", 0, Level.values().length, "Name");            
            writeCell("th", 0, 1, "Progress");            
            writeCell("th", 0, 1, "Last reached checkpoint");
            
            pw.append("</tr>\n");
        }

        public void writeEnd() {
            pw.append("\n</table></body></html>\n");
        }
         
        @Override
        protected void writeBeginLine(Level level) {
            writeIndent(level);
            pw.write("<tr>");
        }
        
        @Override
        protected void writeName(Level level, String name) {
            writeCell("td", level.getValue(), 1 + Level.MAX.getValue() - level.getValue(), name);
        }
        
        @Override
        protected void writeProgression(int progression) {
            final String bgColor;
            if (progression == 0) {
                bgColor = STAGNATION_COLOR;
            } else if (progression < 0) {
                bgColor = REGRESSION_COLOR;
            } else {
                bgColor = PROGRESSION_COLOR;
            }
            
            writeCell("td", 0, 1, Integer.toString(progression), "text-align:right;", bgColor);
        }
        
        @Override
        protected void writeEndLine() {
            pw.write("</tr>\n");
        }
        
        @Override
        protected void writeCheckResult(String result) {
            writeCell("td", 0, 1, result);
        }

        private void writeCell(String tag, int nbColumnsBefore, int columnSpan, String value) {
            writeCell(tag, nbColumnsBefore, columnSpan, value, null);
        }

        private void writeCell(String tag, int nbColumnsBefore, int columnSpan, String value, String style) {
            writeCell(tag, nbColumnsBefore, columnSpan, value, style, null);
        }
        
        private void writeCell(String tag, int nbColumnsBefore, int columnSpan, String value, 
                               String style, String bgColor) {
            for (int i = 0; i < nbColumnsBefore; i++) {
                pw.append("<").append(tag).append(" width=\"30px\"></").append(tag).append(">");
            }
            
            pw.append("<").append(tag);
            if (style != null) {
                pw.append(" style=\"").append(style).append('\"');
            }
            if (columnSpan > 1) {
                pw.append(" colspan=\"").append(Integer.toString(columnSpan)).append('\"');
            }
            if (bgColor != null) {
                pw.append(" bgcolor=\"").append(bgColor).append('\"');
            }
            
            pw.append('>');
            
            pw.append(XMLReportWriter.protect(value));
            pw.append("</").append(tag).append(">");
        }
    };
}
