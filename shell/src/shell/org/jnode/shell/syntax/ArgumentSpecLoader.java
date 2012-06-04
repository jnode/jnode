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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads arguments from a syntax extension point descriptor.
 *
 * In order to provide the benefits of JNode's parsing, completion and help
 * systems, a command requires an {@link ArgumentBundle} to parse a
 * {@link org.jnode.shell.CommandLine}. Even if that command is not actually
 * a {@link org.jnode.shell.Command}, a description of {@code Argument}s can
 * be defined and used.
 *
 * Argument bundle descriptions are provided as sibling elements to syntax nodes
 * and are defined with {@code argument-bundle} tags. This tag requires than an {@code alias}
 * attribute be given. {@code argument-bundle} accepts {@code argument} tags that provide
 * definitions for constructing arguments. It also allows a single {@code typedefs} node to
 * be supplied as the first child.
 *
 * A {@code typedefs} block contains {@code typedef} tags with a mapping between an
 * arbitrary string, and a fully qualified class name for an {@code Argument} using
 * the {@code name} and {@code value} attributes respectively. Although the {@code name}
 * attribute has no restrictions as to what is used, the generally accepted format, for
 * clarity in the descriptor, is the name of the argument class. For example a typedef
 * for {@code org.jnode.shell.syntax.FileArgument} would be:
 * 
 * &lt;typedef name="FileArgument" value="org.jnode.shell.syntax.FileArgument"&gt;
 *
 * The rest of the child nodes to {@code argument-bundle} must be {@code argument} tags. 
 * {@code argument} tags require a 'label' and {@code type} attribute. The 'label' is the 
 * identified which maps the {@code Argument} to a node in the syntax. The {@code type} is 
 * the fully qualified class name of the {@code Argument}, or the name of a {@code typedef}.
 *
 * An {@code argument} tag may also contain {@code param} tags to specify more paramaters for an
 * {@code Argument}s constructor. The {@code param} tags must be specified in the order
 * in which they occur in the constructor. Each {@code param} tag specified two required
 * attributes, a {@code type} which tells the kind of the paramater, and a value to
 * supply the constuctor upon insantiation. The value must be parseable to the
 * given type. If an int type is supplied, but the value cannot be parsed as
 * an {@code int} than the parsing will fail.
 *
 * Currently the only supported types are int, String and flags.
 *
 * The {@code flags} type is a special type that fills in an {@code Argument}'s flags
 * paramater with something other than 0 (None). If the defined argument does
 * not set any non-default flags, it is recommended that no {@code param} be supplied
 * for clarity in the descriptor.
 *
 * @see Argument
 * @see ArgumentBundle
 * @author chris boertien
 */
public class ArgumentSpecLoader {
    
    private Map<String, String> typeDefs;
    
    /**
     * Parses a list of {@link Argument}s as {@code ArgumentSpec} objects.
     *
     * @return an array of {@code ArgumentSpec}s
     * @throws SyntaxFailureException if there was an error in the spec.
     */
    public ArgumentSpec<?>[] loadArguments(SyntaxSpecAdapter element) {
        String alias = element.getAttribute("alias");
        if (alias == null) {
            throw new SyntaxFailureException("'argument-bundle' element has no 'alias' attribute");
        }
        
        int numArgs = element.getNosChildren();
        int start = 0;
        if (numArgs > 0) {
            if (element.getChild(0).getName().equals("typedefs")) {
                numArgs--;
                start++;
                doTypeDefs(element.getChild(0));
            }
            if (numArgs > 0) {
                ArgumentSpec<?>[] args = new ArgumentSpec[numArgs];
                for (int i = 0; i < numArgs; i++) {
                    args[i] = doLoad(element.getChild(i + start));
                }
                return args;
            }
        }
        throw new SyntaxFailureException("No arguments found in 'argument-bundle' node for : " + alias);
    }
    
    /**
     * Parses typedefs used to map a String to a fully qualified class
     * name for an argument.
     */
    private void doTypeDefs(SyntaxSpecAdapter element) {
        int numTypeDefs = element.getNosChildren();
        if (numTypeDefs > 0) {
            typeDefs = new HashMap<String, String>(numTypeDefs);
            for (int i = 0; i < numTypeDefs; i++) {
                String name = element.getChild(i).getAttribute("name");
                String value = element.getChild(i).getAttribute("value");
                if (name == null || value == null) {
                    throw new SyntaxFailureException("Missing value or name in 'typedef'");
                }
                typeDefs.put(name, value);
            }
        } else {
            throw new SyntaxFailureException("'typedefs' found, but no 'typedef' nodes");
        }
    }
    
