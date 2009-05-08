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


import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

public abstract class Message {
    
    public static Object send(Client<?, ?> client, MessageDescriptor desc, Object... params)
        throws ProtocolException, TimeoutException {
        
        boolean needReply = (desc.getResultClass() != null);
        OutputMessage output = OutputMessage.createOutputMessage(desc, params);
        String answer = client.send(output.toMessage(), needReply);

        Object result = null;
        if (needReply) {
            InputMessage input = InputMessage.create(answer);
            result = input.parse(desc.getResultClass());
        }
        return result;
    }

    static final char SEPARATOR = ';';
    static final char ESCAPE_CHARACTER = '\\';
    static final String NULL = "NULL";
}
