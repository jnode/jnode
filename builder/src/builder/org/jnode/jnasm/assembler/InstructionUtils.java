/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.jnasm.assembler;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;
import java.lang.reflect.Field;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class InstructionUtils {
    private static final String INSTRUCTION_ID_SUFFIX = "_ISN";

    public static String[] getMnemonicArray(Map map) {
        Set entries = map.entrySet();
        String[] mnemonics = new String[entries.size()];
        for (Iterator it = entries.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            mnemonics[((Integer) entry.getValue()).intValue()] = (String) entry.getKey();
        }
        return mnemonics;
    }

    public static Map getInstructionMap(Class clazz) {
        Map map = new HashMap();
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = fields.length; i-- > 0;) {
                Field f = fields[i];
                String fn = f.getName();
                if (fn.endsWith(INSTRUCTION_ID_SUFFIX)) {
                    String mnemo = fn.substring(0, fn.length() - INSTRUCTION_ID_SUFFIX.length());
                    if (mnemo != null || !"".equals(mnemo)) {
                        map.put(mnemo.toLowerCase(), new Integer(f.getInt(null)));
                    }
                }
            }
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            System.exit(-1);
        }
        return Collections.unmodifiableMap(map);
    }
}
