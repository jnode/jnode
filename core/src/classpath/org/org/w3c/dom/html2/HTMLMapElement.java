/*
 * Copyright (c) 2003 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom.html2;

/**
 * Client-side image map. See the MAP element definition in HTML 4.01.
 * <p>See also the <a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>Document Object Model (DOM) Level 2 HTML Specification</a>.
 */
public interface HTMLMapElement extends HTMLElement {
    /**
     * The list of areas defined for the image map. 
     */
    public HTMLCollection getAreas();

    /**
     * Names the map (for use with <code>usemap</code>). See the name 
     * attribute definition in HTML 4.01.
     */
    public String getName();
    /**
     * Names the map (for use with <code>usemap</code>). See the name 
     * attribute definition in HTML 4.01.
     */
    public void setName(String name);

}