    /**
     * Parses an argument.
     */
    @SuppressWarnings("unchecked")
    private ArgumentSpec<?> doLoad(SyntaxSpecAdapter element) {
        if (!element.getName().equals("argument")) {
            throw new SyntaxFailureException("Not a valid child of 'argument-bundle': " + element.getName());
        }
        String type = element.getAttribute("type");
        String label = element.getAttribute("label");
        
        int numParams = element.getNosChildren();
        Object[] params;
        Class<?>[] paramTypes;
        if (numParams > 0) {
            params = new Object[numParams + 1];
            paramTypes = new Class<?>[numParams + 1];
            for (int i = 0; i < numParams; i++) {
                parseParam(element.getChild(i), i + 1, params, paramTypes);
            }
        } else {
            params = new Object[2];
            paramTypes = new Class<?>[2];
            params[1] = 0;
            paramTypes[1] = int.class;
        }
        
        params[0] = label;
        paramTypes[0] = String.class;
        
        try {
            if (typeDefs != null && typeDefs.containsKey(type)) {
                type = typeDefs.get(type);
            }
            Class<? extends Argument<?>> argClass = (Class<? extends Argument<?>>) Class.forName(type);
            Constructor<? extends Argument<?>> ctor = argClass.getConstructor(paramTypes);
            return new ArgumentSpec(ctor, params);
        } catch (ClassCastException ex) {
            throw new SyntaxFailureException("'type' is not a subclass of Argument: " + type);
        } catch (ClassNotFoundException ex) {
            throw new SyntaxFailureException("'type' could not be found: " + type);
        } catch (Exception ex) {
            throw new SyntaxFailureException("'type' could not be instantiated, invalid constructor");
        }
    }
    
    /**
     * Parses a paramater for the constructor of an argument.
     *
     * Currently acceptable types: flags, int, String
     */
    private void parseParam(SyntaxSpecAdapter element, int i, Object[] params, Class<?>[] paramTypes) {
        if (!element.getName().equals("param")) {
            throw new SyntaxFailureException("'argument' contains a child that is not a 'param': " + element.getName());
        }
        String type = element.getAttribute("type");
        String value = element.getAttribute("value");
        if (type == null) {
            throw new SyntaxFailureException("'type' cannot be null for node 'param'");
        }
        if (value == null) {
            throw new SyntaxFailureException("'value' cannot be null for node 'param'");
        }
        if (type.equals("String")) {
            params[i] = value;
            paramTypes[i] = String.class;
        } else if (type.equals("int")) {
            try {
                params[i] = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new SyntaxFailureException("'param' node declare 'type' int, but 'value' was not a valid int");
            }
            paramTypes[i] = int.class;
        } else if (type.equals("flags")) {
            params[i] = parseFlags(value);
            paramTypes[i] = int.class;
        } else {
            throw new SyntaxFailureException("The given 'type' is not supported: " + type);
        }
    }
    
    public static class ArgumentSpec<T extends Argument<?>> {
        private Constructor<T> ctor;
        private Object[] params;
        
        private ArgumentSpec(Constructor<T> ctor, Object[] params) {
            this.ctor = ctor;
            this.params = params;
        }
        
        Argument<?> instantiate() throws Exception {
            return (Argument<?>) ctor.newInstance(params);
        }
    }
    
    // this is nothing short of a hack, don't pay it much attention for now
    private int parseFlags(String flags) {
        String[] nameList = flags.trim().split("\\s*,\\s*");
        int res = 0;
        for (String name : nameList) {
            if (name != null && name.length() > 0) {
                if (name.equals("MANDATORY")) {
                    res |= Argument.MANDATORY;
                } else if (name.equals("OPTIONAL")) {
                    res |= Argument.OPTIONAL;
                } else if (name.equals("SINGLE")) {
                    res |= Argument.SINGLE;
                } else if (name.equals("MULTIPLE")) {
                    res |= Argument.MULTIPLE;
                } else if (name.equals("EXISTING")) {
                    res |= Argument.EXISTING;
                } else if (name.equals("NONEXISTENT")) {
                    res |= Argument.NONEXISTENT;
                } else {
                    throw new IllegalArgumentException("unknown flag name '" + name + "'");
                }
            }
        }
        return res;
    }
}
