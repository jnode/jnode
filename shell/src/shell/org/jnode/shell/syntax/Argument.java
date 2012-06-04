/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.syntax;

import java.util.ArrayList;
import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * The Argument class is the base class for argument value holders in the org.jnode.shell.syntax 
 * system.  An command instance object creates an Argument instance for each formal 
 * command-line parameter, and assembled them into an ArgumentBundle.  As a command line is parsed 
 * against the syntax, the parser matches syntactic elements with argument strings, locates the
 * corresponding Argument instances, and calls the Argument.accept(Token) method.  This then calls
 * doAccept(Token) in a subclass which typically tries to convert the Token's value to an instance 
 * of the <V> type.  The doAccept method should either return a non-null V to be accepted, 
 * or throw an exception.
 * <p>
 * An Argument has the following attributes:
 * <ul>
 * <li>The 'argName' is a label that allows the Argument to be matched against nodes in the Syntax 
 * or MuSyntax.  It needs to be unique in the context of the ArgumentBundle containing the Argument.
 * <li>The 'flags' word holds common and subtype specific flags that constrain the way it may
 *     be populated.  The common flags are described in the constants section below.
 * <li>The 'description' string contains optional documentation for the Argument.  
 * </ul>
 * 
 * Many methods on the Argument class check that the Argument is a member of an ArgumentBundle,
 * throw the unchecked exception {@link SyntaxFailureException} if this is not the case.
 * 
 * @author crawley@jnode.org
 *
 * @param <V> this is the value type for the Argument.
 */
public abstract class Argument<V> {
    
    /**
     * This Argument flag indicates that the Argument is optional.  This is the
     * opposite of MANDATORY, and is the default if neither are specified.
     */
    public static final int OPTIONAL = 0x001;
    
    /**
     * This Argument flag indicates that the Argument is mandatory; i.e that least
     * one instance of this Argument must be supplied in a command line.  This is
     * the opposite of OPTIONAL.
     */
    public static final int MANDATORY = 0x002;

    /**
     * This Argument flag indicates that the Argument may have at most one value.
     * This is the opposite of MULTIPLE and the default if neither are specified.
     */
    public static final int SINGLE = 0x004;
    
    /**
     * This Argument flag indicates that multiple instances of this Argument may 
     * be provided.  This is the opposite of SINGLE.
     */
    public static final int MULTIPLE = 0x008;

    /**
     * This Argument flag indicates that an Argument's value must denote an entity
     * that already exists in whatever domain that the Argument values corresponds to.
     * Note that this is <b>not</b> the logical negation of NONEXISTENT!
     */
    public static final int EXISTING = 0x010;
    
    /**
     * This Argument flag indicates that an Argument's value must denote an entity
     * that does not exists in whatever domain that the Argument values corresponds to.
     * Note that this is <b>not</b> the logical negation of EXISTING!
     */
    public static final int NONEXISTENT = 0x020;

    /**
     * Flag bits in this bitset are either common flags, or reserved for future use as
     * common flags.
     */
    public static final int COMMON_FLAGS = 0x0000ffff;
    
    /**
     * Flag bits in this bitset are available for use as Argument-subclass specific flags.
     * Flags in this range may be overridden by a Syntax.
     */
    public static final int SPECIFIC_OVERRIDABLE_FLAGS = 0x00ff0000;

    /**
     * Flag bits in this bitset are available for use as Argument-subclass specific flags.
     * Flags in this range may NOT be overridden by a Syntax.
     */
    private static final int SPECIFIC_NONOVERRIDABLE_FLAGS = 0xff000000;
    
    /**
     * Flag bits in this bitset may not be overridden by a Syntax. 
     */
    public static final int NONOVERRIDABLE_FLAGS = 
        SINGLE | MULTIPLE | MANDATORY | OPTIONAL | SPECIFIC_NONOVERRIDABLE_FLAGS;
    
    private final String label;
    private final int flags;
    private final String description;
    
    protected final List<V> values = new ArrayList<V>();
    
    final V[] vArray;
    
