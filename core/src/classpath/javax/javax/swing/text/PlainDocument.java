/* PlainDocument.java -- 
   Copyright (C) 2002, 2004  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package javax.swing.text;

import java.util.ArrayList;

public class PlainDocument extends AbstractDocument
{
  private static final long serialVersionUID = 4758290289196893664L;
    
  public static final String lineLimitAttribute = "lineLimit";
  public static final String tabSizeAttribute = "tabSize";

  private Element rootElement;
  private int tabSize;
  
  public PlainDocument()
    {
    this(new GapContent());
  }

  public PlainDocument(AbstractDocument.Content content)
  {
    super(content);
    tabSize = 8;
    rootElement = createDefaultRoot();
  }

  protected void reindex()
  {
    Element[] lines;
    try 
      {
        String str = content.getString(0, content.length());

        ArrayList elts = new ArrayList();
        int j = 0;
        for (int i = str.indexOf('\n', 0); i != -1; i = str.indexOf('\n', i+1))
          {
            elts.add(createLeafElement(rootElement, null, j, i));
            j = i;
          }
        
        if (j < content.length())
          elts.add(createLeafElement(rootElement, null, j, content.length()));
        
        lines = new Element[elts.size()];
        for (int i = 0; i < elts.size(); ++i)
          lines[i] = (Element) elts.get(i);
        
      }
    catch (BadLocationException e)
  {
        lines = new Element[1];
    lines[0] = createLeafElement(rootElement, null, 0, 1);
      }

    ((BranchElement) rootElement).replace(0, rootElement.getElementCount(), lines);
  }

  protected AbstractDocument.AbstractElement createDefaultRoot()
  {
    rootElement = createBranchElement(null, null);
    reindex();
    return (AbstractElement) rootElement;
  }

  protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr)
  {
    reindex();
  }

  protected void removeUpdate(DefaultDocumentEvent chng)
  {
    reindex();
    }

    public Element getDefaultRootElement()
    {
    return rootElement;
    }

    public Element getParagraphElement(int  pos)
    {
	return null;
    }
}
