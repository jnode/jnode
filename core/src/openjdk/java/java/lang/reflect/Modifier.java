/*
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.lang.reflect;

import java.security.AccessController;
import sun.reflect.LangReflectAccess;
import sun.reflect.ReflectionFactory;

/**
 * The Modifier class provides {@code static} methods and
 * constants to decode class and member access modifiers.  The sets of
 * modifiers are represented as integers with distinct bit positions
 * representing different modifiers.  The values for the constants
 * representing the modifiers are taken from <a
 * href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/VMSpecTOC.doc.html"><i>The
 * Java</i><sup><small>TM</small></sup> <i>Virtual Machine Specification, Second
 * edition</i></a> tables 
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75734">4.1</a>,
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#88358">4.4</a>,
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75568">4.5</a>, and 
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#88478">4.7</a>.
 *
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 *
 * @author Nakul Saraiya
 * @author Kenneth Russell
 */
public
class Modifier {

    /* 
     * Bootstrapping protocol between java.lang and java.lang.reflect
     *  packages 
     */
    static {
        sun.reflect.ReflectionFactory factory =
            (sun.reflect.ReflectionFactory) AccessController.doPrivileged(
                new ReflectionFactory.GetReflectionFactoryAction()
            );
        factory.setLangReflectAccess(new java.lang.reflect.ReflectAccess());
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code public} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code public} modifier; {@code false} otherwise.
     */
    public static boolean isPublic(int mod) {
	return (mod & PUBLIC) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code private} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code private} modifier; {@code false} otherwise.
     */
    public static boolean isPrivate(int mod) {
	return (mod & PRIVATE) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code protected} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code protected} modifier; {@code false} otherwise.
     */
    public static boolean isProtected(int mod) {
	return (mod & PROTECTED) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code static} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code static} modifier; {@code false} otherwise.
     */
    public static boolean isStatic(int mod) {
	return (mod & STATIC) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code final} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code final} modifier; {@code false} otherwise.
     */
    public static boolean isFinal(int mod) {
	return (mod & FINAL) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code synchronized} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code synchronized} modifier; {@code false} otherwise.
     */
    public static boolean isSynchronized(int mod) {
	return (mod & SYNCHRONIZED) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code volatile} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code volatile} modifier; {@code false} otherwise.
     */
    public static boolean isVolatile(int mod) {
	return (mod & VOLATILE) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code transient} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code transient} modifier; {@code false} otherwise.
     */
    public static boolean isTransient(int mod) {
	return (mod & TRANSIENT) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code native} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code native} modifier; {@code false} otherwise.
     */
    public static boolean isNative(int mod) {
	return (mod & NATIVE) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code interface} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code interface} modifier; {@code false} otherwise.
     */
    public static boolean isInterface(int mod) {
	return (mod & INTERFACE) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code abstract} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code abstract} modifier; {@code false} otherwise.
     */
    public static boolean isAbstract(int mod) {
	return (mod & ABSTRACT) != 0;
    }

    /**
     * Return {@code true} if the integer argument includes the
     * {@code strictfp} modifier, {@code false} otherwise.
     *
     * @param 	mod a set of modifiers
     * @return {@code true} if {@code mod} includes the
     * {@code strictfp} modifier; {@code false} otherwise.
     */
    public static boolean isStrict(int mod) {
	return (mod & STRICT) != 0;
    }

    /**
     * Return a string describing the access modifier flags in
     * the specified modifier. For example:
     * <blockquote><pre>
     *    public final synchronized strictfp
     * </pre></blockquote>
     * The modifier names are returned in an order consistent with the
     * suggested modifier orderings given in <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/j.title.doc.html"><em>The
     * Java Language Specification, Second Edition</em></a> sections
     * <a href="http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#21613">&sect;8.1.1</a>, 
     * <a href="http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#78091">&sect;8.3.1</a>, 
     * <a href="http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#78188">&sect;8.4.3</a>, 
     * <a href="http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#42018">&sect;8.8.3</a>, and
     * <a href="http://java.sun.com/docs/books/jls/second_edition/html/interfaces.doc.html#235947">&sect;9.1.1</a>.  
     * The full modifier ordering used by this method is:
     * <blockquote> {@code
     * public protected private abstract static final transient
     * volatile synchronized native strictfp
     * interface } </blockquote>
     * The {@code interface} modifier discussed in this class is
     * not a true modifier in the Java language and it appears after
     * all other modifiers listed by this method.  This method may
     * return a string of modifiers that are not valid modifiers of a
     * Java entity; in other words, no checking is done on the
     * possible validity of the combination of modifiers represented
     * by the input.
     *
     * @param	mod a set of modifiers
     * @return	a string representation of the set of modifiers
     * represented by {@code mod}
     */
    public static String toString(int mod) {
	StringBuffer sb = new StringBuffer();
	int len;

	if ((mod & PUBLIC) != 0)	sb.append("public ");
	if ((mod & PROTECTED) != 0)	sb.append("protected ");
	if ((mod & PRIVATE) != 0)	sb.append("private ");

	/* Canonical order */
	if ((mod & ABSTRACT) != 0)	sb.append("abstract ");
	if ((mod & STATIC) != 0)	sb.append("static ");
	if ((mod & FINAL) != 0)		sb.append("final ");
	if ((mod & TRANSIENT) != 0)	sb.append("transient ");
	if ((mod & VOLATILE) != 0)	sb.append("volatile ");
	if ((mod & SYNCHRONIZED) != 0)	sb.append("synchronized ");
	if ((mod & NATIVE) != 0)	sb.append("native ");
	if ((mod & STRICT) != 0)	sb.append("strictfp ");
	if ((mod & INTERFACE) != 0)	sb.append("interface ");

	if ((len = sb.length()) > 0)	/* trim trailing space */
	    return sb.toString().substring(0, len-1);
	return "";
    }

    /*
     * Access modifier flag constants from <em>The Java Virtual
     * Machine Specification, Second Edition</em>, tables 4.1, 4.4,
     * 4.5, and 4.7.
     */

    /**
     * The {@code int} value representing the {@code public}
     * modifier.
     */    
    public static final int PUBLIC           = 0x00000001;

    /**
     * The {@code int} value representing the {@code private}
     * modifier.
     */    
    public static final int PRIVATE          = 0x00000002;

    /**
     * The {@code int} value representing the {@code protected}
     * modifier.
     */    
    public static final int PROTECTED        = 0x00000004;

    /**
     * The {@code int} value representing the {@code static}
     * modifier.
     */    
    public static final int STATIC           = 0x00000008;

    /**
     * The {@code int} value representing the {@code final}
     * modifier.
     */    
    public static final int FINAL            = 0x00000010;

    /**
     * The {@code int} value representing the {@code synchronized}
     * modifier.
     */    
    public static final int SYNCHRONIZED     = 0x00000020;

    /**
     * The {@code int} value representing the {@code volatile}
     * modifier.
     */    
    public static final int VOLATILE         = 0x00000040;

    /**
     * The {@code int} value representing the {@code transient}
     * modifier.
     */    
    public static final int TRANSIENT        = 0x00000080;

    /**
     * The {@code int} value representing the {@code native}
     * modifier.
     */    
    public static final int NATIVE           = 0x00000100;

    /**
     * The {@code int} value representing the {@code interface}
     * modifier.
     */    
    public static final int INTERFACE        = 0x00000200;

    /**
     * The {@code int} value representing the {@code abstract}
     * modifier.
     */    
    public static final int ABSTRACT         = 0x00000400;

    /**
     * The {@code int} value representing the {@code strictfp}
     * modifier.
     */    
    public static final int STRICT           = 0x00000800;

    // Bits not (yet) exposed in the public API either because they
    // have different meanings for fields and methods and there is no
    // way to distinguish between the two in this class, or because
    // they are not Java programming language keywords
    static final int BRIDGE    = 0x00000040;
    static final int VARARGS   = 0x00000080;
    static final int SYNTHETIC = 0x00001000;
    static final int ANNOTATION= 0x00002000;
    static final int ENUM      = 0x00004000;
    static boolean isSynthetic(int mod) {
      return (mod & SYNTHETIC) != 0;
    }
}
