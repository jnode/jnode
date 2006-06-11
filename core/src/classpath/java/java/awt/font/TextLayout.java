/* TextLayout.java --
   Copyright (C) 2006  Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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


package java.awt.font;

import gnu.classpath.NotImplementedException;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.Map;

/**
 * @author Sven de Marothy
 */
public final class TextLayout implements Cloneable
{
  private GlyphVector[] runs;
  private Font font;
  private FontRenderContext frc;
  private String string;
  private Rectangle2D boundsCache;
  private LineMetrics lm;

  /**
   * Start and end character indices of the runs.
   * First index is the run number, second is 0 or 1 for the starting 
   * and ending character index of the run, respectively.
   */
  private int[][] runIndices;

  /**
   * Base directionality, determined from the first char.
   */
  private boolean leftToRight;

  /**
   * Whether this layout contains whitespace or not.
   */
  private boolean hasWhitespace = false;

  /**
   * The default caret policy.
   */
  static TextLayout.CaretPolicy DEFAULT_CARET_POLICY = new CaretPolicy();

  /**
   * Constructs a TextLayout.
   */
  public TextLayout (String string, Font font, FontRenderContext frc) 
    {
    this.font = font;
    this.frc = frc;
    this.string = string;
    lm = font.getLineMetrics(string, frc);

    // Get base direction and whitespace info
    getStringProperties();

    if( Bidi.requiresBidi( string.toCharArray(), 0, string.length() ) )
      {
	Bidi bidi = new Bidi( string, leftToRight ? 
			      Bidi.DIRECTION_LEFT_TO_RIGHT : 
			      Bidi.DIRECTION_RIGHT_TO_LEFT );
	int rc = bidi.getRunCount();
	byte[] table = new byte[ rc ];
	for(int i = 0; i < table.length; i++)
	  table[i] = (byte)bidi.getRunLevel(i);

	runs = new GlyphVector[ rc ];
	runIndices = new int[rc][2];
	for(int i = 0; i < runs.length; i++)
	  {
	    runIndices[i][0] = bidi.getRunStart( i );
	    runIndices[i][1] = bidi.getRunLimit( i );
	    if( runIndices[i][0] != runIndices[i][1] ) // no empty runs.
	      {
		runs[i] = font.layoutGlyphVector
		  ( frc, string.toCharArray(),
		    runIndices[i][0], runIndices[i][1],
		    ((table[i] & 1) == 0) ? Font.LAYOUT_LEFT_TO_RIGHT :
		    Font.LAYOUT_RIGHT_TO_LEFT );
	      }
	  }
	Bidi.reorderVisually( table, 0, runs, 0, runs.length );
      }
    else
      {
	runs = new GlyphVector[ 1 ];
	runIndices = new int[1][2];
	runIndices[0][0] = 0;
	runIndices[0][1] = string.length();
	runs[ 0 ] = font.layoutGlyphVector( frc, string.toCharArray(), 
					    0, string.length(),
					    leftToRight ?
					    Font.LAYOUT_LEFT_TO_RIGHT :
					    Font.LAYOUT_RIGHT_TO_LEFT );
    }
  }

  public TextLayout (String string, Map attributes, FontRenderContext frc)  
  {    
    this( string, new Font( attributes ), frc );
  }

  public TextLayout (AttributedCharacterIterator text, FontRenderContext frc)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  /**
   * Scan the character run for the first strongly directional character,
   * which in turn defines the base directionality of the whole layout.
   */
  private void getStringProperties()
  {
    boolean gotDirection = false;
    int i = 0;

    leftToRight = true;
    while( i < string.length() && !gotDirection )
      switch( Character.getDirectionality( string.charAt( i++ ) ) )
	{
	case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
	case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING:
	case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE:
	  gotDirection = true;
	  break;
	  
	case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
	case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
	case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
	case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
	  leftToRight = false;
	  gotDirection = true;
	  break;
	}

    // Determine if there's whitespace in the thing.
    // Ignore trailing chars.
    i = string.length() - 1; 
    hasWhitespace = false;
    while( i >= 0 && Character.isWhitespace( string.charAt(i) ) )
      i--;
    // Check the remaining chars
    while( i >= 0 )
      if( Character.isWhitespace( string.charAt(i--) ) )
	hasWhitespace = true;
  }