    private ArgumentBundle bundle;
    
    
    /**
     * @param label The label that is used associate this Argument object to
     * a component of a Syntax.
     * @param flags This specifies the Argument attributes as a compact form
     * @param vArray A template array used by the getValues method.  It is 
     * typically zero length.
     * @param description Optional documentation for the argument.
     * @throws IllegalArgumentException if the flags are inconsistent
     */
    protected Argument(String label, int flags, V[] vArray, String description) 
        throws IllegalArgumentException {
        super();
        checkFlags(flags);
        this.label = label;
        this.description = description;
        this.flags = flags;
        this.vArray = vArray;
    }
    
    /**
     * Check that the supplied flags are consistent.  
     * <p>
     * Note: this method may be overridden in child classes, but an override should 
     * call this method to check the common flags. 
     * @param flags the flags to be checked.
     * @throws IllegalArgumentException
     */
    protected void checkFlags(int flags) throws IllegalArgumentException {
        if ((flags & EXISTING) != 0 && (flags & NONEXISTENT) != 0) {
            throw new IllegalArgumentException("inconsistent flags: EXISTING and NONEXISTENT");
        }
        if ((flags & SINGLE) != 0 && (flags & MULTIPLE) != 0) {
            throw new IllegalArgumentException("inconsistent flags: SINGLE and MULTIPLE");
        }
        if ((flags & MANDATORY) != 0 && (flags & OPTIONAL) != 0) {
            throw new IllegalArgumentException("inconsistent flags: MANDATORY and OPTIONAL");
        }
    }
    
    /**
     * Return the flags as passed to the constructor.
     * @return the flags.
     */
    public int getFlags() {
        return flags;
    }
    
    /**
     * Convert a comma-separated list of names to a flags word.  The current implementation
     * will silently ignore empty names; e.g. in {@code "MANDATORY,,SINGLE"} or 
     * {@code ",SINGLE"}.
     * 
     * @param names the names separated by commas and optional whitespace.
     * @return the flags
     * @throws IllegalArgument if the list contains unknown flag names.
     */
    public final int namesToFlags(String names) throws IllegalArgumentException {
        String[] nameList = names.trim().split("\\s*,\\s*");
        int res = 0;
        for (String name : nameList) {
            if (name != null && name.length() > 0) {
                res |= nameToFlag(name);
            }
        }
        return res;
    }
    
    /**
     * Convert a flag name to a flag.  
     * <p>
     * Note: this method may be overridden in child 
     * classes, but an override should end by calling this method to deal
     * with flag names that it doesn't understand.
     * 
     * @param name the name to be converted
     * @return the corresponding flag
     * @throws IllegalArgumentWxception if the name is not recognized
     */
    public int nameToFlag(String name) throws IllegalArgumentException {
        if (name.equals("MANDATORY")) {
            return MANDATORY;
        } else if (name.equals("OPTIONAL")) {
            return OPTIONAL;
        } else if (name.equals("SINGLE")) {
            return SINGLE;
        } else if (name.equals("MULTIPLE")) {
            return MULTIPLE;
        } else if (name.equals("EXISTING")) {
            return EXISTING;
        } else if (name.equals("NONEXISTENT")) {
            return NONEXISTENT;
        } else {
            throw new IllegalArgumentException("unknown flag name '" + name + "'");
        }
    }

    /**
     * If this method returns <code>true</code>, this Argument must be bound to an
     * argument in a CommandLine if it is used in a given concrete syntax.
     */
    public boolean isMandatory() {
        return isMandatory(flags);
    }
    
    /**
     * If this method returns <code>true</code>, this Argument need not be bound to an
     * argument in a CommandLine if it is used in a given concrete syntax.
     */
    public boolean isOptional() {
        return isOptional(flags);
    }
    
    /**
     * If this method returns <code>true</code>, this element may have
     * multiple instances in a CommandLine.
     */
    public boolean isMultiple() {
        return isMultiple(flags);
    }
    
    /**
     * If this method returns <code>true</code>, this element must have at
     * most one instance in a CommandLine.
     */
    public boolean isSingle() {
        return isSingle(flags);
    }
    
