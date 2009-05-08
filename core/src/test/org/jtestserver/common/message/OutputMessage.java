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

import org.jtestserver.common.Status;

public class OutputMessage extends Message {
    private final StringBuilder message;

    public static final OutputMessage createResultMessage(MessageDescriptor desc, Object result) {
        OutputMessage output = null;
        
        if (desc.getResultClass() != null) {
            output =  new OutputMessage();
            output.append(desc.getResultClass(), result);
        }
        
        return output;
    }
    
    public static final OutputMessage createOutputMessage(MessageDescriptor desc, Object... params) {
        OutputMessage output = new OutputMessage();
        output.append(desc.getName());
        
        int iParam = 0;
        for (Class<?> paramClass : desc.getParamClasses()) {
            output.append(paramClass, params[iParam]);
            iParam++;
        }
        
        return output;
    }
    
    private OutputMessage() {
        message = new StringBuilder();
    }

    public String toMessage() {
        return message.toString();
    }

    private OutputMessage append(CharSequence chars) {
        if (message.length() > 0) {
            message.append(SEPARATOR);
        }
        
        // add characters one by one in order to escape
        // SEPARATOR and ESCAPE_CHARACTER characters, if any.
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            
            if ((c == SEPARATOR) || (c == ESCAPE_CHARACTER)) {
                message.append(ESCAPE_CHARACTER);                
            }
            
            message.append(c);
        }
        
        return this;
    }

    private OutputMessage append(int i) {
        return append(Integer.toString(i));
    }
    
    private final void append(Class<?> type, Object value) {
        if (value == null) {
            append(NULL);
        } else if (int.class.equals(type)) {
            append(int.class.cast(value));
        } else if (Integer.class.equals(type)) {
            append(Integer.class.cast(value).intValue());
        } else if (String.class.equals(type)) {
            append((String) value);
        } else if (Status.class.equals(type)) {
            append(((Status) value).toString());
        } else {
            //TODO throw exception
        }
    }
}