  protected Object clone ()
  {
    return new TextLayout( string, font, frc );
  }

  public void draw (Graphics2D g2, float x, float y) 
  {
    for(int i = 0; i < runs.length; i++)
      {
	g2.drawGlyphVector(runs[i], x, y);
	Rectangle2D r = runs[i].getLogicalBounds();
	x += r.getWidth();
      }
  }

  public boolean equals (Object obj)
  {
    if( !( obj instanceof TextLayout) )
      return false;

    return equals( (TextLayout) obj );
  }

  public boolean equals (TextLayout tl)
  {
    if( runs.length != tl.runs.length )
      return false;
    // Compare all glyph vectors.
    for( int i = 0; i < runs.length; i++ )
      if( !runs[i].equals( tl.runs[i] ) )
	return false;
    return true;
  }

  public float getAdvance ()
  {
    float totalAdvance = 0f;
    for(int i = 0; i < runs.length; i++)
      totalAdvance += runs[i].getLogicalBounds().getWidth();
    return totalAdvance;
  }

  public float getAscent ()
  {
    return lm.getAscent();
  }

  public byte getBaseline ()
  {
    return (byte)lm.getBaselineIndex();
  }

  public float[] getBaselineOffsets ()
  {
    return lm.getBaselineOffsets();
  }

  public Shape getBlackBoxBounds (int firstEndpoint, int secondEndpoint)
  {
    if( firstEndpoint < 0 || secondEndpoint > getCharacterCount() )
      return new Rectangle2D.Float();

    GeneralPath gp = new GeneralPath();
    int i = 0; // run index
    double advance = 0;

    // go to first run
    while( runIndices[i + 1][1] < firstEndpoint ) 
      {
	advance += runs[i].getLogicalBounds().getWidth();
	i++;
      }

    int j = 0; // index into the run.
    if( runIndices[i][1] - runIndices[i][0] > 1 )
      {
	while( runs[i].getGlyphCharIndex( j + 1 ) <
	       (firstEndpoint - runIndices[i][0] ) )j++;
      }

    gp.append(runs[i].getGlyphVisualBounds( j ), false);
    boolean keepGoing = true;;

    do
      {
	while( j < runs[i].getNumGlyphs() && 
	       runs[i].getGlyphCharIndex( j ) + runIndices[i][0] < 
	       secondEndpoint )
	  {
	    Rectangle2D r2 = (runs[i].getGlyphVisualBounds( j )).
	      getBounds2D();
	    Point2D p = runs[i].getGlyphPosition( j );
	    r2.setRect( advance + p.getX(), r2.getY(), 
			r2.getWidth(), r2.getHeight() );
	    gp.append(r2, false);
	    j++;
	  }

	if( j >= runs[i].getNumGlyphs() )
	  {
	    advance += runs[i].getLogicalBounds().getWidth();
	    i++; 
	    j = 0;
	  }
	else
	  keepGoing = false;
      }
    while( keepGoing );

    return gp;
  }

  public Rectangle2D getBounds()
  {
    if( boundsCache == null )
      boundsCache = getOutline(new AffineTransform()).getBounds();
    return boundsCache;
  }

  public float[] getCaretInfo (TextHitInfo hit)
  {
    return getCaretInfo(hit, getBounds());
  }

  public float[] getCaretInfo (TextHitInfo hit, Rectangle2D bounds)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public Shape getCaretShape (TextHitInfo hit)
  {
    return getCaretShape( hit, getBounds() );
  }

  public Shape getCaretShape (TextHitInfo hit, Rectangle2D bounds)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public Shape[] getCaretShapes (int offset)
  {
    return getCaretShapes( offset, getBounds() );
  }

