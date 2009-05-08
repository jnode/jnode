/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.common.message;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jtestserver.common.Status;

public class InputMessage extends Message {
    private static final String SEPARATOR_STR = new String(new char[]{SEPARATOR});
    
    private final String message;
    private int currentPosition = 0;

    public static InputMessage create(String answer) {
        return new InputMessage(answer);
    }
    
    private InputMessage(String message) {
        this.message = message;
    }

    public final Object[] parseParameters(MessageDescriptor desc) {
        List<Object> parameters = new ArrayList<Object>();
        
        for (Class<?> paramClass : desc.getParamClasses()) {
            parameters.add(parse(paramClass));
        }
        
        return parameters.toArray();
    }

    final Object parse(Class<?> type) {
        Object result = null;
        
        try {
            if (int.class.equals(type) || Integer.class.equals(type)) {
                result = getInt();
            } else if (String.class.equals(type)) {
                result = getString();
            } else if (Status.class.equals(type)) {
                result = getStatus();
            } else {
                //TODO throw exception
            }
        } catch (NullValueException nve) {
            // the parsed value is equals to the NULL constant 
            result = null;
        }
        
        return result;
    }        

    public String getString() { 
        // unescape SEPARATOR and ESCAPE_CHARACTER characters
        StringBuilder sb = new StringBuilder();
        for (; currentPosition < message.length(); currentPosition++) {
            char c = message.charAt(currentPosition);
            
            boolean unescaped = false;
            if (c == ESCAPE_CHARACTER) {
                if (currentPosition < (message.length() - 1)) {
                    char c2 = message.charAt(currentPosition + 1);
                    if ((c2 == ESCAPE_CHARACTER) || (c2 == SEPARATOR)) {
                        // special character, that need to be unescaped
                        sb.append(c2);
                        currentPosition++;
                        unescaped = true;
                    }
                }
            }
            
            if (!unescaped) {
                if (c == SEPARATOR) {
                    // found the "real" separator => end of the field 
                    currentPosition++;
                    break;
                } else {
                    // normal character
                    sb.append(c);
                }
            }
        }
        
        String str = sb.toString();
        if (NULL.equals(str)) {
            throw new NullValueException();
        }
        
        return str;
    }

    public Status getStatus() {
        return Status.valueOf(getString());
    }
    
    public int getInt() {
        return Integer.parseInt(getString());
    }
}
