/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * @(#)$Id: MinimumEscapeHandler.java,v 1.1 2005/04/15 20:03:47 kohsuke Exp $
 */

package com.sun.xml.internal.bind.marshaller;

import java.io.IOException;
import java.io.Writer;

/**
 * Performs no character escaping. Usable only when the output encoding
 * is UTF, but this handler gives the maximum performance.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class MinimumEscapeHandler implements CharacterEscapeHandler {
    
    private MinimumEscapeHandler() {}  // no instanciation please
    
    public static final CharacterEscapeHandler theInstance = new MinimumEscapeHandler(); 
    
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        // avoid calling the Writerwrite method too much by assuming
        // that the escaping occurs rarely.
        // profiling revealed that this is faster than the naive code.
        int limit = start+length;
        for (int i = start; i < limit; i++) {
            char c = ch[i];
            if( c=='&' || c=='<' || c=='>' || (c=='\"' && isAttVal) ) {
                if(i!=start)
                    out.write(ch,start,i-start);
                start = i+1;
                switch (ch[i]) {
                case '&' :
                    out.write("&amp;");
                    break;
                case '<' :
                    out.write("&lt;");
                    break;
                case '>' :
                    out.write("&gt;");
                    break;
                case '\"' :
                    out.write("&quot;");
                    break;
                }
            }
        }
        
        if( start!=limit )
            out.write(ch,start,limit-start);
    }

}