  public Shape[] getCaretShapes (int offset, Rectangle2D bounds)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public int getCharacterCount ()
  {
    return string.length();
  }

  public byte getCharacterLevel (int index)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public float getDescent ()
  {
    return lm.getDescent();
  }

  public TextLayout getJustifiedLayout (float justificationWidth)
  {
    TextLayout newLayout = (TextLayout)clone();

    if( hasWhitespace )
      newLayout.handleJustify( justificationWidth );

    return newLayout;
  }

  public float getLeading ()
  {
    return lm.getLeading();
  }

  public Shape getLogicalHighlightShape (int firstEndpoint, int secondEndpoint)
  {
    return getLogicalHighlightShape( firstEndpoint, secondEndpoint, 
				     getBounds() );
  }

  public Shape getLogicalHighlightShape (int firstEndpoint, int secondEndpoint,
                                         Rectangle2D bounds)
  {
    if( firstEndpoint < 0 || secondEndpoint > getCharacterCount() )
      return new Rectangle2D.Float();

    int i = 0; // run index
    double advance = 0;

    // go to first run
    if( i > 0 )
      while( runIndices[i + 1][1] < firstEndpoint ) 
	{
	  advance += runs[i].getLogicalBounds().getWidth();
	  i++;
  }

    int j = 0; // index into the run.
    if( runIndices[i][1] - runIndices[i][0] > 1 )
  {
	while( runs[i].getGlyphCharIndex( j + 1 ) <
	       (firstEndpoint - runIndices[i][0] ) )j++;
  }

    Rectangle2D r = (runs[i].getGlyphLogicalBounds( j )).getBounds2D();
    boolean keepGoing = true;;

    do
  {
	while( j < runs[i].getNumGlyphs() && 
	       runs[i].getGlyphCharIndex( j ) + runIndices[i][0] < 
	       secondEndpoint )
	  {
	    Rectangle2D r2 = (runs[i].getGlyphLogicalBounds( j )).
	      getBounds2D();
	    Point2D p = runs[i].getGlyphPosition( j );
	    r2.setRect( advance + p.getX(), r2.getY(), 
			r2.getWidth(), r2.getHeight() );
	    r = r.createUnion( r2 );
	    j++;
  }

	if( j >= runs[i].getNumGlyphs() )
  {
	    advance += runs[i].getLogicalBounds().getWidth();
	    i++; 
	    j = 0;
	  }
	else
	  keepGoing = false;
  }
    while( keepGoing );

    return r;
  }

  public int[] getLogicalRangesForVisualSelection (TextHitInfo firstEndpoint,
                                                   TextHitInfo secondEndpoint)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public TextHitInfo getNextLeftHit (int offset)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public TextHitInfo getNextLeftHit (TextHitInfo hit)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public TextHitInfo getNextRightHit (int offset)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public TextHitInfo getNextRightHit (TextHitInfo hit)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public Shape getOutline (AffineTransform tx)
  {
    float x = 0f;
    GeneralPath gp = new GeneralPath();
    for(int i = 0; i < runs.length; i++)
      {
	gp.append( runs[i].getOutline( x, 0f ), false );
	Rectangle2D r = runs[i].getLogicalBounds();
	x += r.getWidth();
      }
    if( tx != null )
      gp.transform( tx );
    return gp;
  }

  public float getVisibleAdvance ()
  {
    float totalAdvance = 0f;

    if( runs.length <= 0 )
      return 0f;

    // No trailing whitespace
    if( !Character.isWhitespace( string.charAt( string.length() -1 ) ) )
      return getAdvance();

    // Get length of all runs up to the last
    for(int i = 0; i < runs.length - 1; i++)
      totalAdvance += runs[i].getLogicalBounds().getWidth();

    int lastRun = runIndices[ runs.length - 1 ][0];
    int j = string.length() - 1;
    while( j >= lastRun && Character.isWhitespace( string.charAt( j ) ) ) j--;

    if( j < lastRun )
      return totalAdvance; // entire last run is whitespace

    int lastNonWSChar = j - lastRun;
    j = 0;
    while( runs[ runs.length - 1 ].getGlyphCharIndex( j )
	   <= lastNonWSChar )
      {
	totalAdvance += runs[ runs.length - 1 ].getGlyphLogicalBounds( j ).
	  getBounds2D().getWidth();
	j ++;
      }
    
    return totalAdvance;
  }

