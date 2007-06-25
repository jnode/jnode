/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Id: DOMX509Data.java,v 1.20 2005/05/12 19:28:34 mullan Exp $
 */
package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.security.cert.*;
import java.util.*;
import javax.xml.crypto.*;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.security.auth.x500.X500Principal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * DOM-based implementation of X509Data.
 *
 * @author Sean Mullan
 */
//@@@ check for illegal combinations of data violating MUSTs in W3c spec
public final class DOMX509Data extends DOMStructure implements X509Data {

    private final List content;
    private CertificateFactory cf; //FIX - make this static?

    /**
     * Creates a DOMX509Data.
     *
     * @param content a list of one or more X.509 data types. Valid types are
     *    {@link String} (subject names), <code>byte[]</code> (subject key ids),
     *    {@link java.security.cert.X509Certificate}, {@link X509CRL},
     *    or {@link javax.xml.dsig.XMLStructure} ({@link X509IssuerSerial}
     *    objects or elements from an external namespace). The list is 
     *    defensively copied to protect against subsequent modification.
     * @return a <code>X509Data</code>
     * @throws NullPointerException if <code>content</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>content</code> is empty
     * @throws ClassCastException if <code>content</code> contains any entries
     *    that are not of one of the valid types mentioned above
     */
    public DOMX509Data(List content) {
        if (content == null) {
            throw new NullPointerException("content cannot be null");
        }
        List contentCopy = new ArrayList(content);
        if (contentCopy.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        }
        for (int i = 0, size = contentCopy.size(); i < size; i++) {
	    Object x509Type = contentCopy.get(i);
	    if (x509Type instanceof String) {
		new X500Principal((String) x509Type);
	    } else if (!(x509Type instanceof byte[]) &&
                !(x509Type instanceof X509Certificate) &&
                !(x509Type instanceof X509CRL) &&
                !(x509Type instanceof XMLStructure)) {
                throw new ClassCastException
                    ("content["+i+"] is not a valid X509Data type");
            }
        }
        this.content = Collections.unmodifiableList(contentCopy);
    }

    /**
     * Creates a <code>DOMX509Data</code> from an element.
     *
     * @param xdElem an X509Data element
     * @throws MarshalException if there is an error while unmarshalling
     */
    public DOMX509Data(Element xdElem) throws MarshalException {
        // get all children nodes
        NodeList nl = xdElem.getChildNodes();
	int length = nl.getLength();
	List content = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            Node child = nl.item(i);
            // ignore all non-Element nodes
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
	    }

