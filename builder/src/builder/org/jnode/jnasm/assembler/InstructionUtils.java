/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;
import java.lang.reflect.Field;

/**
 * @author Levente S\u00e1ntha
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