  public Shape getVisualHighlightShape (TextHitInfo firstEndpoint,
                                        TextHitInfo secondEndpoint)
  {
    return getVisualHighlightShape( firstEndpoint, secondEndpoint, 
				    getBounds() );
  }

  public Shape getVisualHighlightShape (TextHitInfo firstEndpoint,
                                        TextHitInfo secondEndpoint,
                                        Rectangle2D bounds)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public TextHitInfo getVisualOtherHit (TextHitInfo hit)
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  /**
   * This is a protected method of a <code>final</code> class, meaning
   * it exists only to taunt you.
   */
  protected void handleJustify (float justificationWidth)
  {
    // We assume that the text has non-trailing whitespace.
    // First get the change in width to insert into the whitespaces.
    double deltaW = justificationWidth - getVisibleAdvance();
    int nglyphs = 0; // # of whitespace chars

    // determine last non-whitespace char.
    int lastNWS = string.length() - 1;
    while( Character.isWhitespace( string.charAt( lastNWS ) ) ) lastNWS--;

    // locations of the glyphs.
    int[] wsglyphs = new int[string.length() * 10];
    for(int run = 0; run < runs.length; run++ )
      for(int i = 0; i < runs[run].getNumGlyphs(); i++ )
	{
	  int cindex = runIndices[run][0] + runs[run].getGlyphCharIndex( i );
	  if( Character.isWhitespace( string.charAt( cindex ) ) )
	    //	      && cindex < lastNWS )
	    {
	      wsglyphs[ nglyphs * 2 ] = run;
	      wsglyphs[ nglyphs * 2 + 1] = i;
	      nglyphs++;
	    }
  }

    deltaW = deltaW / nglyphs; // Change in width per whitespace glyph
    double w = 0;
    int cws = 0;
    // Shift all characters
    for(int run = 0; run < runs.length; run++ )
      for(int i = 0; i < runs[ run ].getNumGlyphs(); i++ )
  {
	  if( wsglyphs[ cws * 2 ] == run && wsglyphs[ cws * 2 + 1 ] == i )
	    {
	      cws++; // update 'current whitespace'
	      w += deltaW; // increment the shift
	    }
	  Point2D p = runs[ run ].getGlyphPosition( i );
	  p.setLocation( p.getX() + w, p.getY() );
	  runs[ run ].setGlyphPosition( i, p );
	}
  }

  public TextHitInfo hitTestChar (float x, float y)
  {
    return hitTestChar(x, y, getBounds());
  }

  public TextHitInfo hitTestChar (float x, float y, Rectangle2D bounds)
  {
    return hitTestChar( x, y, getBounds() );
  }

  public boolean isLeftToRight ()
  {
    return leftToRight;
  }

  public boolean isVertical ()
  {
    return false; // FIXME: How do you create a vertical layout?
  }

  public int hashCode ()
    throws NotImplementedException
  {
    throw new Error ("not implemented");
  }

  public String toString ()
  {
    return "TextLayout [string:"+string+", Font:"+font+" Rendercontext:"+
      frc+"]";
  }

  /**
   * Inner class describing a caret policy
   */
  public static class CaretPolicy
  {
    public CaretPolicy()
    {
    }

    public TextHitInfo getStrongCaret(TextHitInfo hit1,
				      TextHitInfo hit2,
				      TextLayout layout)
      throws NotImplementedException
    {
      throw new Error ("not implemented");
    }
  }
}


