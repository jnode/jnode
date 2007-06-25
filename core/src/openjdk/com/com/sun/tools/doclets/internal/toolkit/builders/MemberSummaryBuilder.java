/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.internal.toolkit.builders;

import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.javadoc.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Builds the member summary.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 * 
 * @author Jamie Ho
 * @since 1.5
 */
public class MemberSummaryBuilder extends AbstractMemberBuilder {

	/**
	 * The XML root for this builder.
	 */
	public static final String NAME = "MemberSummary";

	/**
	 * The visible members for the given class.
	 */
	private VisibleMemberMap[] visibleMemberMaps;

	/**
	 * The member summary writers for the given class.
	 */
	private MemberSummaryWriter[] memberSummaryWriters;

	/**
	 * The type being documented.
	 */
	private ClassDoc classDoc;

	private MemberSummaryBuilder(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Construct a new MemberSummaryBuilder.
	 *
	 * @param classWriter   the writer for the class whose members are being 
	 *                      summarized.
	 * @param configuration the current configuration of the doclet.
	 */
	public static MemberSummaryBuilder getInstance(
		ClassWriter classWriter, Configuration configuration)
	throws Exception {
		MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
		builder.classDoc = classWriter.getClassDoc();
		builder.init(classWriter);
		return builder;
	}
    
    /**
     * Construct a new MemberSummaryBuilder.
     *
     * @param annotationTypeWriter the writer for the class whose members are 
     *                             being summarized.
     * @param configuration the current configuration of the doclet.
     */
    public static MemberSummaryBuilder getInstance(
        AnnotationTypeWriter annotationTypeWriter, Configuration configuration)
    throws Exception {
        MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
        builder.classDoc = annotationTypeWriter.getAnnotationTypeDoc();
        builder.init(annotationTypeWriter);
        return builder;
    }
    
    private void init(Object writer) throws Exception {
        visibleMemberMaps =
            new VisibleMemberMap[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            visibleMemberMaps[i] =
                new VisibleMemberMap(
                    classDoc,
                    i,
                    configuration.nodeprecated);
        }
        memberSummaryWriters =
            new MemberSummaryWriter[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            if (classDoc.isAnnotationType()) {            
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()? 
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                            (AnnotationTypeWriter) writer, i);
            } else {
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()? 
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                            (ClassWriter) writer, i);
            }
        }
        
    }

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * Return the specified visible member map.
	 *
	 * @param type the type of visible member map to return.
	 * @return the specified visible member map.
	 * @throws ArrayIndexOutOfBoundsException when the type is invalid.
	 * @see VisibleMemberMap
	 */
	public VisibleMemberMap getVisibleMemberMap(int type) {
		return visibleMemberMaps[type];
	}

	/**
	 * Return the specified member summary writer.
	 *
	 * @param type the type of member summary writer to return.
	 * @return the specified member summary writer.
	 * @throws ArrayIndexOutOfBoundsException when the type is invalid.
	 * @see VisibleMemberMap
	 */
	public MemberSummaryWriter getMemberSummaryWriter(int type) {
		return memberSummaryWriters[type];
	}

	/**
	 * Returns a list of methods that will be documented for the given class.  
	 * This information can be used for doclet specific documentation 
	 * generation.
	 *
	 * @param classDoc the {@link ClassDoc} we want to check.
	 * @param type the type of members to return.
	 * @return a list of methods that will be documented.
	 * @see VisibleMemberMap
	 */
	public List members(int type) {
		return visibleMemberMaps[type].getLeafClassMembers(configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	public void invokeMethod(
		String methodName,
		Class[] paramClasses,
		Object[] params)
		throws Exception {
		if (DEBUG) {
			configuration.root.printError(
				"DEBUG: " + this.getClass().getName() + "." + methodName);
		}
		Method method = this.getClass().getMethod(methodName, paramClasses);
		method.invoke(this, params);
	}

	/**
	 * Return true it there are any members to summarize.
	 *
	 * @return true if there are any members to summarize.
	 */
	public boolean hasMembersToDocument() {
        if (classDoc instanceof AnnotationTypeDoc) {
            return ((AnnotationTypeDoc) classDoc).elements().length > 0;            
        }
		for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
			VisibleMemberMap members = visibleMemberMaps[i];
			if (!members.noVisibleMembers()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Build the summary for the enum constants.
	 */
	public void buildEnumConstantsSummary() {
		buildSummary(
			memberSummaryWriters[VisibleMemberMap.ENUM_CONSTANTS],
			visibleMemberMaps[VisibleMemberMap.ENUM_CONSTANTS]);
	}
    
    /**
     * Build the summary for the optional members.
     */
    public void buildAnnotationTypeOptionalMemberSummary() {
        buildSummary(
            memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL], 
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL]);
    }
    
    /**
     * Build the summary for the optional members.
     */
    public void buildAnnotationTypeRequiredMemberSummary() {
        buildSummary(
            memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED], 
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED]);
    }

	/**
	 * Build the summary for the fields.
	 */
	public void buildFieldsSummary() {
		buildSummary(
			memberSummaryWriters[VisibleMemberMap.FIELDS],
			visibleMemberMaps[VisibleMemberMap.FIELDS]);
	}

	/**
	 * Build the inherited summary for the fields.
	 */
	public void buildFieldsInheritedSummary() {
		buildInheritedSummary(
			memberSummaryWriters[VisibleMemberMap.FIELDS],
			visibleMemberMaps[VisibleMemberMap.FIELDS]);
	}

	/**
	 * Build the summary for the nested classes.
	 */
	public void buildNestedClassesSummary() {
		buildSummary(
			memberSummaryWriters[VisibleMemberMap.INNERCLASSES],
			visibleMemberMaps[VisibleMemberMap.INNERCLASSES]);
	}

	/**
	 * Build the inherited summary for the nested classes.
	 */
	public void buildNestedClassesInheritedSummary() {
		buildInheritedSummary(
			memberSummaryWriters[VisibleMemberMap.INNERCLASSES],
			visibleMemberMaps[VisibleMemberMap.INNERCLASSES]);
	}

	/**
	 * Build the method summary.
	 */
	public void buildMethodsSummary() {
		buildSummary(
			memberSummaryWriters[VisibleMemberMap.METHODS],
			visibleMemberMaps[VisibleMemberMap.METHODS]);
	}

	/**
	 * Build the inherited method summary.
	 */
	public void buildMethodsInheritedSummary() {
		buildInheritedSummary(
			memberSummaryWriters[VisibleMemberMap.METHODS],
			visibleMemberMaps[VisibleMemberMap.METHODS]);
	}

	/**
	 * Build the constructor summary.
	 */
	public void buildConstructorsSummary() {
		buildSummary(
			memberSummaryWriters[VisibleMemberMap.CONSTRUCTORS],
			visibleMemberMaps[VisibleMemberMap.CONSTRUCTORS]);
	}

	/**
	 * Build the member summary for the given members.
	 *
	 * @param writer           the summary writer to write the output.
	 * @param visibleMemberMap the given members to summarize.
	 */
	private void buildSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap) {
        List members = new ArrayList(visibleMemberMap.getLeafClassMembers(
            configuration));
        if (members.size() > 0) {
            Collections.sort(members);
            writer.writeMemberSummaryHeader(classDoc);
            for (int i = 0; i < members.size(); i++) {
                ProgramElementDoc member = (ProgramElementDoc) members.get(i);                
                Tag[] firstSentenceTags = member.firstSentenceTags();
                if (member instanceof MethodDoc && firstSentenceTags.length == 0) {
                    //Inherit comments from overriden or implemented method if
                    //necessary.
                    DocFinder.Output inheritedDoc = 
                        DocFinder.search(new DocFinder.Input((MethodDoc) member));
                    if (inheritedDoc.holder != null && 
                            inheritedDoc.holder.firstSentenceTags().length > 0) {
                        firstSentenceTags = inheritedDoc.holder.firstSentenceTags();   
                    }
                }
                writer.writeMemberSummary(classDoc, member, firstSentenceTags, 
                    i == 0, i == members.size() - 1);
            }
            writer.writeMemberSummaryFooter(classDoc);
        }
	}

    /**
     * Build the inherited member summary for the given methods.
     *
     * @param writer           the writer for this member summary.
     * @param visibleMemberMap the map for the members to document.
     */
	private void buildInheritedSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap) {
        for (Iterator iter = visibleMemberMap.getVisibleClassesList().iterator();
                iter.hasNext();) {
            ClassDoc inhclass = (ClassDoc) (iter.next());
            if (! (inhclass.isPublic() || 
                Util.isLinkable(inhclass, configuration))) {
                continue;
            }
            if (inhclass == classDoc) {
                continue;
            }
            List inhmembers = visibleMemberMap.getMembersFor(inhclass);
            if (inhmembers.size() > 0) {
                Collections.sort(inhmembers);
                writer.writeInheritedMemberSummaryHeader(inhclass);
                for (int j = 0; j < inhmembers.size(); ++j) {
                    writer.writeInheritedMemberSummary(
                        inhclass.isPackagePrivate() && 
                            ! Util.isLinkable(inhclass, configuration) ?
                            classDoc : inhclass,
                        (ProgramElementDoc) inhmembers.get(j),
                        j == 0,
                        j == inhmembers.size() - 1);
                }
                writer.writeInheritedMemberSummaryFooter(inhclass);
            }
        }
    }
}
