/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.internal.toolkit.util;

import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.taglets.*;
import java.util.*;

/**
 * Search for the requested documentation.  Inherit documentation if necessary.
 * 
 * @author Jamie Ho
 * @since 1.5
 */
public class DocFinder {
    
    /**
     * The class that encapsulates the input.
     */
    public static class Input {
        /**
         * The method to search documentation from.
         */
        public MethodDoc method = null;
        /**
         * The taglet to search for documentation on behalf of. Null if we want 
         * to search for overall documentation.
         */
        public InheritableTaglet taglet = null;
        
        /**  
         * The id of the tag to retrieve documentation for. 
         */
        public String tagId = null;
        
        /**
         * The tag to retrieve documentation for.  This is only used for the
         * inheritDoc tag.
         */
        public Tag tag = null;
        
        /**
         * True if we only want to search for the first sentence.
         */
        public boolean isFirstSentence = false;
        
        /**                         
         * True if we are looking for documentation to replace the inheritDocTag.
         */
        public boolean isInheritDocTag = false;
        
        /**
         * Used to distinguish between type variable param tags and regular
         * param tags.
         */
        public boolean isTypeVariableParamTag = false;
        
        public Input() {}
        
        public Input(MethodDoc method, InheritableTaglet taglet, Tag tag, 
                boolean isFirstSentence, boolean isInheritDocTag) {
            this.method = method;
            this.taglet = taglet;
            this.tag = tag;
            this.isFirstSentence = isFirstSentence;
            this.isInheritDocTag = isInheritDocTag;
        }
        
        public Input(MethodDoc method, InheritableTaglet taglet, String tagId) {
            this.method = method;
            this.taglet = taglet;
            this.tagId = tagId;
        }
        
        public Input(MethodDoc method, InheritableTaglet taglet, String tagId,
            boolean isTypeVariableParamTag) {
            this.method = method;
            this.taglet = taglet;
            this.tagId = tagId;
            this.isTypeVariableParamTag = isTypeVariableParamTag; 
        }
        
        public Input(MethodDoc method, InheritableTaglet taglet) {
            this.method = method;
            this.taglet = taglet;  
        }
        
        public Input(MethodDoc method) {
            this.method = method; 
        }
        
        public Input(MethodDoc method, boolean isFirstSentence) {
            this.method = method;
            this.isFirstSentence = isFirstSentence; 
        }
        
        public Input copy() {
            Input clone = new Input();
            clone.method = this.method;
            clone.taglet = this.taglet;
            clone.tagId = this.tagId;
            clone.tag = this.tag;
            clone.isFirstSentence = this.isFirstSentence;
            clone.isInheritDocTag = this.isInheritDocTag;
            clone.isTypeVariableParamTag = this.isTypeVariableParamTag;
            return clone;
            
        }
    }
    
    /**
     * The class that encapsulates the output.
     */
    public static class Output {
        /**
         * The tag that holds the documentation.  Null if documentation
         * is not held by a tag.
         */       
        public Tag holderTag;
        
        /**
         * The Doc object that holds the documentation.
         */
        public Doc holder;
        
        /**
         * The inherited documentation.
         */
        public Tag[] inlineTags = new Tag[] {};
        
        /**
         * False if documentation could not be inherited.
         */
        public boolean isValidInheritDocTag = true;
        
        /**
         * When automatically inheriting throws tags, you sometime must inherit
         * more than one tag.  For example if the method declares that it throws
         * IOException and the overidden method has throws tags for IOException and
         * ZipException, both tags would be inherited because ZipException is a
         * subclass of IOException.  This subclass of DocFinder.Output allows
         * multiple tag inheritence.
         */
        public List tagList  = new ArrayList();
    }
    
    /**
     * Search for the requested comments in the given method.  If it does not
     * have comments, return documentation from the overriden method if possible. 
     * If the overriden method does not exist or does not have documentation to
     * inherit, search for documentation to inherit from implemented methods.
     * 
     * @param input the input object used to perform the search.
     *               
     * @return an Output object representing the documentation that was found.
     */
    public static Output search(Input input) {
        Output output = new Output();        
        if (input.isInheritDocTag) {
            //Do nothing because "method" does not have any documentation.
            //All it has it {@inheritDoc}.
        } else if (input.taglet == null) {
            //We want overall documentation.
            output.inlineTags = input.isFirstSentence ?
                input.method.firstSentenceTags() : 
                input.method.inlineTags();
            output.holder = input.method;
        } else {
            input.taglet.inherit(input, output);
        }
        
        if (output.inlineTags != null && output.inlineTags.length > 0) {
            return output;
        } 
        output.isValidInheritDocTag = false;
        Input inheritedSearchInput = input.copy(); 
        inheritedSearchInput.isInheritDocTag = false; 
        if (input.method.overriddenMethod() != null) {                       
            inheritedSearchInput.method = input.method.overriddenMethod();                     
            output = search(inheritedSearchInput);
            output.isValidInheritDocTag = true;
            if (output != null && output.inlineTags.length > 0) {
                return output;
            }
        } 
        //NOTE:  When we fix the bug where ClassDoc.interfaceTypes() does 
        //       not pass all implemented interfaces, we will use the 
        //       appropriate method here.
        MethodDoc[] implementedMethods = 
            (new ImplementedMethods(input.method, null)).build(false);
        for (int i = 0; i < implementedMethods.length; i++) {
            inheritedSearchInput.method = implementedMethods[i];
            output = search(inheritedSearchInput);
            output.isValidInheritDocTag = true;
            if (output != null && output.inlineTags.length > 0) {
                return output;
            }
        }
        return output;
    }
}
