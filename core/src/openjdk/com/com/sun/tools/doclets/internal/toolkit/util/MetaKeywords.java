/*
 * Copyright 2002-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.javadoc.*;
import java.util.*;

/**
 * Provides methods for creating an array of class, method and
 * field names to be included as meta keywords in the HTML header
 * of class pages.  These keywords improve search results
 * on browsers that look for keywords.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 * 
 * @author Doug Kramer
 */
public class MetaKeywords {
    
    /**
     * The global configuration information for this run.
     */
    private final Configuration configuration;

    /**
     * Constructor
     */
    public MetaKeywords(Configuration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Returns an array of strings where each element
     * is a class, method or field name.  This array is
     * used to create one meta keyword tag for each element.
     * Method parameter lists are converted to "()" and
     * overloads are combined.
     *
     * Constructors are not included because they have the same
     * name as the class, which is already included.
     * Nested class members are not included because their
     * definitions are on separate pages.
     */
    public String[] getMetaKeywords(ClassDoc classdoc) {
        ArrayList results = new ArrayList();

        // Add field and method keywords only if -keywords option is used
        if( configuration.keywords ) {
            results.addAll(getClassKeyword(classdoc));
            results.addAll(getMemberKeywords(classdoc.fields()));
            results.addAll(getMemberKeywords(classdoc.methods()));
        }
        return (String[]) results.toArray(new String[]{});
    }

    /**
     * Get the current class for a meta tag keyword, as the first
     * and only element of an array list.
     */
    protected ArrayList getClassKeyword(ClassDoc classdoc) {
        String cltypelower = classdoc.isInterface() ? "interface" : "class";
        ArrayList metakeywords = new ArrayList(1);
        metakeywords.add(classdoc.qualifiedName() + " " + cltypelower);
        return metakeywords;
    }
    
    /**
     * Get the package keywords.
     */
    public String[] getMetaKeywords(PackageDoc packageDoc) {
        if( configuration.keywords ) {
            String pkgName = Util.getPackageName(packageDoc);
            return new String[] { pkgName + " " + "package" };
        } else {
            return new String[] {}; 
        }
    }
    
    /**
     * Get the overview keywords.
     */
    public String[] getOverviewMetaKeywords(String title, String docTitle) {
        if( configuration.keywords ) {
            String windowOverview = configuration.getText(title);
            String[] metakeywords = { windowOverview };
            if (docTitle.length() > 0 ) {
                metakeywords[0] += ", " + docTitle;
            }
            return metakeywords;
        } else {
            return new String[] {}; 
        }
    }

    /**
     * Get members for meta tag keywords as an array,
     * where each member name is a string element of the array.
     * The parameter lists are not included in the keywords;
     * therefore all overloaded methods are combined.<br>
     * Example: getValue(Object) is returned in array as getValue()
     *
     * @param memberdocs  array of MemberDoc objects to be added to keywords
     */
    protected ArrayList getMemberKeywords(MemberDoc[] memberdocs) {
        ArrayList results = new ArrayList();
        String membername;
        for (int i=0; i < memberdocs.length; i++) {
            membername = memberdocs[i].name()
                             + (memberdocs[i].isMethod() ? "()" : "");
            if ( ! results.contains(membername) ) {
                results.add(membername);
            }
        }
        return results;
    }
}
