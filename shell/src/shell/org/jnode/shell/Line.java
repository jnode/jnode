/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.shell;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import org.jnode.driver.console.TextConsole;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
class Line {
	//TODO get the real screen width (in columns)
    final static private int SCREEN_WIDTH = 80;
    
    private int consoleX;

    private int consoleY;

    /**
     * Contains the current position of the cursor on the currentLine
     */
    private int posOnCurrentLine = 0;

    /** Contains the current line * */
    private StringBuffer currentLine = new StringBuffer(80);

    private boolean shortened = true;

    private int oldLength = 0;

    private int maxLength = 0;

    private final TextConsole console;

    private CommandShell shell;

    private PrintStream out;

    public Line(TextConsole console, CommandShell shell, PrintStream out) {
        this.console = console;
        this.shell = shell;
        this.out = out;
    }

    public void start() {
        start(false);
    }

    public boolean isEmpty() {
        return currentLine.toString().trim().length() == 0;
    }

    public void start(boolean keepContent) {
        if (keepContent) {
            // we stay at the same position in X coordinate
            // only Y may have changed
            consoleY = console.getCursorY();
        } else {
            consoleX = console.getCursorX();
            consoleY = console.getCursorY();

            setContent("");
            console.setCursor( consoleX,consoleY);//move the cursor to the start of the line.
        }
    }

    public String getContent() {
        return currentLine.toString();
    }

    public void setContent(String content) {
        startModif();
        currentLine.setLength(0);
        currentLine.append(content);
        moveEnd();
        endModif();
    }

    public boolean moveLeft() {
        if (posOnCurrentLine > 0) {
            posOnCurrentLine--;
            return true;
        }
        return false;
    }

    public boolean moveRight() {
        if (posOnCurrentLine < currentLine.length()) {
            posOnCurrentLine++;
            return true;
        }
        return false;
    }

    public void moveEnd() {
        posOnCurrentLine = currentLine.length();
    }

    public void moveBegin() {
        posOnCurrentLine = 0;
    }

    public boolean backspace() {
        if (posOnCurrentLine > 0) {
            moveLeft();
            delete();
            return true;
        }
        return false;
    }

    public void delete() {
        if ((posOnCurrentLine >= 0)
                && (posOnCurrentLine < currentLine.length())) {
            startModif();
            currentLine.deleteCharAt(posOnCurrentLine);
            endModif();
        }
    }

    public CompletionInfo complete(String currentPrompt) {
        CompletionInfo info = null;
        //int oldPosOnCurrentLine = posOnCurrentLine;
        if (posOnCurrentLine != currentLine.length()) {
            String ending = currentLine.substring(posOnCurrentLine);
            info = shell.complete(currentLine.substring(0, posOnCurrentLine));
            printList(info, currentPrompt);
            if (info.getCompleted() != null) {
                setContent(info.getCompleted() + ending);
                posOnCurrentLine = currentLine.length() - ending.length();
            }
        } else {
            info = shell.complete(currentLine.toString());
            printList(info, currentPrompt);
            if (info.getCompleted() != null) {
                setContent(info.getCompleted());
                posOnCurrentLine = currentLine.length();
            }
        }

        return info;
    }

    protected void printList(CompletionInfo info, String currentPrompt) {
        if ((info != null) && info.hasItems()) {
            int oldPosOnCurrentLine = posOnCurrentLine;
            moveEnd();
            refreshCurrentLine(currentPrompt);

            out.println();
            String[] list = info.getItems();
            
            final int minItemsToSplit = 5;
            if(list.length > minItemsToSplit)
            {
	            list = splitInColumns(list); 
            }

        	// display items column (may be single or multiple columns)
            for (String item : list)
            {               	
            	// item may actually be a single item or in fact multiple items
            	if((item.length() % SCREEN_WIDTH) == 0)
            	{
            		// we are already at the first column of the next line 
            		out.print(item);
            	}
            	else
            	{
            		// we aren't at the first column of the next line
            		out.println(item);
            	}
            }

            posOnCurrentLine = oldPosOnCurrentLine;
        }
    }
    
    protected String[] splitInColumns(String[] items)
    {
        final int separatorWidth = 3;
        
        // compute the maximum width of items
        int maxWidth = 0;
        for(String item : items)
        {
        	if(item.length() > maxWidth)
        	{
        		maxWidth = item.length();
        	}
        }
        
        final int columnWidth = Math.min(SCREEN_WIDTH, maxWidth + separatorWidth);
        final int nbColumns = SCREEN_WIDTH / columnWidth;
        final boolean lastLineIsFull = ((items.length % nbColumns) == 0);
        final int nbLines = (items.length / nbColumns) + (lastLineIsFull ? 0 : 1);
        
        String[] lines = new String[nbLines];
    	StringBuilder line = new StringBuilder(SCREEN_WIDTH);
    	int lineNum = 0;
        for(int itemNum = 0 ; itemNum < items.length ; )
        {
        	for(int c = 0 ; c < nbColumns ; c++)
        	{
        		final String item = items[itemNum++];
        		line.append(item);
        		
        		// add some blanks
        		final int nbBlanks = columnWidth - item.length();
        		for(int i = 0 ; i < nbBlanks ; i++)
        		{
        			line.append(' ');
        		}

        		if(itemNum >= items.length) break;
        	}
        	
        	lines[lineNum++] = line.toString(); 
        	line.setLength(0); // clear the buffer
        }
        
        return lines; 
    }

    public void appendChar(char c) {
        startModif();
        if (posOnCurrentLine == currentLine.length()) {
            currentLine.append(c);
        } else {
            currentLine.insert(posOnCurrentLine, c);
        }
        posOnCurrentLine++;
        endModif();
    }

    protected void startModif() {
        shortened = false;
        oldLength = currentLine.length();
    }

    protected void endModif() {
        maxLength = Math.max(oldLength, currentLine.length());
        shortened = oldLength > currentLine.length();
        oldLength = 0;
    }

    public void refreshCurrentLine(String currentPrompt) {
        try {
            int x = consoleX;
            int width = console.getWidth();
            int nbLines = ((x + maxLength) / width);

            if (((x + maxLength) % width) != 0)
                nbLines++;

            // if the line has not been shortened (delete, backspace...)
            if (!shortened)
                // scroll up the buffer if necessary, and get the new y
                console.ensureVisible(consoleY + nbLines - 1);

            for (int i = 0; i < nbLines; i++) {
                console.clearRow(consoleY + i);
            }

            // print the prompt and the command line
            console.setCursor(0, consoleY);
            out.print(currentPrompt + currentLine);

            int posCurX = x + posOnCurrentLine;
            int posCurY = consoleY;
            if (posCurX >= width) {
                posCurY += posCurX / width;
                posCurX = (posCurX % width);
            }
            console.setCursor(posCurX, posCurY);
        } catch (Exception e) {
            //todo: why is it ignored?
        }
    }
}