    /**
     * If this method returns <code>true</code>, an Argument value must correspond 
     * to an existing entity in the domain of entities denoted by the Argument type.
     */
    public boolean isExisting() {
        return isExisting(flags);
    }

    /**
     * If this method returns <code>true</code>, an Argument value must <i>not</i> correspond 
     * to an existing entity in the domain of entities denoted by the Argument type.
     */
    public boolean isNonexistent() {
        return isNonexistent(flags);
    }
    
    /**
     * If this method returns <code>true</code>, the flags say that the corresponding Argument 
     * must be bound to an argument in a CommandLine if it is used in a given concrete syntax.
     */
    public static boolean isMandatory(int flags) {
        return (flags & MANDATORY) != 0;
    }
    
    /**
     * If this method returns <code>true</code>, the flags say that the corresponding Argument 
     * need not be bound to an argument in a CommandLine if it is used in a given concrete syntax.
     */
    public static boolean isOptional(int flags) {
        return (flags & MANDATORY) == 0;
    }
    
    /**
     * If this method returns <code>true</code>, the corresponding Argument may have
     * multiple instances in a CommandLine.
     */
    public static boolean isMultiple(int flags) {
        return (flags & MULTIPLE) != 0;
    }
    
    /**
     * If this method returns <code>true</code>, the corresponding Argument must have at
     * most one instance in a CommandLine.
     */
    public static boolean isSingle(int flags) {
        return (flags & MULTIPLE) == 0;
    }
    
    /**
     * If this method returns <code>true</code>, the corresponding Argument value must denote 
     * an existing entity.
     */
    public static boolean isExisting(int flags) {
        return (flags & EXISTING) != 0;
    }

    /**
     * If this method returns <code>true</code>, the corresponding Argument value must 
     * <i>not</i> denote an existing entity.
     */
    public boolean isNonexistent(int flags) {
        return (flags & NONEXISTENT) != 0;
    }
    
    /**
     * The label is the application's identifier for the Argument.  It is used to identify
     * the Argument in a concrete syntax specification.  The label could also be used by the
     * application for name lookup of the Argument in the {@link ArgumentBundle}, but the
     * normal design pattern is for a Command class to retain references to each Argument
     * in private attributes.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Test if this Argument currently has a bound value or values.
     */
    public boolean isSet() {
        checkArgumentsSet();
        return values.size() != 0;
    }
    
    /**
     * Get this Arguments bound values as an array. 
     * @return an array of values, possibly empty but never {@code null}.
     */
    public V[] getValues() {
        checkArgumentsSet();
        return values.toArray(vArray);
    }

