/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.internal.ws.wsdl.document;

import com.sun.tools.internal.ws.wsdl.framework.*;
import com.sun.tools.internal.ws.wscompile.ErrorReceiver;
import com.sun.tools.internal.ws.wscompile.AbortException;
import com.sun.tools.internal.ws.resources.WsdlMessages;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Entity corresponding to the "message" WSDL element.
 *
 * @author WS Development Team
 */
public class Message extends GlobalEntity {

    public Message(Defining defining, Locator locator, ErrorReceiver errReceiver) {
        super(defining, locator, errReceiver);
        _parts = new ArrayList<MessagePart>();
        _partsByName = new HashMap<String, MessagePart>();
    }

    public void add(MessagePart part) {
        if (_partsByName.get(part.getName()) != null){
            errorReceiver.error(part.getLocator(), WsdlMessages.VALIDATION_DUPLICATE_PART_NAME(getName(), part.getName()));
            throw new AbortException();
        }

        _partsByName.put(part.getName(), part);
        _parts.add(part);
    }

    public Iterator<MessagePart> parts() {
        return _parts.iterator();
    }

    public List<MessagePart> getParts(){
        return _parts;
    }

    public MessagePart getPart(String name) {
        return _partsByName.get(name);
    }

    public int numParts() {
        return _parts.size();
    }

    public Kind getKind() {
        return Kinds.MESSAGE;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_MESSAGE;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        super.withAllSubEntitiesDo(action);

        for (Iterator iter = _parts.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        for (Iterator<MessagePart> iter = _parts.iterator(); iter.hasNext();) {
            iter.next().accept(visitor);
        }
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (getName() == null) {
            errorReceiver.error(getLocator(), WsdlMessages.VALIDATION_MISSING_REQUIRED_ATTRIBUTE("name", "wsdl:message"));
            throw new AbortException();
        }
    }

    private Documentation _documentation;
    private List<MessagePart> _parts;
    private Map<String, MessagePart> _partsByName;
}
