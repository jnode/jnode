/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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
 * corresponding Argument instances, and calls the Argument.accept(Token) method.  This typically 
 * converts the Token's value to an instance of the <V> type and adds it to the value holder.
 * <p>
 * An Argument has the following attributes:
 * <ul>
 * <li>The 'argName' is a label that allows the Argument to be matched against nodes in the Syntax 
 * or MuSyntax.  It needs to be unique in the context of the ArgumentBundle containing the Argument.
 * <li>The 'mandatory' flag says whether or not a value for the Argument <i>must</i> be provided.
 * <li>The 'multiple' flag says whether or not multiple values are allowed for the Argument.
 * <li>The 'conditional' flag denotes a conditional argument; i.e. one that must have a value if 
 * mentioned in the syntax, but not otherwise.  (This feature is experimental and may be dropped.)
 * <li>The 'description' string contains optional documentation for the Argument.  
 * </ul>
 * 
 * @author crawley@jnode.org
 *
 * @param <V> this is the value type for the Argument.
 */
public abstract class Argument<V> {
    
    public static final int OPTIONAL = 0x00;
    public static final int MANDATORY = 0x01;
    public static final int CONDITIONAL = 0x02;

    public static final int SINGLE = 0x00;
    public static final int MULTIPLE = 0x04;
    
    private final String label;
    private final boolean mandatory;
    private final boolean conditional;
    private final boolean multiple;
    private final String description;
    
    protected final List<V> values = new ArrayList<V>();
    
    private final V[] vArray;
    
    private ArgumentBundle bundle;
    
    
    /**
     * @param label The label that is used associate this Argument object to
     * a component of a Syntax.
     * @param flags This specifies the Argument attributes as a compact form
     * @param vArray A template array used by the getValues method.  It is 
     * typically zero length.
     * @param description Optional documentation for the argument.
     */
    Argument(String label, int flags, V[] vArray, String description) {
        super();
        this.label = label;
        this.description = description;
        this.mandatory = (flags & MANDATORY) != 0;
        this.conditional = (flags & CONDITIONAL) != 0;
        this.multiple = (flags & MULTIPLE) != 0;
        this.vArray = vArray;
    }

    /**
     * If this method returns <code>true</code>, this Argument must be bound to an
     * argument in a CommandLine if it is used in a given concrete syntax.
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * If this method returns <code>true</code>, this Argument is conditional; i.e. not
     * every concrete syntax is required to use the Argument.
     */
    public boolean isConditional() {
        return conditional;
    }
    
    /**
     * The label is the application's identifier for the Argument.  It is used to identify
     * the Argument in a concrete syntax specification.  It can also be used by the
     * application for name lookup of a bound argument's values.  
     */
    public String getLabel() {
        return label;
    }
    
    public boolean isSet() {
        checkArgumentsSet();
        return values.size() != 0;
    }
    
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
        }
        else {
            throw new SyntaxMultiplicityException(label + " is bound to " + size + " values");
        }
    }
    
    private void checkArgumentsSet() {
        if (bundle == null) {
            throw new SyntaxFailureException(
                    "This Argument is not associated with an ArgumentBundle");
        }
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
        doAccept(value);
    }

    /**
     * This method is called by 'accept' after performing multiplicity checks.
     * 
     * @param value the token that will supply the Argument's value.
     */
    protected abstract void doAccept(Token value) throws CommandSyntaxException;

    /**
     * Accept the String as the value of this argument.  This method is called with a String
     * that is part of a Token's value.  The default implementation is not to accept the String.
     * If the method call returns, the caller should treat the String as consumed.
     * 
     * @param str the String that will supply the Argument's value
     * @throws CommandSyntaxException if the String is not acceptable.
     */
    public void acceptEmbedded(String str) throws CommandSyntaxException {
        throw new UnsupportedOperationException("acceptEmbedded has no implementation");
    }
    
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

    @Override
    public String toString() {
        return "label=" + label;
    }

    void undoLastValue() {
        values.remove(values.size() - 1);
    }

    public final String format() {
        return label + ":" + argumentKind();
    }
    
    /**
     * Get the argument's optional description
     * @return the description or <code>null</code>
     */
    public String getDescription() {
        return description;
    }
    
    protected abstract String argumentKind();
}
