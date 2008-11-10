// Copyright (c) 2008 Fabien DUMINY (fduminy@jnode.org)

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

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
