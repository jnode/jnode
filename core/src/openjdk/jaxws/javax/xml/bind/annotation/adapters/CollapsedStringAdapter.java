/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.xml.bind.annotation.adapters;



/**
 * Built-in {@link XmlAdapter} to handle <tt>xs:token</tt> and its derived types.
 *
 * <p>
 * This adapter removes leading and trailing whitespaces, then truncate any
 * sequnce of tab, CR, LF, and SP by a single whitespace character ' '.
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB 2.0
 */
public class CollapsedStringAdapter extends XmlAdapter<String,String> {
    /**
     * Removes leading and trailing whitespaces of the string
     * given as the parameter, then truncate any
     * sequnce of tab, CR, LF, and SP by a single whitespace character ' '.
     */
    public String unmarshal(String text) {
        if(text==null)  return null;        // be defensive

        int len = text.length();

        // most of the texts are already in the collapsed form.
        // so look for the first whitespace in the hope that we will
        // never see it.
        int s=0;
        while(s<len) {
            if(isWhiteSpace(text.charAt(s)))
                break;
            s++;
        }
        if(s==len)
            // the input happens to be already collapsed.
            return text;

        // we now know that the input contains spaces.
        // let's sit down and do the collapsing normally.

        StringBuffer result = new StringBuffer(len /*allocate enough size to avoid re-allocation*/ );

        if(s!=0) {
            for( int i=0; i<s; i++ )
                result.append(text.charAt(i));
            result.append(' ');
        }

        boolean inStripMode = true;
        for (int i = s+1; i < len; i++) {
            char ch = text.charAt(i);
            boolean b = isWhiteSpace(ch);
            if (inStripMode && b)
                continue; // skip this character

            inStripMode = b;
            if (inStripMode)
                result.append(' ');
            else
                result.append(ch);
        }

        // remove trailing whitespaces
        len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ')
            result.setLength(len - 1);
        // whitespaces are already collapsed,
        // so all we have to do is to remove the last one character
        // if it's a whitespace.

        return result.toString();
    }

    /**
     * No-op.
     *
     * Just return the same string given as the parameter.
     */
    public String marshal(String s) {
        return s;
    }


    /** returns true if the specified char is a white space character. */
    protected static boolean isWhiteSpace(char ch) {
        // most of the characters are non-control characters.
        // so check that first to quickly return false for most of the cases.
        if( ch>0x20 )   return false;

        // other than we have to do four comparisons.
        return ch == 0x9 || ch == 0xA || ch == 0xD || ch == 0x20;
    }
}
