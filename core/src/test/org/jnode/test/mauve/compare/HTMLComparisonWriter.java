package org.jnode.test.mauve.compare;

import java.io.PrintWriter;

import org.jnode.test.mauve.XMLReportWriter;

/**
 * Writer in HTML format for a {@link Comparison}
 * @author fabien
 *
 */
public class HTMLComparisonWriter extends ComparisonWriter {
    @Override
    protected Visitor createVisitor(PrintWriter pw) {
        return new HTMLVisitor(pw);
    }
    
    protected static class HTMLVisitor extends Visitor { 
        private HTMLVisitor(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        protected void writeSummary(int nbRegressions, int nbProgressions, int nbStagnations) {
            pw.append("<h2>Summary</h2>");
            appendLink(nbRegressions, EvolutionType.REGRESSION, " regressions. ");            
            appendLink(nbProgressions, EvolutionType.PROGRESSION, " progressions. ");
            appendLink(nbStagnations, EvolutionType.STAGNATION, " stagnations. ");
        }
        
        private void appendLink(int value, EvolutionType type, String label) {
            pw.append("<a href=\"#").append(type.toString()).append("\">");
            pw.append(Integer.toString(value)).append(label);
            pw.append("</a>").append("&nbsp;&nbsp;&nbsp;");;
        }
        
        public void writeBegin() {
            pw.append("<html><head></head><body>");
        }

        public void writeEnd() {
            pw.append("</body></html>\n");
        }

        public void writeBeginTable() {
            pw.append("<br/><h2 id=\"").append(type.toString()).append("\">");
            pw.append(evolutionLabel);
            pw.append("</h2><br/>");
            
            pw.append("<table border=\"1\" cellspacing=\"1\" cellpadding=\"1\"><tr>");
            
            writeCell("th", 0, Level.values().length, "Name");            
            writeCell("th", 0, 1, "Last reached checkpoint");
            
            pw.append("</tr>\n");
        }

        public void writeEndTable() {
            pw.append("\n</table>");
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
