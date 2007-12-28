/*
 * Copyright 2001-2004 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

/**
 * Class PresentationDirection is a printing attribute class, an enumeration,
 * that is used in conjunction with the {@link  NumberUp NumberUp} attribute to
 * indicate the layout of multiple print-stream pages to impose upon a
 * single side of an instance of a selected medium.
 * This is useful to mirror the text layout conventions of different scripts.
 * For example, English is "toright-tobottom", Hebrew is "toleft-tobottom"
 *  and Japanese is usually "tobottom-toleft".
 * <P>
 * <B>IPP Compatibility:</B>  This attribute is not an IPP 1.1
 * attribute; it is an attribute in the Production Printing Extension
 * (<a href="ftp://ftp.pwg.org/pub/pwg/standards/pwg5100.3.pdf">PDF</a>)
 * of IPP 1.1.  The category name returned by 
 * <CODE>getName()</CODE> is the IPP attribute name.  The enumeration's 
 * integer value is the IPP enum value.  The <code>toString()</code> method 
 * returns the IPP string representation of the attribute value.
 * <P>
 *
 * @author  Phil Race.
 */
public final class PresentationDirection extends EnumSyntax
       implements PrintJobAttribute, PrintRequestAttribute  {

    private static final long serialVersionUID = 8294728067230931780L;

    /**
     * Pages are laid out in columns starting at the top left,
     * proceeeding towards the bottom & right.
     */
    public static final PresentationDirection TOBOTTOM_TORIGHT =
        new PresentationDirection(0);

    /**
     * Pages are laid out in columns starting at the top right,
     * proceeeding towards the bottom & left.
     */
    public static final PresentationDirection TOBOTTOM_TOLEFT =
        new PresentationDirection(1);

    /**
     * Pages are laid out in columns starting at the bottom left,
     * proceeeding towards the top & right.
     */
    public static final PresentationDirection TOTOP_TORIGHT =
        new PresentationDirection(2);

    /**
     * Pages are laid out in columns starting at the bottom right,
     * proceeeding towards the top & left.
     */
    public static final PresentationDirection TOTOP_TOLEFT =
        new PresentationDirection(3);

    /**
     * Pages are laid out in rows starting at the top left,
     * proceeeding towards the right & bottom.
     */
    public static final PresentationDirection TORIGHT_TOBOTTOM =
        new PresentationDirection(4);

    /**
     * Pages are laid out in rows starting at the bottom left,
     * proceeeding towards the right & top.
     */
    public static final PresentationDirection TORIGHT_TOTOP =
        new PresentationDirection(5);

    /**
     * Pages are laid out in rows starting at the top right,
     * proceeeding towards the left & bottom.
     */
    public static final PresentationDirection TOLEFT_TOBOTTOM =
        new PresentationDirection(6);

    /**
     * Pages are laid out in rows starting at the bottom right,
     * proceeeding towards the left & top.
     */
    public static final PresentationDirection TOLEFT_TOTOP =
        new PresentationDirection(7);

    /**
     * Construct a new presentation direction enumeration value with the given
     * integer value. 
     *
     * @param  value  Integer value.
     */
    private PresentationDirection(int value) {
	super (value);
    }

    private static final String[] myStringTable = {
	"tobottom-toright",
	"tobottom-toleft",
	"totop-toright",
	"totop-toleft",
	"toright-tobottom",
	"toright-totop",
	"toleft-tobottom",
	"toleft-totop",
    };

    private static final PresentationDirection[] myEnumValueTable = {
	TOBOTTOM_TORIGHT,
	TOBOTTOM_TOLEFT,
	TOTOP_TORIGHT,
	TOTOP_TOLEFT,
	TORIGHT_TOBOTTOM,
	TORIGHT_TOTOP,
	TOLEFT_TOBOTTOM,
	TOLEFT_TOTOP,
    };

    /**
     * Returns the string table for class PresentationDirection.
     */
    protected String[] getStringTable() {
	return myStringTable;
    }

    /**
     * Returns the enumeration value table for class PresentationDirection.
     */
    protected EnumSyntax[] getEnumValueTable() {
	return myEnumValueTable;
    }

    /**
     * Get the printing attribute class which is to be used as the "category" 
     * for this printing attribute value.
     * <P>
     * For class PresentationDirection
     * the category is class PresentationDirection itself. 
     *
     * @return  Printing attribute class (category), an instance of class
     *          {@link java.lang.Class java.lang.Class}.
     */
    public final Class<? extends Attribute> getCategory() {
	return PresentationDirection.class;
    }

    /**
     * Get the name of the category of which this attribute value is an 
     * instance. 
     * <P>
     * For class PresentationDirection
     * the category name is <CODE>"presentation-direction"</CODE>. 
     *
     * @return  Attribute category name.
     */
    public final String getName() {
	return "presentation-direction";
    }

}
