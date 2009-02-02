/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * <li>The 'mandatory' flag says whether or not a value for the Argument <i>must</i> be provided.
 * <li>The 'multiple' flag says whether or not multiple values are allowed for the Argument.
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
     * This Argument flag indicates that the Argument is optional.
     */
    public static final int OPTIONAL = 0x001;
    
    /**
     * This Argument flag indicates that the Argument is mandatory.  At least
     * one instance of this Argument must be supplied.
     */
    public static final int MANDATORY = 0x002;

    /**
     * This Argument flag indicates that the Argument may have at most one value.
     */
    public static final int SINGLE = 0x004;
    
    /**
     * This Argument flag indicates that multiple instances of this Argument may 
     * be provided.
     */
    public static final int MULTIPLE = 0x008;

    /**
     * This Argument flag indicates that an Argument's value must denote an entity
     * that already exists in whatever domain that the Argument values corresponds to.
     */
    public static final int EXISTING = 0x010;
    
    /**
     * This Argument flag indicates that an Argument's value must denote an entity
     * that does not exists in whatever domain that the Argument values corresponds to.
     */
    public static final int NONEXISTENT = 0x020;
    
    private final String label;
    private final boolean mandatory;
    private final boolean multiple;
    private final boolean existing;
    private final boolean nonexistent;
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
        if ((flags & EXISTING) != 0 && (flags & NONEXISTENT) != 0) {
            throw new IllegalArgumentException("inconsistent flags: EXISTING and NONEXISTENT");
        }
        if ((flags & SINGLE) != 0 && (flags & MULTIPLE) != 0) {
            throw new IllegalArgumentException("inconsistent flags: SINGLE and MULTIPLE");
        }
        if ((flags & MANDATORY) != 0 && (flags & OPTIONAL) != 0) {
            throw new IllegalArgumentException("inconsistent flags: MANDATORY and OPTIONAL");
        }
        this.label = label;
        this.description = description;
        this.mandatory = (flags & MANDATORY) != 0;
        this.multiple = (flags & MULTIPLE) != 0;
        this.existing = (flags & EXISTING) != 0;
        this.nonexistent = (flags & NONEXISTENT) != 0;
        this.vArray = vArray;
    }
    
    /**
     * Reconstruct and return Argument flags equiivalent to those passed to the constructor.
     * @return the flags.
     */
    public int getFlags() {
        return ((mandatory ? MANDATORY : OPTIONAL) | (multiple ? MULTIPLE : SINGLE) |
                (existing ? EXISTING : 0) | (nonexistent ? NONEXISTENT : 0));
    }

    /**
     * If this method returns <code>true</code>, this Argument must be bound to an
     * argument in a CommandLine if it is used in a given concrete syntax.
     */
    public boolean isMandatory() {
        return mandatory;
    }
    
    /**
     * The label is the application's identifier for the Argument.  It is used to identify
     * the Argument in a concrete syntax specification.  It can also be used by the
     * application for name lookup of a bound argument's values.  
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
     */
    public V[] getValues() {
        checkArgumentsSet();
        return values.toArray(vArray);
    }

    public V getValue() throws SyntaxMultiplicityException {
        checkArgumentsSet();
        if (values.size() == 0) {
            return null;
        }
        int size = values.size();
        if (size == 1) {
            return values.get(0);
        } else {
            throw new SyntaxMultiplicityException(label + " is bound to " + size + " values");
        }
    }
    
    private void checkArgumentsSet() {
        if (bundle == null) {
            throw new SyntaxFailureException(
                    "This Argument is not associated with an ArgumentBundle");
        }
        // FIXME ... complete this
        switch (bundle.getStatus()) {
        
        }
    }

    final void addValue(V value) {
        values.add(value);
    }

    /**
     * Accept the Token as the value of this argument.  If the method call returns,
     * the caller should treat the Token as consumed.
     * 
     * @param value the token that will supply the Argument's value.
     */
    public final void accept(Token value) throws CommandSyntaxException {
        if (isSet() && !isMultiple()) {
            throw new SyntaxMultiplicityException("this argument cannot be repeated");
        }
        addValue(doAccept(value));
    }

    /**
     * This method is called by 'accept' after performing multiplicity checks.  It
     * should either return a non-null V to be accepted, or throw an exception.
     * 
     * @param value the token that will supply the Argument's value.
     */
    protected abstract V doAccept(Token value) throws CommandSyntaxException;

    /**
     * Perform argument completion on the supplied (partial) argument value.  The
     * results of the completion should be added to the supplied CompletionInfo.
     * <p>
     * The default behavior is to set no completion.  
     * Subtypes of Argument should override this method if they are capable of doing
     * non-trivial completion.  Completions should be registered by calling one
     * of the 'addCompletion' methods on the CompletionInfo.
     * 
     * @param completion the CompletionInfo object for registering any completions.
     * @param partial the argument string to be completed.
     */
    public void complete(CompletionInfo completion, String partial) {
        // set no completion
    }
    
    /**
     * Test if sufficient values have been bound to the Argument to satisfy the
     * the Argument's specified cardinality constraints.
     */
    public boolean isSatisfied() {
        return !isMandatory() || isSet();
    }
    
    /**
     * If this method returns <code>true</code>, this element may have
     * multiple instances in a CommandLine.
     */
    public boolean isMultiple() {
        return multiple;
    }
    
    /**
     * If this method returns <code>true</code>, an Argument value must correspond 
     * to an existing entity in the domain of entities denoted by the Argument type.
     */
    public boolean isExisting() {
        return existing;
    }

    /**
     * If this method returns <code>true</code>, an Argument value must <i>not</i> correspond 
     * to an existing entity in the domain of entities denoted by the Argument type.
     */
    public boolean isNonexistent() {
        return nonexistent;
    }

    void setBundle(ArgumentBundle bundle) {
        this.bundle = bundle;
    }
    
    ArgumentBundle getBundle() {
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
