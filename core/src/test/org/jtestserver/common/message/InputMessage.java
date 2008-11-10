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

import javax.net.ssl.SSLEngineResult.Status;

import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

public class InputMessage extends Message {
    private final StringTokenizer message;

    public static InputMessage create(Protocol protocol) throws ProtocolException, TimeoutException {
        return new InputMessage(protocol.receive());
    }
    
    private InputMessage(String message) {
        this.message = new StringTokenizer(message, SEPARATOR, false);
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
        String str = message.nextToken();
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