    /**
     * Get this Argument's single bound value.
     * @return the value or {@code null}.
     * @throws SyntaxMultiplicityException if this is a multi-valued Argument
     *     bound to more than one value. 
     */
    public V getValue() throws SyntaxMultiplicityException {
        checkArgumentsSet();
        int size = values.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return values.get(0);
        } else {
            throw new SyntaxMultiplicityException(
                    label + " is bound to " + size + " values");
        }
    }
    
    private void checkArgumentsSet() {
        if (bundle == null) {
            throw new SyntaxFailureException(
                    "This Argument is not associated with an ArgumentBundle");
        }
        
        switch (bundle.getStatus()) {
            case ArgumentBundle.UNPARSED:
                throw new SyntaxFailureException(
                        "This Argument's ArgumentBundle has not been " +
                        "populated by the syntax parser");
            case ArgumentBundle.PARSE_FAILED:
                throw new SyntaxFailureException(
                        "The syntax parser failed for this Argument's ArgumentBundle");
        }
    }

    final void addValue(V value) {
        values.add(value);
    }

    /**
     * Try to accept the Token as the value of this argument.  If the method call returns,
     * the caller should treat the Token as consumed.
     * <p>
     * After merging the flags and doing some preliminary checks, this method calls
     * the 'doAccept' method to perform the appropriate checking and token-to-value
     * conversion.  The value returned by the 'doAccept' call is then bound to 
     * this Argument.
     * 
     * @param value the token that will supply the Argument's value.
     * @param flags extra flags from the syntax system.  These will be OR'ed with
     *     the Arguments existing flags, after masking out an in the flag set defined
     *     by {@link #NONOVERRIDABLE_FLAGS}.
     * @throws CommandSyntaxException if the value is unacceptable, or if an attempt
     *     is made to repeat a single-valued Argument.
     */
    public final void accept(Token value, int flags) 
        throws CommandSyntaxException, IllegalArgumentException {
        if (isSet() && !isMultiple()) {
            throw new SyntaxMultiplicityException("this argument cannot be repeated");
        }
        flags = (flags & ~NONOVERRIDABLE_FLAGS) | this.flags;
        checkFlags(flags);
        addValue(doAccept(value, flags));
    }

    /**
     * This method is called by 'accept' after performing multiplicity checks to
     * check that the supplied token is valid and to convert it into a value of
     * the required type. It should either 'accept' the value by returning
     * a non-null V, or throw an exception whose message says why the value is
     * unacceptable.
     * 
     * @param value the token that will supply the Argument's value.
     * @param flags the flags to be used.
     * @return a (non-{@code null}) value to be accepted
     * @throws CommandSyntaxException if the value is unacceptable
     */
    protected abstract V doAccept(Token value, int flags) throws CommandSyntaxException;

    /**
     * Perform argument completion on the supplied (partial) argument value.  The
     * results of the completion should be added to the supplied CompletionInfo.
     * <p>
     * The default behavior is to set no completion.  
     * Subtypes of Argument should override this method if they are capable of doing
     * non-trivial completion.  Completions should be registered by calling one
     * of the 'addCompletion' methods on the CompletionInfo.
     * 
     * @param completions the CompletionInfo object for registering any completions.
     * @param partial the argument string to be completed.
     */
    public final void complete(CompletionInfo completions, String partial, int flags) {
        if (isSet() && !isMultiple()) {
            throw new SyntaxMultiplicityException("this argument cannot be repeated");
        }
        flags = (flags & ~NONOVERRIDABLE_FLAGS) | this.flags;
        checkFlags(flags);
        doComplete(completions, partial, flags);
    }

    /**
     * Perform argument completion on the supplied (partial) argument value.  The
     * results of the completion should be added to the supplied CompletionInfo.
     * Completions posted by calling {@link CompletionInfo#addCompletion(String)}
     * or {@link CompletionInfo#addCompletion(String, boolean)}.
     * <p>
     * The default behavior of this method is to do no completion.  Subtypes of 
     * Argument should override this method if they are capable of doing <i>useful</i>
     * completion.  Note that not all completion is useful.  For example, it is a
     * bad idea post all legal completions for a large integer range.  Also, a 
     * an override should avoid posting completions that would not be accepted
     * by the {@link #doAccept} method, as this will lead to confusing behavior.
     * 
     * @param completions the {@link CompletionInfo} object for posting possible
     *    completions.
     * @param partial the argument string to be completed.
     */
    public void doComplete(CompletionInfo completions, String partial, int flags) {
        // set no completion
    }

    void setBundle(ArgumentBundle bundle) {
        this.bundle = bundle;
    }
    
    public ArgumentBundle getBundle() {
        return bundle;
    }
    
    /**
     * Clear the argument's values
     */
    void clear() {
        values.clear();
    }

    /**
     * Render this Argument for debug purposes.
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + state() + "}";
    }

    /**
     * Render this Argument's state for debug purposes.  Override this
     * method in child classes to dump any relevant child class state.
     */
    protected String state() {
        return "label=" + label;
    }

    /**
     * This method is called by MuParser while backtracking.
     */
    void undoLastValue() {
        values.remove(values.size() - 1);
    }

    /**
     * Format this argument for a usage message.
     */
    public final String formatForUsage() {
        return label;
    }
    
    /**
     * Get the argument's optional description
     * @return the description or <code>null</code>
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Return a String that describes the 'kind' of the Argument; e.g. a 
     * "class name" or an "integer".
     */
    protected abstract String argumentKind();

    /**
     * Get a description of the argument's type
     * @return the argument type description.
     */
    public String getTypeDescription() {
        return argumentKind();
    }
}
