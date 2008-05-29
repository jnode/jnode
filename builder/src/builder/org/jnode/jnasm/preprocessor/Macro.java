/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.jnasm.preprocessor;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class Macro {
    private static int localLabelCount = 0;
    private String name;
    private int paramCount;
    private int maxParamCount = -1;
    private String[] defaultValues;
    private String body;
    private String[] localLabels;

    public String getName() {
        return name;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    public void setMaxParamCount(int maxParamCount) {
        this.maxParamCount = maxParamCount;
    }

    public void setDefaultValues(String[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setLocalLabels(String[] localLabels) {
        this.localLabels = localLabels;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String expand(String[] params) {
        //if(paramCount != params.length) return null;
        String exp = body;
        for (int i = 0; i < localLabels.length; i++) {
            exp = exp.replaceAll(localLabels[i], "__jnasm_macro_local_label_" + localLabelCount++);
        }

        for (int i = 0; i < params.length; i++) {
            String par = params[i];
            par = (par == null) ? "" : par.trim();
            exp = exp.replaceAll("%" + (i + 1), par);
        }

        if (maxParamCount > params.length) {
            if (defaultValues == null) {
                for (int i = params.length; i < maxParamCount; i++) {
                    exp = exp.replaceAll("%" + (i + 1), "");
                }
            } else {
                for (int i = params.length; i < maxParamCount; i++) {
                    if (defaultValues.length > i - params.length) {
                        String def = defaultValues[i - params.length];
                        def = (def == null) ? "" : def.trim();
                        exp = exp.replaceAll("%" + (i + 1), def);
                    } else {
                        exp = exp.replaceAll("%" + (i + 1), "");
                    }
                }
            }
        }

        return exp;
    }

    public String toString() {
        return "MACRO " + name + " " + paramCount + "\n" + body + "\n\n";
    }
}
