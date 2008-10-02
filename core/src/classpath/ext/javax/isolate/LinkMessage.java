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
 
package javax.isolate;

import java.net.ServerSocket;
import java.net.Socket;

import org.jnode.vm.isolate.LinkMessageFactory;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class LinkMessage {

    public boolean containsComposite() {
        return false;
    }
    
    public boolean containsData() {
        return false;
    }
    
    public boolean containsIsolate() {
        return false;
    }
    
    public boolean containsLink() {
        return false;
    }
    
    public boolean containsServerSocket() {
        return false;
    }
    
    public boolean containsSocket() {
        return false;
    }
    
    public boolean containsStatus() {
        return false;
    }
    
    public boolean containsString() {
        return false;
    }
    
    public abstract Object extract();
    
    public LinkMessage[] extractComposite() {
        throw new IllegalStateException();
    }
    
    public byte[] extractData() {
        throw new IllegalStateException();        
    }
    
    public Isolate extractIsolate() {
        throw new IllegalStateException();        
    }
    
    public Link extractLink() {
        throw new IllegalStateException();                
    }
    
    public ServerSocket extractServerSocket() {
        throw new IllegalStateException();                        
    }
    
    public Socket extractSocket() {
        throw new IllegalStateException();                        
    }
    
    public IsolateStatus extractStatus() {
        throw new IllegalStateException();                        
    }
    
    public String extractString() {
        throw new IllegalStateException();                        
    }
    
    public static LinkMessage newCompositeMessage(LinkMessage... messages) {
        return LinkMessageFactory.newCompositeMessage(messages);
    }
    
    public static LinkMessage newDataMessage(byte[] bytes) {
        return newDataMessage(bytes, 0, bytes.length);
    }
    
    public static LinkMessage newDataMessage(byte[] bytes,
            int offset,
            int length) {
        return LinkMessageFactory.newDataMessage(bytes, offset, length);
    }
    
    public static LinkMessage newIsolateMessage(Isolate isolate) {
        return LinkMessageFactory.newIsolateMessage(isolate.getImpl());
    }
    
    public static LinkMessage newLinkMessage(Link link) {
        return LinkMessageFactory.newLinkMessage(link);        
    }
    
    public static LinkMessage newServerSocketMessage(ServerSocket socket) {
        return LinkMessageFactory.newServerSocketMessage(socket);
    }
    
    public static LinkMessage newSocketMessage(Socket socket) {
        return LinkMessageFactory.newSocketMessage(socket);
    }
    
    public static LinkMessage newStringMessage(String string) {
        return LinkMessageFactory.newStringMessage(string);
    }
}
