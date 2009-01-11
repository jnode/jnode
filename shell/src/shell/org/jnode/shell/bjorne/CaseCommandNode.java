/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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
package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

public class CaseCommandNode extends CommandNode {

    private final BjorneToken word;

    private final CaseItemNode[] caseItems;

    public CaseCommandNode(BjorneToken word, CaseItemNode[] caseItems) {
        super(BjorneInterpreter.CMD_CASE);
        this.word = word;
        this.caseItems = caseItems;
    }

    public CaseItemNode[] getCaseItems() {
        return caseItems;
    }

    public BjorneToken getWord() {
        return word;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CaseCommand{").append(super.toString());
        sb.append(",word=").append(word);
        if (caseItems != null) {
            sb.append(",caseItems=");
            CommandNode.appendArray(sb, caseItems);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int rc = -1;
        
        CharSequence expandedWord = context.expand(word.text);
        for (CaseItemNode caseItem : caseItems) {
            for (BjorneToken pattern : caseItem.getPattern()) {
                CharSequence pat = context.expand(pattern.text);
                if (context.patternMatch(expandedWord, pat)) {
                    throw new ShellException("not implemented yet");
                }
            }
        }
        
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
