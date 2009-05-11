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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentSpecLoader {
    
    private Map<String, String> typeDefs;
    
    public ArgumentSpec[] loadArguments(SyntaxSpecAdapter element) {
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
                ArgumentSpec[] args = new ArgumentSpec[numArgs];
                for (int i = 0; i < numArgs; i++) {
                    args[i] = doLoad(element.getChild(i + start));
                }
                return args;
            }
        }
        throw new SyntaxFailureException("No arguments found in 'argument-bundle' node for : " + alias);
    }
    
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
    
    private ArgumentSpec doLoad(SyntaxSpecAdapter element) {
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
    
    private void parseParam(SyntaxSpecAdapter element, int i, Object[] params, Class<?>[] paramTypes) {
        if (!element.getName().equals("param")) {
            throw new SyntaxFailureException("'arg' contains a child that is not a 'param': " + element.getName());
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
    
    static class ArgumentSpec<T extends Argument<?>> {
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
