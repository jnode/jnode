/*
 * Portions Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * =========================================================================== 
 *
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 *
 * ===========================================================================
 */
/*
 * $Id: DOMRetrievalMethod.java,v 1.24 2005/05/12 19:28:32 mullan Exp $
 */
package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.parsers.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;

/**
 * DOM-based implementation of RetrievalMethod.
 *
 * @author Sean Mullan
 * @author Joyce Leung
 */
public final class DOMRetrievalMethod extends DOMStructure
    implements RetrievalMethod, DOMURIReference {

    private final List transforms;
    private String uri;
    private String type;
    private Attr here;

    /**
     * Creates a <code>DOMRetrievalMethod</code> containing the specified 
     * URIReference and List of Transforms.
     *
     * @param uri the URI
     * @param type the type
     * @param transforms a list of {@link Transform}s. The list is defensively
     *    copied to prevent subsequent modification. May be <code>null</code>
     *    or empty.
     * @throws IllegalArgumentException if the format of <code>uri</code> is 
     *    invalid, as specified by Reference's URI attribute in the W3C
     *    specification for XML-Signature Syntax and Processing
     * @throws NullPointerException if <code>uriReference</code>
     *    is <code>null</code> 
     * @throws ClassCastException if <code>transforms</code> contains any
     *    entries that are not of type {@link Transform}
     */
    public DOMRetrievalMethod(String uri, String type, List transforms) {
	if (uri == null) {
	    throw new NullPointerException("uri cannot be null");
	}
        if (transforms == null || transforms.isEmpty()) {
            this.transforms = Collections.EMPTY_LIST;
        } else {
            List transformsCopy = new ArrayList(transforms);
            for (int i = 0, size = transformsCopy.size(); i < size; i++) {
                if (!(transformsCopy.get(i) instanceof Transform)) {
                    throw new ClassCastException
                        ("transforms["+i+"] is not a valid type");
                }
            }
            this.transforms = Collections.unmodifiableList(transformsCopy);
        }
	this.uri = uri;
        if ((uri != null) && (!uri.equals(""))) {
            try {
                new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

	this.type = type;
    }
	
    /**
     * Creates a <code>DOMRetrievalMethod</code> from an element.
     *
     * @param rmElem a RetrievalMethod element
     */
    public DOMRetrievalMethod(Element rmElem, XMLCryptoContext context) 
	throws MarshalException {
        // get URI and Type attributes
        uri = DOMUtils.getAttributeValue(rmElem, "URI");
        type = DOMUtils.getAttributeValue(rmElem, "Type");

	// get here node
	here = rmElem.getAttributeNodeNS(null, "URI");

        // get Transforms, if specified
        List transforms = new ArrayList();
        Element transformsElem = DOMUtils.getFirstChildElement(rmElem);
        if (transformsElem != null) {
            Element transformElem = 
		DOMUtils.getFirstChildElement(transformsElem);
            while (transformElem != null) {
                transforms.add(new DOMTransform(transformElem, context));
                transformElem = DOMUtils.getNextSiblingElement(transformElem);
	    }
        }
	if (transforms.isEmpty()) {
            this.transforms = Collections.EMPTY_LIST;
	} else {
            this.transforms = Collections.unmodifiableList(transforms);
	}
    }

    public String getURI() {
	return uri;
    }

    public String getType() {
	return type;
    }

    public List getTransforms() {
	return transforms;
    }

    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
	throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);

        Element rmElem = DOMUtils.createElement
            (ownerDoc, "RetrievalMethod", XMLSignature.XMLNS, dsPrefix);

        // add URI and Type attributes
        DOMUtils.setAttribute(rmElem, "URI", uri);
        DOMUtils.setAttribute(rmElem, "Type", type);

        // add Transforms elements
	if (!transforms.isEmpty()) {
	    Element transformsElem = DOMUtils.createElement
                (ownerDoc, "Transforms", XMLSignature.XMLNS, dsPrefix);
	    rmElem.appendChild(transformsElem);
	    for (int i = 0, size = transforms.size(); i < size; i++) {
	        ((DOMTransform) transforms.get(i)).marshal
		    (transformsElem, dsPrefix, context);
            }
	}

        parent.appendChild(rmElem);

	// save here node
	here = rmElem.getAttributeNodeNS(null, "URI");
    }

    public Node getHere() {
	return here;
    }

    public Data dereference(XMLCryptoContext context)
        throws URIReferenceException {

	if (context == null) {
	    throw new NullPointerException("context cannot be null");
	}

	/*
         * If URIDereferencer is specified in context; use it, otherwise use 
	 * built-in.
	 */
        URIDereferencer deref = context.getURIDereferencer();
        if (deref == null) {
	    deref = DOMURIDereferencer.INSTANCE;
	}

	Data data = deref.dereference(this, context);

        // pass dereferenced data through Transforms
	try {
	    for (int i = 0, size = transforms.size(); i < size; i++) {
                Transform transform = (Transform) transforms.get(i);
                data = ((DOMTransform) transform).transform(data, context);
            }
	} catch (Exception e) {
	    throw new URIReferenceException(e);
	}
	return data;
    }

    public XMLStructure dereferenceAsXMLStructure(XMLCryptoContext context)
	throws URIReferenceException {

	try {
	    ApacheData data = (ApacheData) dereference(context);
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(new ByteArrayInputStream
		(data.getXMLSignatureInput().getBytes()));
	    Element kiElem = doc.getDocumentElement();
            if (kiElem.getLocalName().equals("X509Data")) {
		return new DOMX509Data(kiElem);
	    } else {
		return null; // unsupported
	    }
	} catch (Exception e) {
	    throw new URIReferenceException(e);
	}
    }

    public boolean equals(Object obj) {
	if (this == obj) {
            return true;
	}
        if (!(obj instanceof RetrievalMethod)) {
            return false;
	}
        RetrievalMethod orm = (RetrievalMethod) obj;

	boolean typesEqual = (type == null ? orm.getType() == null :
            type.equals(orm.getType()));

	return (uri.equals(orm.getURI()) && 
	    transforms.equals(orm.getTransforms()) && typesEqual);
    }
}
