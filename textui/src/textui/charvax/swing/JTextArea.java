/* class JTextArea
 *
 * Copyright (C) 2001  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charvax.swing;

import charva.awt.*;
import charva.awt.event.KeyEvent;
import charva.awt.event.MouseEvent;
import charva.awt.event.ScrollEvent;
import charva.awt.event.ScrollListener;

import java.util.Enumeration;
import java.util.Vector;

/**
 * JTextArea is an (optionally editable) multi-line area that displays
 * plain text.
 * The JTextArea class implements the Scrollable interface, which enables
 * it to be placed inside a charvax.swing.JScrollPane. In fact, in the
 * CHARVA framework it should ALWAYS be used inside a JScrollPane, otherwise
 * it will be unusable (its size depends on the text it contains).<p>
 * <p/>
 * Note that, unlike the javax.swing.JTextArea, pressing the TAB key while
 * the keyboard focus is in the JTextArea will not cause a tab to be
 * inserted; instead, it will move the keyboard input focus to the next
 * focus-traversable component (if there is one). This is because (in
 * javax.swing) the user can user can use the mouse to move the keyboard
 * input focus away from the JTextArea, whereas CHARVA has no mouse support.
 */
public class JTextArea
        extends charvax.swing.text.JTextComponent
        implements charva.awt.Scrollable {
    /**
     * The default constructor creates an empty text area with 10 rows
     * and 10 columns.
     */
    public JTextArea() {
        this( "" );
    }

    /**
     * Construct a text area with 10 rows and 10 columns, and containing
     * the specified text.
     */
    public JTextArea( String text_ ) {
        this( text_, 10, 10 );
    }

    /**
     * Construct a text area wth the specified number of rows and columns,
     * and containing the specified text.
     */
    public JTextArea( String text_, int rows_, int columns_ ) {
        setDocument( text_ );
        _rows = rows_;
        _preferredRows = rows_;
        _columns = columns_;
        _preferredColumns = columns_;
        setCaretPosition( 0 );
    }

    /**
     * Sets the number of columns in this JTextArea.
     */
    public void setColumns( int columns_ ) {
        _columns = columns_;
        _preferredColumns = columns_;
    }

    /**
     * Returns the number of columns in this JTextArea.
     */
    public int getColumns() {
        return _preferredColumns;
    }

    /**
     * Sets the number of rows in this JTextArea.
     */
    public void setRows( int rows_ ) {
        _rows = rows_;
        _preferredRows = rows_;
    }

    /**
     * Returns the number of rows in this JTextArea.
     */
    public int getRows() {
        return _preferredRows;
    }

    /**
     * Returns the size of this component.
     */
    public Dimension getSize() {
        return new Dimension( _columns, _rows );
    }

    public int getWidth() {
        return _columns;
    }

    public int getHeight() {
        return _rows;
    }

    /**
     * Appends the specified text to the end of the document.
     */
    public synchronized void append( String text_ ) {
        super._document.append( text_ );
        _caretPosition = super._document.length();

        refresh();
    }

    /**
     * Inserts the specified text at the specified position (ie at the
     * specified offset from the start of the document)..
     */
    public synchronized void insert( String text_, int pos_ ) {
        super._document.insert( pos_, text_ );
        _caretPosition = pos_ + text_.length();

        refresh();
    }

    /**
     * Inserts the specified character at the specified position (ie at the
     * specified offset from the start of the document)..
     */
    public synchronized void insert( char ch, int pos_ ) {
        try {
            super._document.insert( pos_, ch );
            _caretPosition = pos_ + 1;
            refresh();
        }
        catch( StringIndexOutOfBoundsException sioobe ) {
            System.err.println( "Insert ch=" + ch + ", pos=" + pos_ + " in document of length" + super._document.length() + " failed" );
        }

    }

    /**
     * Replaces the text from the specified start position to end position
     * with the specified text.
     */
    public synchronized void replaceRange( String text_, int start_, int end_ ) {
        super._document.replace( start_, end_, text_ );
        _caretPosition = start_ + text_.length();

        refresh();
    }

    /**
     * Sets the position of the text insertion caret for this JTextArea.
     */
    public void setCaretPosition( int caret_ ) {
        super.setCaretPosition( caret_ );

        refresh();
    }

    /**
     * Returns the number of lines of text displayed in the JTextArea.
     */
    public int getLineCount() {
        return offsetCalc( LINE_COUNT, 0 );
    }

    /**
     * Returns the offset of the first character in the specified line
     * of text.
     */
    public int getLineStartOffset( int line_ ) {
        return offsetCalc( LINE_START_OFFSET, line_ );
    }

    /**
     * Returns the offset of the last character in the specified line.
     */
    public int getLineEndOffset( int line_ ) {
        return offsetCalc( LINE_END_OFFSET, line_ );
    }

    /**
     * Translates an offset (relative to the start of the document)
     * to a line number.
     */
    public int getLineOfOffset( int offset_ ) {
        return offsetCalc( LINE_OF_OFFSET, offset_ );
    }

    /**
     * Sets the line-wrapping policy of the JTextArea. If set to true,
     * lines will be wrapped if they are too long to fit within the
     * allocated width. If set to false, the lines will always be
     * unwrapped. The default value of this property is false.
     */
    public void setLineWrap( boolean wrap_ ) {
        _lineWrap = wrap_;
        _rows = _preferredRows;
        _columns = _preferredColumns;
    }

    /**
     * Returns the line-wrapping policy of the JTextArea. If set to true,
     * lines will be wrapped if they are too long to fit within the
     * allocated width. If set to false, the lines will always be
     * unwrapped.
     */
    public boolean getLineWrap() {
        return _lineWrap;
    }

    /**
     * Sets the line-wrapping style to be used if getLineWrap() is true.
     * If true, lines will be wrapped at word boundaries (whitespace) if
     * they are too long to fit in the allocated number of columns. If
     * false, lines will be wrapped at character boundaries. The default
     * value of this property is false.
     */
    public void setWrapStyleWord( boolean wrapWord_ ) {
        _wrapStyleWord = wrapWord_;
    }

    /**
     * Returns the line-wrapping style to be used if getLineWrap() is true.
     * If true, lines will be wrapped at word boundaries (whitespace) if
     * they are too long to fit in the allocated number of columns. If
     * false, lines will be wrapped at character boundaries.
     */
    public boolean getWrapStyleWord() {
        return _wrapStyleWord;
    }

    /**
     * Called by the LayoutManager.
     */
    public Dimension minimumSize() {
        return getSize();
    }

    /**
     * Process KeyEvents that have been generated by this JTextArea.
     */
    public void processKeyEvent( KeyEvent ke_ ) {

        /* First call all KeyListener objects that may have been registered
         * for this component.
         */
        super.processKeyEvent( ke_ );

        /* Check if any of the KeyListeners consumed the KeyEvent.
         */
        if( ke_.isConsumed() ) {
            return;
        }

        int caret = getCaretPosition();
        int line = getLineOfOffset( caret );
        int key = ke_.getKeyCode();
        if( key == '\t' ) {
            getParent().nextFocus();
            return;
        }
        else if( key == KeyEvent.VK_BACK_TAB ) {
            getParent().previousFocus();
            return;
        }
        else if( key == KeyEvent.VK_LEFT && caret > 0 ) {
            setCaretPosition( caret - 1 );
        }
        else if( key == KeyEvent.VK_RIGHT &&
                 caret < getDocument().length() ) {
            setCaretPosition( caret + 1 );
        }
        else if( key == KeyEvent.VK_HOME ) {
            int lineStart = getLineStartOffset( line );
            setCaretPosition( lineStart );
        }
        else if( key == KeyEvent.VK_END ) {
            int lineEnd = getLineEndOffset( line );
            setCaretPosition( lineEnd );
        }
        else if( ( key == KeyEvent.VK_PAGE_UP ||
                   key == KeyEvent.VK_PAGE_DOWN ) &&
                 ( getParent() instanceof JViewport ) ) {

            JViewport viewport = (JViewport)getParent();
            int vertical_offset = -1 * viewport.getViewPosition().y;
            int viewport_height = viewport.getSize().height;
            if( key == KeyEvent.VK_PAGE_UP ) {
                if( line > vertical_offset ) {
                    line = vertical_offset;
                }
                else {
                    line = vertical_offset - viewport_height;
                }

                line = ( line < 0 ) ? 0 : line;
            }
            else {
                if( line < vertical_offset + viewport_height - 1 ) {
                    line = vertical_offset + viewport_height - 1;
                }
                else {
                    line = vertical_offset + ( 2 * viewport_height ) - 1;
                }

                line = ( line > getLineCount() - 1 ) ?
                       ( getLineCount() - 1 ) :
                       line;
            }
            setCaretPosition( getLineStartOffset( line ) );
        }
        else if( key == KeyEvent.VK_UP && line > 0 ) {
            int column = caret - getLineStartOffset( line );
            int prevlineStart = getLineStartOffset( line - 1 );
            int prevlineEnd = getLineEndOffset( line - 1 );
            if( column > prevlineEnd - prevlineStart ) {
                column = prevlineEnd - prevlineStart;
            }
            setCaretPosition( prevlineStart + column );
        }
        else if( key == KeyEvent.VK_DOWN &&
                 line < getLineCount() - 1 ) {
            int column = caret - getLineStartOffset( line );
            int nextlineStart = getLineStartOffset( line + 1 );
            int nextlineEnd = getLineEndOffset( line + 1 );
            if( column > nextlineEnd - nextlineStart ) {
                column = nextlineEnd - nextlineStart;
            }
            setCaretPosition( nextlineStart + column );
        }
        else if( super.isEditable() == false ) {
            Toolkit.getDefaultToolkit().beep();
        }
        else if( key >= ' ' && key <= 0177 ) {
            insert( (char)key, caret );
        }
        else if( key == KeyEvent.VK_ENTER ) {
            insert( '\n', caret );
        }
        else if( key == KeyEvent.VK_BACK_SPACE && caret > 0 ) {
            replaceRange( "", caret - 1, caret );
        }
        else if( key == KeyEvent.VK_DELETE &&
                 caret < getDocument().length() - 1 ) {
            replaceRange( "", caret, caret + 1 );
        }

        /* If this JTextArea is contained in a JViewport, let the JViewport
         * do the drawing, after setting the clip rectangle.
         */
        if( ( getParent() instanceof JViewport ) == false ) {
            draw( Toolkit.getDefaultToolkit() );
            requestFocus();
            super.requestSync();
        }
    }

    /**
     * Process a MouseEvent that was generated by clicking the mouse
     * somewhere inside this JTextArea.
     * Clicking the mouse inside the JTextArea moves the caret position
     * to where the mouse was clicked.
     */
    public void processMouseEvent( MouseEvent e_ ) {
        super.processMouseEvent( e_ );

        if( e_.getButton() == MouseEvent.BUTTON1 &&
            e_.getModifiers() == MouseEvent.MOUSE_CLICKED &&
            this.isFocusTraversable() ) {

            /* Get the absolute origin of this component.
             */
            Point origin = getLocationOnScreen();
            Insets insets = super.getInsets();
            origin.translate( insets.left, insets.top );

            int line = e_.getY() - origin.y;
            if( line > getLineCount() - 1 ) {
                return;
            }

            int column = e_.getX() - origin.x;
            int lineStart = getLineStartOffset( line );
            int lineEnd = getLineEndOffset( line );
            if( column > lineEnd - lineStart ) {
                column = lineEnd - lineStart;
            }
            setCaretPosition( lineStart + column );
            repaint();
        }
    }

    /**
     * Implements the abstract method in charva.awt.Component.
     *
     * @param toolkit
     */
    public void draw( Toolkit toolkit ) {
        Point tempCaret = null;
        Point caret = _caret;

        /* Get the absolute origin of this component.
         */
        Point origin = getLocationOnScreen();

        int colorpair = getCursesColor();

        /* Start by blanking out the text area
         */
        toolkit.blankBox( origin, getSize(), colorpair );
        toolkit.setCursor( origin );

        StringBuffer charBuffer = new StringBuffer();
        /* Scan through the entire document, drawing each character in it.
         */
        ScrollEvent scrollevent = null;
        int row = 0, col = 0;
        // outerloop:
            for( int i = 0; i < super._document.length(); i++ ) {

                /* At some point during the scan of the document, the
                 * caret position should match the scan index, unless the caret
                 * position is after the last character of the document.
                 */
                if( _caretPosition == i ) {
                    tempCaret = new Point( col, row );

                    /* If the caret has changed, generate a ScrollEvent. Note
                     * that this method may be called multiple times during the
                     * scan; however, we must post only the last event generated.
                     */
                    if( tempCaret.equals( caret ) == false ) {
                        scrollevent = generateScrollEvent( tempCaret,
                                                           new Point( col, row ) );
                        caret = tempCaret;
                    }
                }

                char chr = super._document.charAt( i );
                if( col < _columns ) {
                    if( chr == '\n' ) {
                        col = 0;
                        row++;
                        if( row >= _rows ) {
                            _rows++;
                        }
                        if(charBuffer.length() > 0){
                            toolkit.addString( charBuffer.toString(), 0, colorpair );
                            charBuffer.setLength(0);
                        }
                        toolkit.setCursor( origin.addOffset( col, row ) );
                    }
                    else {
                        charBuffer.append(chr);
                        //toolkit.addChar( chr, 0, colorpair );
                        col++;
                    }
                }
                else {	// We have reached the right-hand column.
                    if(charBuffer.length() > 0){
                        toolkit.addString( charBuffer.toString(), 0, colorpair );
                        charBuffer.setLength(0);
                    }
                    if( _lineWrap == false ) {
                        if( chr == '\n' ) {
                            col = 0;
                            row++;
                            if( row >= _rows ) {
                                _rows++;
                            }
                            toolkit.setCursor( origin.addOffset( col, row ) );
                        }
                        else {
                            toolkit.addChar( chr, 0, colorpair );
                            col++;
                            _columns++;
                        }
                    }
                    else {	// line-wrap is true
                        if( _wrapStyleWord == false ) {
                            col = 0;
                            row++;
                            if( row >= _rows ) {
                                _rows++;
                            }
                            toolkit.setCursor( origin.addOffset( col, row ) );
                            if( chr != '\n' )    // thanks to Chris Rogers for this
                            {
                                toolkit.addChar( chr, 0, colorpair );
                            }
                        }
                        else {
                            /* We must back-track until we get to whitespace, so
                             * that we can move the word to the next line.
                             */
                            int j;
                            for( j = 0; j < _columns; j++ ) {
                                char tmpchr = super._document.charAt( i - j );
                                if( tmpchr == ' ' || tmpchr == '\t' ) {
                                    deleteEOL( toolkit, col - j, row, colorpair );
                                    col = 0;
                                    row++;
                                    if( row >= _rows ) {
                                        _rows++;
                                    }
                                    i -= j;
                                    toolkit.setCursor( origin.addOffset( col, row ) );
                                    break;
                                }
                            }
                            if( j == _columns ) {	// the word was too long
                                if( chr == ' ' || chr == '\n' || chr == '\t' ) {
                                    col = 0;
                                    row++;
                                    if( row >= _rows ) {
                                        _rows++;
                                    }
                                    toolkit.setCursor( origin.addOffset( col, row ) );
                                }
                            }
                        }
                    }	// end if line-wrap is true
                }		// end if we have reached the right-hand column
            }		// end FOR loop.

        if(charBuffer.length() > 0){
            toolkit.addString( charBuffer.toString(), 0, colorpair );
            charBuffer.setLength(0);
        }
        /* Check for the case where the caret position is after the last
         * character of the document.
         */
        if( _caretPosition == super._document.length() ) {
            tempCaret = new Point( col, row );

            /* If the caret has changed, generate a ScrollEvent
             */
            if( tempCaret.equals( caret ) == false ) {
                scrollevent = generateScrollEvent( tempCaret,
                                                   new Point( col, row ) );
            }
            caret = tempCaret;
        }

        /* Post a ScrollEvent, if one was generated; but only if the
         * caret has really changed.  We have to be careful to avoid an
         * endless loop, where a ScrollEvent triggers a draw(), which
         * triggers an unnecessary ScrollEvent and so on.
         */
        if( ( _caret.equals( caret ) == false ) && scrollevent != null ) {
            toolkit.getSystemEventQueue().postEvent( scrollevent );
            _caret = caret;
        }
    }

    public void requestFocus() {
        /* Generate the FOCUS_GAINED event.
         */
        super.requestFocus();

        /* Get the absolute origin of this component.
         */
        Point origin = getLocationOnScreen();
        Toolkit.getDefaultToolkit().setCursor( origin.addOffset( _caret ) );
    }

    /**
     * Register a ScrollListener object for this JTextArea.
     */
    public void addScrollListener( ScrollListener sl_ ) {
        if( _scrollListeners == null ) {
            _scrollListeners = new Vector<ScrollListener>();
        }
        _scrollListeners.add( sl_ );
    }

    /**
     * Remove a ScrollListener object that is registered for this JTextArea.
     */
    public void removeScrollListener( ScrollListener sl_ ) {
        if( _scrollListeners == null ) {
            return;
        }
        _scrollListeners.remove( sl_ );
    }

    /**
     * Process scroll events generated by this JTextArea.
     */
    public void processScrollEvent( ScrollEvent e_ ) {
        if( _scrollListeners != null ) {
            for( Enumeration<ScrollListener> e = _scrollListeners.elements();
                 e.hasMoreElements(); ) {

                ScrollListener sl = e.nextElement();
                sl.scroll( e_ );
            }
        }
    }

    /**
     * Returns the preferred size of the viewport for this JTextArea
     * when it is in a JScrollPane (this method implements the
     * Scrollable interface). The size is determined by the number of
     * rows and columns set for this JTextArea (either in the constructor
     * or in the setColumns() and setRows() methods).
     */
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension( _preferredColumns, _preferredRows );
    }

    public void debug( int level_ ) {
        for( int i = 0; i < level_; i++ ) {
            System.err.print( "    " );
        }
        System.err.println( "JTextArea origin=" + _origin +
                            " size=" + getSize() );
    }

    /**
     * A private helper method to delete from a specified position
     * until the end of the line.
     */
    private void deleteEOL( Toolkit term_, int col_, int row_, int colorpair_ ) {
        Point origin = getLocationOnScreen();
        term_.setCursor( origin.addOffset( col_, row_ ) );
        for( int i = col_; i < _columns; i++ ) {
            term_.addChar( ' ', 0, colorpair_ );
        }
    }

    /**
     * This helper method converts offset to line number and
     * vice versa.
     */
    private int offsetCalc( int mode_, int value_ ) {
        int lineOfOffset = 0;
        int row = 0;

        if( mode_ == LINE_START_OFFSET && value_ == 0 ) {
            return 0;
        }

        for( int col = 0, i = 0;
             i < super._document.length();
             i++ ) {

            if( mode_ == LINE_OF_OFFSET && value_ == i ) {
                lineOfOffset = row;
            }

            char chr = super._document.charAt( i );
            if( col < _columns ) {
                if( chr == '\n' ) {
                    col = 0;
                    row++;
                }
                else {
                    col++;
                }
            }
            else {	// We have reached the right-hand column.
                if( _lineWrap == false ) {
                    if( chr == '\n' ) {
                        col = 0;
                        row++;
                    }
                }
                else {	// line-wrap is true
                    if( _wrapStyleWord == false ) {
                        col = 0;
                        row++;
                    }
                    else {
                        /* We must back-track until we get to whitespace, so
                         * that we can move the word to the next line.
                         */
                        int j;
                        for( j = 0; j < _columns; j++ ) {
                            char tmpchr = super._document.charAt( i - j );
                            if( tmpchr == ' ' || tmpchr == '\t' ) {
                                col = 0;
                                row++;
                                i -= j;
                                break;
                            }
                        }
                        if( j == _columns ) {	// the word was too long
                            if( chr == ' ' || chr == '\n' || chr == '\t' ) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                }	// end if line-wrap is true
            }		// end if we have reached the right-hand column

            if( mode_ == LINE_START_OFFSET && col == 0 && row == value_ ) {
                return i + 1;
            }
            else if( mode_ == LINE_END_OFFSET && col == 0 && row == value_ + 1 ) {
                return i;
            }

        }		// end FOR loop.

        if( mode_ == LINE_OF_OFFSET ) {
            if( value_ == super._document.length() ) {
                return row;
            }
            else {
                return lineOfOffset;
            }
        }
        else if( mode_ == LINE_COUNT ) {
            return row + 1;
        }
        else if( mode_ == LINE_END_OFFSET && row == value_ ) {
            return super._document.length();
        }
        else {
            throw new IndexOutOfBoundsException( "Invalid offset or line number: mode=" + mode_ +
                                                 " value=" + value_ + " row=" + row + " doc=\"" + _document + "\"" );
        }
    }

    /* Private helper method used to redraw the component if its state
     * has changed.
     */
    @SuppressWarnings("unused")
    private void refreshOrig() {
        /* If this JTextArea is contained in a JViewport, the PaintEvent
         * that we post must request a redraw of the JViewport, so that
         * the JViewport can set the clipping rectangle before calling the
         * draw() method of this JTextArea.
         */
        Component todraw;
        if( getParent() instanceof JViewport ) {
            todraw = getParent();
        }
        else {
            todraw = this;
        }

        /* If this component is already displayed, generate a PaintEvent
         * and post it onto the queue.
         */
        todraw.repaint();
    }

    private void refresh() {
        /* If this JTextArea is contained in a JViewport, the PaintEvent
         * that we post must request a redraw of the JViewport, so that
         * the JViewport can set the clipping rectangle before calling the
         * draw() method of this JTextArea.
         */
        Component todraw;
        Component parent = getParent();
        if( parent == null ) {
            this.repaint();
        }
        else {
            if( parent instanceof JViewport ) {
                todraw = parent;
            }
            else {
                todraw = this;
            }
            /* If this component is already displayed, generate a PaintEvent
             * and post it onto the queue.
             */
            todraw.draw( Toolkit.getDefaultToolkit() );
        }
    }

    /**
     * Private method, called whenever the caret changes.
     */
    private ScrollEvent generateScrollEvent( Point tempCaret_, Point col_row_ ) {
        int direction;

        /* Determine the direction of scrolling
         */
        if( tempCaret_.y > _caret.y ) {
            if( tempCaret_.x > _caret.x ) {
                direction = ScrollEvent.UP_LEFT;
            }
            else if( tempCaret_.x < _caret.x ) {
                direction = ScrollEvent.UP_RIGHT;
            }
            else {
                direction = ScrollEvent.UP;
            }
        }
        else if( tempCaret_.y < _caret.y ) {
            if( tempCaret_.x > _caret.x ) {
                direction = ScrollEvent.DOWN_LEFT;
            }
            else if( tempCaret_.x < _caret.x ) {
                direction = ScrollEvent.DOWN_RIGHT;
            }
            else {
                direction = ScrollEvent.DOWN;
            }
        }
        else {
            if( tempCaret_.x > _caret.x ) {
                direction = ScrollEvent.LEFT;
            }
            else {
                direction = ScrollEvent.RIGHT;
            }
        }
        return new ScrollEvent( this, direction, col_row_ );

    }

    //====================================================================
    // INSTANCE VARIABLES

    private int _rows;
    private int _columns;

    private int _preferredRows;
    private int _preferredColumns;

    /**
     * The caret is updated only when the component is drawn.
     */
    private Point _caret = new Point( 0, 0 );

    private boolean _lineWrap;
    private boolean _wrapStyleWord = false;

    /**
     * A list of ScrollListeners registered for this JTextArea.
     */
    private Vector<ScrollListener> _scrollListeners = null;

    private static final int LINE_COUNT = 1;
    private static final int LINE_START_OFFSET = 2;
    private static final int LINE_END_OFFSET = 3;
    private static final int LINE_OF_OFFSET = 4;
}