            Element childElem = (Element) child;
	    String localName = childElem.getLocalName();
            if (localName.equals("X509Certificate")) {
                content.add(unmarshalX509Certificate(childElem));
            } else if (localName.equals("X509IssuerSerial")) {
                content.add(new DOMX509IssuerSerial(childElem));
            } else if (localName.equals("X509SubjectName")) {
                content.add(childElem.getFirstChild().getNodeValue());
            } else if (localName.equals("X509SKI")) {
		try {
                    content.add(Base64.decode(childElem));
		} catch (Base64DecodingException bde) {
		    throw new MarshalException("cannot decode X509SKI", bde);
		}
            } else if (localName.equals("X509CRL")) {
                content.add(unmarshalX509CRL(childElem));
            } else {
		content.add(new javax.xml.crypto.dom.DOMStructure(childElem));
            }
        }
        this.content = Collections.unmodifiableList(content);
    }

    public List getContent() {
	return content;
    }

    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
	throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);

        Element xdElem = DOMUtils.createElement
            (ownerDoc, "X509Data", XMLSignature.XMLNS, dsPrefix);

        // append children and preserve order
	for (int i = 0, size = content.size(); i < size; i++) {
	    Object object = content.get(i);
            if (object instanceof X509Certificate) {
                marshalCert((X509Certificate) object,xdElem,ownerDoc,dsPrefix);
	    } else if (object instanceof XMLStructure) {
	        if (object instanceof X509IssuerSerial) {
		    ((DOMX509IssuerSerial) object).marshal
			(xdElem, dsPrefix, context);
		} else {
		    javax.xml.crypto.dom.DOMStructure domContent =
			(javax.xml.crypto.dom.DOMStructure) object;
		    DOMUtils.appendChild(xdElem, domContent.getNode());
		}
	    } else if (object instanceof byte[]) {
                marshalSKI((byte[]) object, xdElem, ownerDoc, dsPrefix);
            } else if (object instanceof String) {
                marshalSubjectName((String) object, xdElem, ownerDoc,dsPrefix);
            } else if (object instanceof X509CRL) {
                marshalCRL((X509CRL) object, xdElem, ownerDoc, dsPrefix);
            }
        }

        parent.appendChild(xdElem);
    }

    private void marshalSKI(byte[] skid, Node parent, Document doc, 
	String dsPrefix) {

        Element skidElem = DOMUtils.createElement
	    (doc, "X509SKI", XMLSignature.XMLNS, dsPrefix);
        skidElem.appendChild(doc.createTextNode(Base64.encode(skid)));
        parent.appendChild(skidElem);
    }

    private void marshalSubjectName(String name, Node parent, Document doc, 
	String dsPrefix) {

        Element snElem = DOMUtils.createElement
	    (doc, "X509SubjectName", XMLSignature.XMLNS, dsPrefix);
        snElem.appendChild(doc.createTextNode(name));
        parent.appendChild(snElem);
    }

    private void marshalCert(X509Certificate cert, Node parent, Document doc,
	String dsPrefix) throws MarshalException {

        Element certElem = DOMUtils.createElement
	    (doc, "X509Certificate", XMLSignature.XMLNS, dsPrefix);
        try {
            certElem.appendChild(doc.createTextNode
                (Base64.encode(cert.getEncoded())));
        } catch (CertificateEncodingException e) {
            throw new MarshalException("Error encoding X509Certificate", e);
        }
        parent.appendChild(certElem);
    }

    private void marshalCRL(X509CRL crl, Node parent, Document doc, 
	String dsPrefix) throws MarshalException {

        Element crlElem = DOMUtils.createElement
	    (doc, "X509CRL", XMLSignature.XMLNS, dsPrefix);
        try {
            crlElem.appendChild(doc.createTextNode
                (Base64.encode(crl.getEncoded())));
        } catch (CRLException e) {
            throw new MarshalException("Error encoding X509CRL", e);
        }
        parent.appendChild(crlElem);
    }

    private X509Certificate unmarshalX509Certificate(Element elem) 
	throws MarshalException {
        try {
            ByteArrayInputStream bs = unmarshalBase64Binary(elem);
            return (X509Certificate) cf.generateCertificate(bs);
        } catch (CertificateException e) {
            throw new MarshalException("Cannot create X509Certificate", e);
        }
    }

    private X509CRL unmarshalX509CRL(Element elem) throws MarshalException {
        try {
            ByteArrayInputStream bs = unmarshalBase64Binary(elem);
            return (X509CRL) cf.generateCRL(bs);
        } catch (CRLException e) {
            throw new MarshalException("Cannot create X509CRL", e);
        }
    }

    private ByteArrayInputStream unmarshalBase64Binary(Element elem) 
	throws MarshalException {
        try {
            if (cf == null) {
                cf = CertificateFactory.getInstance("X.509");
	    }
            return new ByteArrayInputStream(Base64.decode(elem));
        } catch (CertificateException e) {
            throw new MarshalException("Cannot create CertificateFactory", e);
        } catch (Base64DecodingException bde) {
            throw new MarshalException("Cannot decode Base64-encoded val", bde);
        }
    }

    public boolean equals(Object o) {
	if (this == o) {
            return true;
	}

        if (!(o instanceof X509Data)) {
            return false;
	}
        X509Data oxd = (X509Data) o;

	List ocontent = oxd.getContent();
	int size = content.size();
	if (size != ocontent.size()) {
	    return false;
	}

	for (int i = 0; i < size; i++) {
	    Object x = content.get(i);
	    Object ox = ocontent.get(i);
	    if (x instanceof byte[]) {
		if (!(ox instanceof byte[]) || 
		    !Arrays.equals((byte[]) x, (byte[]) ox)) {
		    return false;
		} 
	    } else {
		if (!(x.equals(ox))) {
		    return false;
		}
	    }
	}

	return true;
    }
}
