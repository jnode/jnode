/*
 * Copyright 2001-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.internal.toolkit.taglets;

import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.javadoc.*;

/**
 * A taglet that represents the @return tag.
 * 
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 * 
 * @author Jamie Ho
 * @since 1.4
 */
public class ReturnTaglet extends BaseExecutableMemberTaglet 
        implements InheritableTaglet {
    
    public ReturnTaglet() {
        name = "return";
    }
    
    /**
     * {@inheritDoc}
     */
    public void inherit(DocFinder.Input input, DocFinder.Output output) {
       Tag[] tags = input.method.tags("return");
        if (tags.length > 0) {
            output.holder = input.method;
            output.holderTag = tags[0];
            output.inlineTags = input.isFirstSentence ?
                tags[0].firstSentenceTags() : tags[0].inlineTags();
        } 
    }
    
    /**
     * Return true if this <code>Taglet</code>
     * is used in constructor documentation.
     * @return true if this <code>Taglet</code>
     * is used in constructor documentation and false
     * otherwise.
     */
    public boolean inConstructor() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) {
        Type returnType = ((MethodDoc) holder).returnType();        
        Tag[] tags = holder.tags(name);
        
        //Make sure we are not using @return tag on method with void return type.      
        if (returnType.isPrimitive() && returnType.typeName().equals("void")) {
            if (tags.length > 0) {            
                writer.getMsgRetriever().warning(holder.position(), 
                    "doclet.Return_tag_on_void_method");
            }
            return null;
        }
        //Inherit @return tag if necessary.
        if (tags.length == 0) {
            DocFinder.Output inheritedDoc = 
                DocFinder.search(new DocFinder.Input((MethodDoc) holder, this));
            tags = inheritedDoc.holderTag == null ? tags : new Tag[] {inheritedDoc.holderTag};
        }
        return tags.length > 0 ? writer.returnTagOutput(tags[0]) : null;
    }
}
