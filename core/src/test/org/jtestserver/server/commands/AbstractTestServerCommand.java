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
package org.jtestserver.server.commands;

import org.jtestserver.common.message.InputMessage;
import org.jtestserver.common.message.MessageDescriptor;
import org.jtestserver.common.message.OutputMessage;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.server.TestServerCommand;

public abstract class AbstractTestServerCommand<T> implements TestServerCommand {
    private final MessageDescriptor descriptor;
    
    public AbstractTestServerCommand(MessageDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public final String getName() {
        return descriptor.getName();
    }
    
    @Override
    public final OutputMessage execute(InputMessage input) throws ProtocolException, TimeoutException {
        Object[] params = input.parseParameters(descriptor);
        T result = execute(params);
        
        return OutputMessage.createResultMessage(descriptor, result);
    }
    
    protected abstract T execute(Object[] params) throws ProtocolException, TimeoutException;
}
