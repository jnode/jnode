/* StreamSerializer.java -- 
   Copyright (C) 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.xml.transform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Serializes a DOM node to an output stream.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class StreamSerializer
{
  
  static final int SPACE = 0x20;
  static final int BANG = 0x21; // !
  static final int APOS = 0x27; // '
  static final int SLASH = 0x2f; // /
  static final int BRA = 0x3c; // <
  static final int KET = 0x3e; // >
  static final int EQ = 0x3d; // =

  protected String encoding;
  boolean compatibilityMode;
  final int mode;
  final Map namespaces;
  protected String eol;
  Collection cdataSectionElements = Collections.EMPTY_SET;

  protected boolean discardDefaultContent;
  protected boolean xmlDeclaration = true;

  public StreamSerializer()
  {
    this(Stylesheet.OUTPUT_XML, null, null);
  }

  public StreamSerializer(String encoding)
  {
    this(Stylesheet.OUTPUT_XML, encoding, null);
  }

  public StreamSerializer(int mode, String encoding, String eol)
  {
    this.mode = mode;
    if (encoding == null)
      {
        encoding = "UTF-8";
      }
    this.encoding = encoding.intern();
    compatibilityMode = true;
    if (encoding.length() > 3)
      {
        String p = encoding.substring(0, 3);
        if (p.equalsIgnoreCase("UTF") ||
            p.equalsIgnoreCase("UCS"))
          {
            compatibilityMode = false;
          }
      }
    this.eol = (eol != null) ? eol : System.getProperty("line.separator");
    namespaces = new HashMap();
  }

  void setCdataSectionElements(Collection c)
  {
    cdataSectionElements = c;
  }

  public void serialize(final Node node, final OutputStream out)
    throws IOException
  {
    serialize(node, out, false);
  }
  
  void serialize(final Node node, final OutputStream out,
                 boolean convertToCdata)
    throws IOException
  {
    if (out == null)
      {
        throw new NullPointerException("no output stream");
      }
    String value, prefix;
    Node children;
    Node next = node.getNextSibling();
    String uri = node.getNamespaceURI();
    boolean defined = false;
    short nt = node.getNodeType();
    if (convertToCdata && nt == Node.TEXT_NODE)
      {
        nt = Node.CDATA_SECTION_NODE;
      }
    switch (nt)
      {
      case Node.ATTRIBUTE_NODE:
        prefix = node.getPrefix();
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri) ||
            XMLConstants.XMLNS_ATTRIBUTE.equals(prefix) ||
            (prefix != null && prefix.startsWith("xmlns:")))
          {
            String nsuri = node.getNodeValue();
            if (isDefined(nsuri))
              {
                break;
              }
            define(nsuri, node.getLocalName());
          }
        else if (uri != null && !isDefined(uri))
          {
            prefix = define(uri, prefix);
            String nsname = (prefix == null) ? "xmlns" : "xmlns:" + prefix;
            out.write(SPACE);
            out.write(encodeText(nsname));
            out.write(EQ);
            String nsvalue = "'" + encode(uri, true, true) + "'";
            out.write(nsvalue.getBytes(encoding));
            defined = true;
          }
        out.write(SPACE);
        String a_nodeName = node.getNodeName();
        out.write(encodeText(a_nodeName));
        String a_nodeValue = node.getNodeValue();
        if (mode == Stylesheet.OUTPUT_HTML &&
            a_nodeName.equals(a_nodeValue))
          {
            break;
          }
        out.write(EQ);
        value = "'" + encode(a_nodeValue, true, true) + "'";
        out.write(encodeText(value));
        break;
      case Node.ELEMENT_NODE:
        value = node.getNodeName();
        out.write(BRA);
        out.write(encodeText(value));
        if (uri != null && !isDefined(uri))
          {
            prefix = define(uri, node.getPrefix());
            String nsname = (prefix == null) ? "xmlns" : "xmlns:" + prefix;
            out.write(SPACE);
            out.write(encodeText(nsname));
            out.write(EQ);
            String nsvalue = "'" + encode(uri, true, true) + "'";
            out.write(encodeText(nsvalue));
            defined = true;
          }
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null)
          {
            int len = attrs.getLength();
            for (int i = 0; i < len; i++)
              {
                Attr attr = (Attr) attrs.item(i);
                if (discardDefaultContent && !attr.getSpecified())
                  {
                    // NOOP
                  }
                else
                  {
                    serialize(attr, out, false);
                  }
              }
          }
        convertToCdata = cdataSectionElements.contains(value);
        children = node.getFirstChild();
        if (children == null)
          {
            out.write(SLASH);
            out.write(KET);
          }
        else
          {
            out.write(KET);
            serialize(children, out, convertToCdata);
            out.write(BRA);
            out.write(SLASH);
            out.write(encodeText(value));
            out.write(KET);
          }
        break;
      case Node.TEXT_NODE:
        value = node.getNodeValue();
        if (!"yes".equals(node.getUserData("disable-output-escaping")))
          {
            value = encode(value, false, false);
          }
        out.write(encodeText(value));
        break;
      case Node.CDATA_SECTION_NODE:
        value = "<![CDATA[" + node.getNodeValue() + "]]>";
        out.write(encodeText(value));
        break;
      case Node.COMMENT_NODE:
        value = "<!--" + node.getNodeValue() + "-->";
        out.write(encodeText(value));
        Node cp = node.getParentNode();
        if (cp != null && cp.getNodeType() == Node.DOCUMENT_NODE)
          {
            out.write(encodeText(eol));
          }
        break;
      case Node.DOCUMENT_NODE:
      case Node.DOCUMENT_FRAGMENT_NODE:
        if (mode == Stylesheet.OUTPUT_XML)
          {
            if ("UTF-16".equalsIgnoreCase(encoding))
              {
                out.write(0xfe);
                out.write(0xff);
              }
            if (!"yes".equals(node.getUserData("omit-xml-declaration")) &&
                xmlDeclaration)
              {
                Document doc = (node instanceof Document) ?
                  (Document) node : null;
                String version = (doc != null) ? doc.getXmlVersion() : null;
                if (version == null)
                  {
                    version = (String) node.getUserData("version");
                  }
                if (version == null)
                  {
                    version = "1.0";
                  }
                out.write(BRA);
                out.write(0x3f);
                out.write("xml version='".getBytes("US-ASCII"));
                out.write(version.getBytes("US-ASCII"));
                out.write(APOS);
                if (!("UTF-8".equalsIgnoreCase(encoding)))
                  {
                    out.write(" encoding='".getBytes("US-ASCII"));
                    out.write(encoding.getBytes("US-ASCII"));
                    out.write(APOS);
                  }
                if ((doc != null && doc.getXmlStandalone()) ||
                    "yes".equals(node.getUserData("standalone")))
                  {
                    out.write(" standalone='yes'".getBytes("US-ASCII"));
                  }
                out.write(0x3f);
                out.write(KET);
                out.write(encodeText(eol));
              }
            // TODO warn if not outputting the declaration would be a
            // problem
          }
        else if (mode == Stylesheet.OUTPUT_HTML)
          {
            // Ensure that encoding is accessible
            String mediaType = (String) node.getUserData("media-type");
            if (mediaType == null)
              {
                mediaType = "text/html";
              }
            String contentType = mediaType + "; charset=" +
              ((encoding.indexOf(' ') != -1) ?
                "\"" + encoding + "\"" :
                encoding);
            Document doc = (node instanceof Document) ? (Document) node :
              node.getOwnerDocument();
            Node html = null;
            for (Node ctx = node.getFirstChild(); ctx != null;
                 ctx = ctx.getNextSibling())
              {
                if (ctx.getNodeType() == Node.ELEMENT_NODE)
                  {
                    html = ctx;
                    break;
                  }
              }
            if (html == null)
              {
                html = doc.createElement("html");
                node.appendChild(html);
              }
            Node head = null;
            for (Node ctx = html.getFirstChild(); ctx != null;
                 ctx = ctx.getNextSibling())
              {
                if (ctx.getNodeType() == Node.ELEMENT_NODE &&
                    "head".equalsIgnoreCase(ctx.getLocalName()))
                  {
                    head = ctx;
                    break;
                  }
              }
            if (head == null)
              {
                head = doc.createElement("head");
                Node c1 = null;
                for (Node ctx = html.getFirstChild(); ctx != null;
                     ctx = ctx.getNextSibling())
                  {
                    if (ctx.getNodeType() == Node.ELEMENT_NODE)
                      {
                        c1 = ctx;
                        break;
                      }
                  }
                if (c1 != null)
                  {
                    html.insertBefore(head, c1);
                  }
                else
                  {
                    html.appendChild(head);
                  }
              }
            Node meta = null;
            Node metaContent = null;
            for (Node ctx = head.getFirstChild(); ctx != null;
                 ctx = ctx.getNextSibling())
              {
                if (ctx.getNodeType() == Node.ELEMENT_NODE &&
                    "meta".equalsIgnoreCase(ctx.getLocalName()))
                  {
                    NamedNodeMap metaAttrs = ctx.getAttributes();
                    int len = metaAttrs.getLength();
                    String httpEquiv = null;
                    Node content = null;
                    for (int i = 0; i < len; i++)
                      {
                        Node attr = metaAttrs.item(i);
                        String attrName = attr.getNodeName();
                        if ("http-equiv".equalsIgnoreCase(attrName))
                          {
                            httpEquiv = attr.getNodeValue();
                          }
                        else if ("content".equalsIgnoreCase(attrName))
                          {
                            content = attr;
                          }
                      }
                    if ("Content-Type".equalsIgnoreCase(httpEquiv))
                      {
                        meta = ctx;
                        metaContent = content;
                        break;
                      }
                  }
              }
            if (meta == null)
              {
                meta = doc.createElement("meta");
                // Insert first
                Node first = head.getFirstChild();
                if (first == null)
                  {
                    head.appendChild(meta);
                  }
                else
                  {
                    head.insertBefore(meta, first);
                  }
                Node metaHttpEquiv = doc.createAttribute("http-equiv");
                meta.getAttributes().setNamedItem(metaHttpEquiv);
                metaHttpEquiv.setNodeValue("Content-Type");
              }
            if (metaContent == null)
              {
                metaContent = doc.createAttribute("content");
                meta.getAttributes().setNamedItem(metaContent);
              }
            metaContent.setNodeValue(contentType);
            // phew
          }
        children = node.getFirstChild();
        if (children != null)
          {
            serialize(children, out, convertToCdata);
          }
        break;
      case Node.DOCUMENT_TYPE_NODE:
        DocumentType doctype = (DocumentType) node;
        out.write(BRA);
        out.write(BANG);
        value = doctype.getNodeName();
        out.write(encodeText(value));
        String publicId = doctype.getPublicId();
        if (publicId != null)
          {
            out.write(encodeText(" PUBLIC "));
            out.write(APOS);
            out.write(encodeText(publicId));
            out.write(APOS);
          }
        String systemId = doctype.getSystemId();
        if (systemId != null)
          {
            out.write(encodeText(" SYSTEM "));
            out.write(APOS);
            out.write(encodeText(systemId));
            out.write(APOS);
          }
        String internalSubset = doctype.getInternalSubset();
        if (internalSubset != null)
          {
            out.write(encodeText(internalSubset));
          }
        out.write(KET);
        out.write(eol.getBytes(encoding));
        break;
      case Node.ENTITY_REFERENCE_NODE:
        value = "&" + node.getNodeValue() + ";";
        out.write(encodeText(value));
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        value = "<?" + node.getNodeName() + " " + node.getNodeValue() + "?>";
        out.write(encodeText(value));
        Node pp = node.getParentNode();
        if (pp != null && pp.getNodeType() == Node.DOCUMENT_NODE)
          {
            out.write(encodeText(eol));
          }
        break;
      }
    if (defined)
      {
        undefine(uri);
      }
    if (next != null)
      {
        serialize(next, out, convertToCdata);
      }
  }

  boolean isDefined(String uri)
  {
    return XMLConstants.XML_NS_URI.equals(uri) ||
      XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri) ||
      namespaces.containsKey(uri);
  }

  String define(String uri, String prefix)
  {
    while (namespaces.containsValue(prefix))
      {
        // Fabricate new prefix
        prefix = prefix + "_";
      }
    namespaces.put(uri, prefix);
    return prefix;
  }

  void undefine(String uri)
  {
    namespaces.remove(uri);
  }

  final byte[] encodeText(String text)
    throws UnsupportedEncodingException
  {
    if (compatibilityMode)
      {
        int len = text.length();
        StringBuffer buf = null;
        for (int i = 0; i < len; i++)
          {
            char c = text.charAt(i);
            if (c >= 127)
              {
                if (buf == null)
                  {
                    buf = new StringBuffer(text.substring(0, i));
                  }
                buf.append('&');
                buf.append('#');
                buf.append((int) c);
                buf.append(';');
              }
            else if (buf != null)
              {
                buf.append(c);
              }
          }
        if (buf != null)
          {
            text = buf.toString();
          }
      }
    return text.getBytes(encoding);
  }

  String encode(String text, boolean encodeCtl, boolean inAttr)
  {
    int len = text.length();
    StringBuffer buf = null;
    for (int i = 0; i < len; i++)
      {
        char c = text.charAt(i);
        if (c == '<')
          {
            if (buf == null)
              {
                buf = new StringBuffer(text.substring(0, i));
              }
            buf.append("&lt;");
          }
        else if (c == '>')
          {
            if (buf == null)
              {
                buf = new StringBuffer(text.substring(0, i));
              }
            buf.append("&gt;");
          }
        else if (c == '&')
          {
            if (mode == Stylesheet.OUTPUT_HTML && (i + 1) < len &&
                text.charAt(i + 1) == '{')
              {
                if (buf != null)
                  {
                    buf.append(c);
                  }
              }
            else
              {
                if (buf == null)
                  {
                    buf = new StringBuffer(text.substring(0, i));
                  }
                buf.append("&amp;");
              }
          }
        else if (c == '\'' && inAttr)
          {
            if (buf == null)
              {
                buf = new StringBuffer(text.substring(0, i));
              }
            buf.append("&apos;");
          }
        else if (c == '"' && inAttr)
          {
            if (buf == null)
              {
                buf = new StringBuffer(text.substring(0, i));
              }
            buf.append("&quot;");
          }
        else if (encodeCtl)
          {
            if (c < 0x20)
              {
                if (buf == null)
                  {
                    buf = new StringBuffer(text.substring(0, i));
                  }
                buf.append('&');
                buf.append('#');
                buf.append((int) c);
                buf.append(';');
              }
            else if (buf != null)
              {
                buf.append(c);
              }
          }
        else if (buf != null)
          {
            buf.append(c);
          }
      }
    return (buf == null) ? text : buf.toString();
  }

  String toString(Node node)
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try
      {
        serialize(node, out);
        return new String(out.toByteArray(), encoding);
      }
    catch (IOException e)
      {
        throw new RuntimeException(e.getMessage());
      }
  }
  
}
