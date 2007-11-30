/*
 * Copyright 1997-2000 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.awt.print;

import java.util.Vector;

/**
 * The <code>Book</code> class provides a representation of a document in
 * which pages may have different page formats and page painters. This
 * class uses the {@link Pageable} interface to interact with a
 * {@link PrinterJob}.
 * @see Pageable
 * @see PrinterJob 
*/

public class Book implements Pageable {

 /* Class Constants */

 /* Class Variables */

 /* Instance Variables */

    /**
     * The set of pages that make up the Book.
     */
    private Vector mPages;

 /* Instance Methods */

    /**
     *	Creates a new, empty <code>Book</code>.
     */
    public Book() {
    	mPages = new Vector();
    }

    /**
     * Returns the number of pages in this <code>Book</code>.
     * @return the number of pages this <code>Book</code> contains.
     */
    public int getNumberOfPages(){
	return mPages.size();
    }

    /**
     * Returns the {@link PageFormat} of the page specified by
     * <code>pageIndex</code>.
     * @param pageIndex the zero based index of the page whose
     *            <code>PageFormat</code> is being requested
     * @return the <code>PageFormat</code> describing the size and
     *          orientation of the page.
     * @throws IndexOutOfBoundsException if the <code>Pageable</code> 
     * 		does not contain the requested page 
     */
    public PageFormat getPageFormat(int pageIndex)
	throws IndexOutOfBoundsException
    {
	return getPage(pageIndex).getPageFormat();
    }

    /**
     * Returns the {@link Printable} instance responsible for rendering   
     * the page specified by <code>pageIndex</code>.
     * @param pageIndex the zero based index of the page whose
     *                  <code>Printable</code> is being requested
     * @return the <code>Printable</code> that renders the page.
     * @throws IndexOutOfBoundsException if the <code>Pageable</code>
     *            does not contain the requested page 
     */
    public Printable getPrintable(int pageIndex)
	throws IndexOutOfBoundsException
    {
	return getPage(pageIndex).getPrintable();
    }

    /**
     * Sets the <code>PageFormat</code> and the <code>Painter</code> for a
     * specified page number.
     * @param pageIndex the zero based index of the page whose
     *                  painter and format is altered
     * @param painter   the <code>Printable</code> instance that
     *                  renders the page
     * @param page      the size and orientation of the page
     * @throws IndexOutOfBoundsException if the specified
     *		page is not already in this <code>Book</code> 
     * @throws NullPointerException if the <code>painter</code> or
     *		<code>page</code> argument is <code>null</code>
     */
    public void setPage(int pageIndex, Printable painter, PageFormat page)
	throws IndexOutOfBoundsException
    {
	if (painter == null) {
	    throw new NullPointerException("painter is null");
	}

	if (page == null) {
	    throw new NullPointerException("page is null");
	}

	mPages.setElementAt(new BookPage(painter, page), pageIndex);
    }

    /**
     * Appends a single page to the end of this <code>Book</code>.   
     * @param painter   the <code>Printable</code> instance that
     *                  renders the page
     * @param page      the size and orientation of the page 
     * @throws <code>NullPointerException</code>
     *          If the <code>painter</code> or <code>page</code>
     *		argument is <code>null</code>
     */
    public void append(Printable painter, PageFormat page) {
	mPages.addElement(new BookPage(painter, page));
    }

    /**
     * Appends <code>numPages</code> pages to the end of this
     * <code>Book</code>.  Each of the pages is associated with 
     * <code>page</code>.
     * @param painter   the <code>Printable</code> instance that renders
     *                  the page
     * @param page      the size and orientation of the page
     * @param numPages  the number of pages to be added to the
     *                  this <code>Book</code>. 
     * @throws <code>NullPointerException</code>
     *          If the <code>painter</code> or <code>page</code>
     *		argument is <code>null</code>
     */
    public void append(Printable painter, PageFormat page, int numPages) {
	BookPage bookPage = new BookPage(painter, page);
	int pageIndex = mPages.size();
	int newSize = pageIndex + numPages;

	mPages.setSize(newSize);
	for(int i = pageIndex; i < newSize; i++){
	    mPages.setElementAt(bookPage, i);
	}
    }

    /**
     * Return the BookPage for the page specified by 'pageIndex'.
     */
    private BookPage getPage(int pageIndex)
	throws ArrayIndexOutOfBoundsException
    {
	return (BookPage) mPages.elementAt(pageIndex);
    }

    /**
     * The BookPage inner class describes an individual
     * page in a Book through a PageFormat-Printable pair.
     */
    private class BookPage {
	/**
	 *  The size and orientation of the page.
	 */
	private PageFormat mFormat;

	/**
	 * The instance that will draw the page.
	 */
	private Printable mPainter;

	/**
	 * A new instance where 'format' describes the page's
	 * size and orientation and 'painter' is the instance
	 * that will draw the page's graphics.
	 * @throws  NullPointerException
	 *          If the <code>painter</code> or <code>format</code>
	 *	    argument is <code>null</code>
	 */
	BookPage(Printable painter, PageFormat format) {

	    if (painter == null || format == null) {
		throw new NullPointerException();
	    }

	    mFormat = format;
	    mPainter = painter;
	}

	/**
	 * Return the instance that paints the
	 * page.
	 */
	Printable getPrintable() {
	    return mPainter;
	}

	/**
	 * Return the format of the page.
	 */
	PageFormat getPageFormat() {
	    return mFormat;
	}
    }
}
