/* Stylesheet.java -- 
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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import gnu.xml.xpath.Expr;
import gnu.xml.xpath.NameTest;
import gnu.xml.xpath.NodeTypeTest;
import gnu.xml.xpath.Pattern;
import gnu.xml.xpath.Selector;
import gnu.xml.xpath.Root;
import gnu.xml.xpath.Test;
import gnu.xml.xpath.XPathImpl;

/**
 * An XSL stylesheet.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class Stylesheet
  implements NamespaceContext, XPathFunctionResolver, UserDataHandler, Cloneable
{

  static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
  
  static final int OUTPUT_XML = 0;
  static final int OUTPUT_HTML = 1;
  static final int OUTPUT_TEXT = 2;

  final TransformerFactoryImpl factory;
  TransformerImpl transformer;
  Stylesheet parent;
  final XPathImpl xpath;
  final String systemId;
  final int precedence;

  final boolean debug;

  /**
   * Version of XSLT.
   */
  String version;

  Collection extensionElementPrefixes;
  Collection excludeResultPrefixes;

  /**
   * Set of element names for which we should strip whitespace.
   */
  Set stripSpace;

  /**
   * Set of element names for which we should preserve whitespace.
   */
  Set preserveSpace;

  /**
   * Output options.
   */
  Node output;
  int outputMethod;
  String outputVersion;
  String outputEncoding;
  boolean outputOmitXmlDeclaration;
  boolean outputStandalone;
  String outputPublicId;
  String outputSystemId;
  Collection outputCdataSectionElements;
  boolean outputIndent;
  String outputMediaType;

  /**
   * Keys.
   */
  Collection keys;

  /**
   * Decimal formats.
   */
  Map decimalFormats;
  
  /**
   * Namespace aliases.
   */
  Map namespaceAliases;

  /**
   * Attribute-sets.
   */
  List attributeSets;

  /**
   * Variables.
   */
  List variables;

  /**
   * Variable and parameter bindings.
   */
  Bindings bindings;

  /**
   * Templates.
   */
  LinkedList templates;

  TemplateNode builtInNodeTemplate;
  TemplateNode builtInTextTemplate;

  /**
   * Holds the current node while parsing.
   * Necessary to associate the document function with its declaring node,
   * to resolve namespaces, and to maintain the current node for the
   * current() function.
   */
  Node current;

  /**
   * Set by a terminating message.
   */
  transient boolean terminated;

  /**
   * Current template in force.
   */
  transient Template currentTemplate;

  Stylesheet(TransformerFactoryImpl factory,
             Stylesheet parent,
             Document doc,
             String systemId,
             int precedence)
    throws TransformerConfigurationException
  {
    this.factory = factory;
    this.systemId = systemId;
    this.precedence = precedence;
    this.parent = parent;
    extensionElementPrefixes = new HashSet();
    excludeResultPrefixes = new HashSet();
    stripSpace = new LinkedHashSet();
    preserveSpace = new LinkedHashSet();
    outputCdataSectionElements = new LinkedHashSet();
    xpath = (XPathImpl) factory.xpathFactory.newXPath();
    if (parent == null)
      {
        bindings = new Bindings(this);
        attributeSets = new LinkedList();
        variables = new LinkedList();
        namespaceAliases = new LinkedHashMap();
        templates = new LinkedList();
        keys = new LinkedList();
        decimalFormats = new LinkedHashMap();
        initDefaultDecimalFormat();
        xpath.setNamespaceContext(this);
        xpath.setXPathFunctionResolver(this);
      }
    else
      {
        /* Test for import circularity */
        for (Stylesheet ctx = this; ctx.parent != null; ctx = ctx.parent)
          {
            if (systemId != null && systemId.equals(ctx.parent.systemId))
              {
                String msg = "circularity importing " + systemId;
                throw new TransformerConfigurationException(msg);
              }
          }
        /* OK */
        Stylesheet root = getRootStylesheet();
        bindings = root.bindings;
        attributeSets = root.attributeSets;
        variables = root.variables;
        namespaceAliases = root.namespaceAliases;
        templates = root.templates;
        keys = root.keys;
        decimalFormats = root.decimalFormats;
        xpath.setNamespaceContext(root);
        xpath.setXPathFunctionResolver(root);
      }
    xpath.setXPathVariableResolver(bindings);

    Test anyNode = new NodeTypeTest((short) 0);
    List tests = Collections.singletonList(anyNode);
    builtInNodeTemplate =
      new ApplyTemplatesNode(new Selector(Selector.CHILD, tests),
                             null, null, null, true);
    builtInTextTemplate =
      new ValueOfNode(new Selector(Selector.SELF, tests),
                      false);
    
    parse(doc.getDocumentElement(), true);
    current = doc; // Alow namespace resolution during processing
    
    debug = ("yes".equals(System.getProperty("xsl.debug")));

    if (debug)
      {
        System.err.println("Stylesheet: " + doc.getDocumentURI());
        for (Iterator i = templates.iterator(); i.hasNext(); )
          {
            Template t = (Template) i.next();
            t.list(System.err);
            System.err.println("--------------------");
          }
      }
  }

  Stylesheet getRootStylesheet()
  {
    Stylesheet stylesheet = this;
    while (stylesheet.parent != null)
      {
        stylesheet = stylesheet.parent;
      }
    return stylesheet;
  }
  
  void initDefaultDecimalFormat()
  {
    DecimalFormat defaultDecimalFormat = new DecimalFormat();
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    symbols.setGroupingSeparator(',');
    symbols.setPercent('%');
    symbols.setPerMill('\u2030');
    symbols.setZeroDigit('0');
    symbols.setDigit('#');
    symbols.setPatternSeparator(';');
    symbols.setInfinity("Infinity");
    symbols.setNaN("NaN");
    symbols.setMinusSign('-');
    defaultDecimalFormat.setDecimalFormatSymbols(symbols);
    decimalFormats.put(null, defaultDecimalFormat);
  }
  
  // -- Cloneable --

  public Object clone()
  {
    try
      {
        Stylesheet clone = (Stylesheet) super.clone();
        clone.bindings = (Bindings) bindings.clone();

        LinkedList templates2 = new LinkedList();
        for (Iterator i = templates.iterator(); i.hasNext(); )
          {
            Template t = (Template) i.next();
            templates2.add(t.clone(clone));
          }
        clone.templates = templates2;

        LinkedList attributeSets2 = new LinkedList();
        for (Iterator i = attributeSets.iterator(); i.hasNext(); )
          {
            AttributeSet as = (AttributeSet) i.next();
            attributeSets2.add(as.clone(clone));
          }
        clone.attributeSets = attributeSets2;

        LinkedList variables2 = new LinkedList();
        for (Iterator i = variables.iterator(); i.hasNext(); )
          {
            ParameterNode var = (ParameterNode) i.next();
            variables2.add(var.clone(clone));
          }
        clone.variables = variables2;

        LinkedList keys2 = new LinkedList();
        for (Iterator i = keys.iterator(); i.hasNext(); )
          {
            Key k = (Key) i.next();
            keys2.add(k.clone(clone));
          }
        clone.keys = keys2;
        
        return clone;
      }
    catch (CloneNotSupportedException e)
      {
        throw new Error(e.getMessage());
      }
  }

  // -- Variable evaluation --

  void initTopLevelVariables(Node context)
    throws TransformerException
  {
    for (Iterator i = variables.iterator(); i.hasNext(); )
      {
        ParameterNode var = (ParameterNode) i.next();
        bindings.set(var.name,
                     var.getValue(this, null, context, 1, 1),
                     var.global);
      }
  }

  // -- NamespaceContext --

  public String getNamespaceURI(String prefix)
  {
    return (current == null) ? null : current.lookupNamespaceURI(prefix);
  }

  public String getPrefix(String namespaceURI)
  {
    return (current == null) ? null : current.lookupPrefix(namespaceURI);
  }

  public Iterator getPrefixes(String namespaceURI)
  {
    // TODO
    return Collections.singleton(getPrefix(namespaceURI)).iterator();
  }
  
  // -- Template selection --
  
  TemplateNode getTemplate(QName mode, Node context, boolean applyImports)
    throws TransformerException
  {
    if (debug)
      {
        System.err.println("getTemplate: mode="+mode+" context="+context);
      }
    Set candidates = new TreeSet();
    for (Iterator j = templates.iterator(); j.hasNext(); )
      {
        Template t = (Template) j.next();
        boolean isMatch = t.matches(mode, context);
        if (applyImports)
          {
            if (currentTemplate == null)
              {
                String msg = "current template may not be null " +
                  "during apply-imports";
                throw new TransformerException(msg);
              }
            if (!currentTemplate.imports(t))
              {
                isMatch = false;
              }
          }
        //System.err.println("\t"+context+" "+t+"="+isMatch);
        if (isMatch)
          {
            candidates.add(t);
          }
      }
    //System.err.println("\tcandidates="+candidates);
    if (candidates.isEmpty())
      {
        // Apply built-in template
        // Current template is unchanged
        if (debug)
          {
            System.err.println("\tbuiltInTemplate context="+context);
          }
        switch (context.getNodeType())
          {
          case Node.ELEMENT_NODE:
          case Node.DOCUMENT_NODE:
          case Node.DOCUMENT_FRAGMENT_NODE:
          case Node.PROCESSING_INSTRUCTION_NODE:
          case Node.COMMENT_NODE:
            return builtInNodeTemplate;
          case Node.TEXT_NODE:
          case Node.ATTRIBUTE_NODE:
            return builtInTextTemplate;
          default:
            return null;
          }
      }
    else
      {
        Template t = (Template) candidates.iterator().next();
        // Set current template
        currentTemplate = t;
        if (debug)
          {
            System.err.println("\ttemplate="+t+" context="+context);
          }
        return t.node;
      }
  }

  TemplateNode getTemplate(QName mode, QName name)
    throws TransformerException
  {
    //System.err.println("getTemplate: mode="+mode+" name="+name);
    Set candidates = new TreeSet();
    for (Iterator j = templates.iterator(); j.hasNext(); )
      {
        Template t = (Template) j.next();
        boolean isMatch = t.matches(name);
        //System.err.println("\t"+name+" "+t+"="+isMatch);
        if (isMatch)
          {
            candidates.add(t);
          }
      }
    if (candidates.isEmpty())
      {
        return null;
        //throw new TransformerException("template '" + name + "' not found");
      }
    Template t = (Template) candidates.iterator().next();
    //System.err.println("\ttemplate="+t+" context="+context);
    return t.node;
  }

  /**
   * template
   */
  final Template parseTemplate(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException, XPathExpressionException
  {
    String n = getAttribute(attrs, "name");
    QName name = (n == null) ? null : getQName(n);
    String m = getAttribute(attrs, "match");
    Pattern match = null;
    if (m != null)
      {
        try
          {
            match = (Pattern) xpath.compile(m);
          }
        catch (ClassCastException e)
          {
            String msg = "illegal pattern: " + m;
            throw new TransformerConfigurationException(msg);
          }
      }
    String p = getAttribute(attrs, "priority");
    String mm = getAttribute(attrs, "mode");
    QName mode = (mm == null) ? null : getQName(mm);
    double priority = (p == null) ? Template.DEFAULT_PRIORITY :
      Double.parseDouble(p);
    Node children = node.getFirstChild();
    return new Template(this, name, match, parse(children),
                        precedence, priority, mode);
  }

  /**
   * output
   */
  final void parseOutput(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException
  {
    output = node;
    String method = getAttribute(attrs, "method");
    if ("xml".equals(method) || method == null)
      {
        outputMethod = OUTPUT_XML;
      }
    else if ("html".equals(method))
      {
        outputMethod = OUTPUT_HTML;
      }
    else if ("text".equals(method))
      {
        outputMethod = OUTPUT_TEXT;
      }
    else
      {
        String msg = "unsupported output method: " + method;
        DOMSourceLocator l = new DOMSourceLocator(node);
        throw new TransformerConfigurationException(msg, l);
      }
    outputPublicId = getAttribute(attrs, "public-id");
    outputSystemId = getAttribute(attrs, "system-id");
    outputEncoding = getAttribute(attrs, "encoding");
    String indent = getAttribute(attrs, "indent");
    if (indent != null)
      {
        outputIndent = "yes".equals(indent);
      }
    outputVersion = getAttribute(attrs, "version");
    String omitXmlDecl = getAttribute(attrs, "omit-xml-declaration");
    if (omitXmlDecl != null)
      {
        outputOmitXmlDeclaration = "yes".equals(omitXmlDecl);
      }
    String standalone = getAttribute(attrs, "standalone");
    if (standalone != null)
      {
        outputStandalone = "yes".equals(standalone);
      }
    outputMediaType = getAttribute(attrs, "media-type");
    String cdataSectionElements =
      getAttribute(attrs, "cdata-section-elements");
    if (cdataSectionElements != null)
      {
        StringTokenizer st = new StringTokenizer(cdataSectionElements, " ");
        while (st.hasMoreTokens())
          {
            outputCdataSectionElements.add(st.nextToken());
          }
      }
  }

  /**
   * key
   */
  final void parseKey(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException, XPathExpressionException
  {
    String n = getRequiredAttribute(attrs, "name", node);
    String m = getRequiredAttribute(attrs, "match", node);
    String u = getRequiredAttribute(attrs, "use", node);
    QName name = getQName(n);
    Expr use = (Expr) xpath.compile(u);
    try
      {
        Pattern match = (Pattern) xpath.compile(m);
        Key key = new Key(name, match, use);
        keys.add(key);
      }
    catch (ClassCastException e)
      {
        throw new TransformerConfigurationException("invalid pattern: " + m);
      }
  }

  /**
   * decimal-format
   */
  final void parseDecimalFormat(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException
  {
    String dfName = getAttribute(attrs, "name");
    DecimalFormat df = new DecimalFormat();
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator(parseDFChar(attrs, "decimal-separator", '.'));
    symbols.setGroupingSeparator(parseDFChar(attrs, "grouping-separator", ','));
    symbols.setInfinity(parseDFString(attrs, "infinity", "Infinity"));
    symbols.setMinusSign(parseDFChar(attrs, "minus-sign", '-'));
    symbols.setNaN(parseDFString(attrs, "NaN", "NaN"));
    symbols.setPercent(parseDFChar(attrs, "percent", '%'));
    symbols.setPerMill(parseDFChar(attrs, "per-mille", '\u2030'));
    symbols.setZeroDigit(parseDFChar(attrs, "zero-digit", '0'));
    symbols.setDigit(parseDFChar(attrs, "digit", '#'));
    symbols.setPatternSeparator(parseDFChar(attrs, "pattern-separator", ';'));
    df.setDecimalFormatSymbols(symbols);
    decimalFormats.put(dfName, df);
  }

  private final char parseDFChar(NamedNodeMap attrs, String name, char def)
    throws TransformerConfigurationException
  {
    Node attr = attrs.getNamedItem(name);
    try
      {
        return (attr == null) ? def : attr.getNodeValue().charAt(0);
      }
    catch (StringIndexOutOfBoundsException e)
      {
        throw new TransformerConfigurationException("empty attribute '" +
                                                    name +
                                                    "' in decimal-format", e);
      }
  }

  private final String parseDFString(NamedNodeMap attrs, String name,
                                     String def)
  {
    Node attr = attrs.getNamedItem(name);
    return (attr == null) ? def : attr.getNodeValue();
  }
  
  /**
   * namespace-alias
   */
  final void parseNamespaceAlias(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException
  {
    String sp = getRequiredAttribute(attrs, "stylesheet-prefix", node);
    String rp = getRequiredAttribute(attrs, "result-prefix", node);
    namespaceAliases.put(sp, rp);
  }

  /**
   * attribute-set
   */
  final void parseAttributeSet(Node node, NamedNodeMap attrs)
    throws TransformerConfigurationException, XPathExpressionException
  {
    TemplateNode children = parse(node.getFirstChild());
    String name = getRequiredAttribute(attrs, "name", node);
    String uas = getAttribute(attrs, "use-attribute-sets");
    attributeSets.add(new AttributeSet(children, name, uas));
  }

  /**
   * Parse top-level elements.
   */
  void parse(Node node, boolean root)
    throws TransformerConfigurationException
  {
    while (node != null)
      {
    current = node;
        doParse(node, root);
        node = node.getNextSibling();
      }
  }

  void doParse(Node node, boolean root)
    throws TransformerConfigurationException
  {
    try
      {
        String namespaceUri = node.getNamespaceURI();
        if (XSL_NS.equals(namespaceUri) &&
            node.getNodeType() == Node.ELEMENT_NODE)
          {
            String name = node.getLocalName();
            NamedNodeMap attrs = node.getAttributes();
            if ("stylesheet".equals(name))
              {
                version = getAttribute(attrs, "version");
                String eep = getAttribute(attrs, "extension-element-prefixes");
                if (eep != null)
                  {
                    StringTokenizer st = new StringTokenizer(eep);
                    while (st.hasMoreTokens())
                      {
                        extensionElementPrefixes.add(st.nextToken());
                      }
                  }
                String erp = getAttribute(attrs, "exclude-result-prefixes");
                if (erp != null)
                  {
                    StringTokenizer st = new StringTokenizer(erp);
                    while (st.hasMoreTokens())
                      {
                        excludeResultPrefixes.add(st.nextToken());
                      }
                  }
                parse(node.getFirstChild(), false);
              }
            else if ("template".equals(name))
              {
                templates.add(parseTemplate(node, attrs));
              }
            else if ("param".equals(name) ||
                     "variable".equals(name))
              {
                boolean global = "variable".equals(name);
                TemplateNode content = parse(node.getFirstChild());
                String paramName = getRequiredAttribute(attrs, "name", node);
                String select = getAttribute(attrs, "select");
                ParameterNode param;
                if (select != null && select.length() > 0)
                  {
                    if (content != null)
                      {
                        String msg = "parameter '" + paramName +
                          "' has both select and content";
                        DOMSourceLocator l = new DOMSourceLocator(node);
                        throw new TransformerConfigurationException(msg, l);
                      }
                    Expr expr = (Expr) xpath.compile(select);
                    param = new ParameterNode(paramName, expr, global);
                  }
                else
                  {
                    param = new ParameterNode(paramName, null, global);
                    param.children = content;
                  }
                variables.add(param);
                bindings.set(paramName, content, global);
              }
            else if ("include".equals(name) || "import".equals(name))
              {
                int delta = "import".equals(name) ? -1 : 0;
                String href = getRequiredAttribute(attrs, "href", node);
                Source source;
                synchronized (factory.resolver)
                  {
                    if (transformer != null)
                      {
                        factory.resolver
                          .setUserResolver(transformer.getURIResolver());
                        factory.resolver
                          .setUserListener(transformer.getErrorListener());
                      }
                    source = factory.resolver.resolve(systemId, href);
                  }
                factory.newStylesheet(source, precedence + delta, this);
              }
            else if ("output".equals(name))
              {
                parseOutput(node, attrs);
              }
            else if ("preserve-space".equals(name))
              {
                String elements =
                  getRequiredAttribute(attrs, "elements", node);
                StringTokenizer st = new StringTokenizer(elements,
                                                         " \t\n\r");
                while (st.hasMoreTokens())
                  {
                    preserveSpace.add(parseNameTest(st.nextToken()));
                  }
              }
            else if ("strip-space".equals(name))
              {
                String elements =
                  getRequiredAttribute(attrs, "elements", node);
                StringTokenizer st = new StringTokenizer(elements,
                                                         " \t\n\r");
                while (st.hasMoreTokens())
                  {
                    stripSpace.add(parseNameTest(st.nextToken()));
                  }
              }
            else if ("key".equals(name))
              {
                parseKey(node, attrs);
              }
            else if ("decimal-format".equals(name))
              {
                parseDecimalFormat(node, attrs);
              }
            else if ("namespace-alias".equals(name))
              {
                parseNamespaceAlias(node, attrs);
              }
            else if ("attribute-set".equals(name))
              {
                parseAttributeSet(node, attrs);
              }
          }
        else if (root)
          {
            // Literal document element
            Attr versionNode =
              ((Element)node).getAttributeNodeNS(XSL_NS, "version");
            if (versionNode == null)
              {
                String msg = "no xsl:version attribute on literal result node";
                DOMSourceLocator l = new DOMSourceLocator(node);
                throw new TransformerConfigurationException(msg, l);
              }
            version = versionNode.getValue();
            Node rootClone = node.cloneNode(true);
            NamedNodeMap attrs = rootClone.getAttributes();
            attrs.removeNamedItemNS(XSL_NS, "version");
            templates.add(new Template(this, null, new Root(),
                                       parse(rootClone),
                                       precedence,
                                       Template.DEFAULT_PRIORITY,
                                       null));
          }
        else
          {
            // Skip unknown elements, text, comments, etc
          }
      }
    catch (TransformerException e)
      {
        DOMSourceLocator l = new DOMSourceLocator(node);
        throw new TransformerConfigurationException(e.getMessage(), l, e);
      }
    catch (DOMException e)
      {
        DOMSourceLocator l = new DOMSourceLocator(node);
        throw new TransformerConfigurationException(e.getMessage(), l, e);
      }
    catch (XPathExpressionException e)
      {
        DOMSourceLocator l = new DOMSourceLocator(node);
        throw new TransformerConfigurationException(e.getMessage(), l, e);
      }
  }

  final NameTest parseNameTest(String token)
  {
    if ("*".equals(token))
      {
        return new NameTest(null, true, true);
      }
    else if (token.endsWith(":*"))
      {
        QName qName = getQName(token.substring(0, token.length() - 2));
        return new NameTest(qName, true, false);
      }
    else
      {
        QName qName = getQName(token);
        return new NameTest(qName, false, false);
      }
  }

  final QName getQName(String name)
  {
    QName qName = QName.valueOf(name);
    String prefix = qName.getPrefix();
    String uri = qName.getNamespaceURI();
    if (prefix != null && (uri == null || uri.length() == 0))
      {
        uri = getNamespaceURI(prefix);
        String localName = qName.getLocalPart();
        qName = new QName(uri, localName, prefix);
      }
    return qName;
  }

  final TemplateNode parseAttributeValueTemplate(String value, Node source)
    throws TransformerConfigurationException, XPathExpressionException
  {
    current = source;
    // Tokenize
    int len = value.length();
    int off = 0;
    List tokens = new ArrayList(); // text tokens
    List types = new ArrayList(); // literal or expression
    int depth = 0;
    for (int i = 0; i < len; i++)
      {
        char c = value.charAt(i);
        if (c == '{')
          {
            if (i < (len - 1) && value.charAt(i + 1) == '{')
              {
                tokens.add(value.substring(off, i + 1));
                types.add(Boolean.FALSE);
                i++;
                off = i + 1;
                continue;
              }
            if (depth == 0)
              {
                if (i - off > 0)
                  {
                    tokens.add(value.substring(off, i));
                    types.add(Boolean.FALSE);
                  }
                off = i + 1;
              }
            depth++;
          }
        else if (c == '}')
          {
            if (i < (len - 1) && value.charAt(i + 1) == '}')
              {
                tokens.add(value.substring(off, i + 1));
                types.add(Boolean.FALSE);
                i++;
                off = i + 1;
                continue;
              }
            if (depth == 1)
              {
                if (i - off > 0)
                  {
                    tokens.add(value.substring(off, i));
                    types.add(Boolean.TRUE);
                  }
                else
                  {
                    String msg = "attribute value template " +
                      "must contain expression: " + value;
                    DOMSourceLocator l = new DOMSourceLocator(source);
                    throw new TransformerConfigurationException(msg, l);
                  }
                off = i + 1;
              }
            depth--;
          }
      }
    if (depth > 0)
      {
        String msg = "invalid attribute value template: " + value;
        throw new TransformerConfigurationException(msg);
      }
    if (len - off > 0)
      {
        // Trailing text
        tokens.add(value.substring(off));
        types.add(Boolean.FALSE);
      }
    
    // Construct template node tree
    TemplateNode ret = null;
    Document doc = source.getOwnerDocument();
    len = tokens.size();
    for (int i = len - 1; i >= 0; i--)
      {
        String token = (String) tokens.get(i);
        Boolean type = (Boolean) types.get(i);
        if (type == Boolean.TRUE)
          {
            // Expression text
            Expr select = (Expr) xpath.compile(token);
            TemplateNode ret2 = new ValueOfNode(select, false);
            ret2.next = ret;
            ret = ret2;
          }
        else
          {
            // Verbatim text
            TemplateNode ret2 = new LiteralNode(doc.createTextNode(token));
            ret2.next = ret;
            ret = ret2;
          }
      }
    return ret;
  }

  boolean isPreserved(Text text)
    throws TransformerConfigurationException
  {
    // Check characters in text
    String value = text.getData();
    if (value != null)
      {
        int len = value.length();
        for (int i = 0; i < len; i++)
          {
            char c = value.charAt(i);
            if (c != 0x20 && c != 0x09 && c != 0x0a && c != 0x0d)
              {
                return true;
              }
          }
      }
    // Check parent node
    Node ctx = text.getParentNode();
    if (!preserveSpace.isEmpty())
      {
        for (Iterator i = preserveSpace.iterator(); i.hasNext(); )
          {
            NameTest preserveTest = (NameTest) i.next();
            if (preserveTest.matches(ctx, 1, 1))
              {
                boolean override = false;
                if (!stripSpace.isEmpty())
                  {
                    for (Iterator j = stripSpace.iterator(); j.hasNext(); )
                      {
                        NameTest stripTest = (NameTest) j.next();
                        if (stripTest.matches(ctx, 1, 1))
                          {
                            override = true;
                            break;
                          }
                      }
                  }
                if (!override)
                  {
                    return true;
                  }
              }
          }
      }
    // Check whether any ancestor specified xml:space
    while (ctx != null)
      {
        if (ctx.getNodeType() == Node.ELEMENT_NODE)
          {
            Element element = (Element) ctx;
            String xmlSpace = element.getAttribute("xml:space");
            if ("default".equals(xmlSpace))
              {
                break;
              }
            else if ("preserve".equals(xmlSpace))
              {
                return true;
              }
            else if (xmlSpace.length() > 0)
              {
                String msg = "Illegal value for xml:space: " + xmlSpace;
                throw new TransformerConfigurationException(msg);
              }
            else if ("text".equals(ctx.getLocalName()) &&
                     XSL_NS.equals(ctx.getNamespaceURI()))
              {
                // xsl:text implies xml:space='preserve'
                return true;
              }
          }
        ctx = ctx.getParentNode();
      }
    return false;
  }

  public XPathFunction resolveFunction(QName name, int arity)
  {
    String uri = name.getNamespaceURI();
    if (XSL_NS.equals(uri) || uri == null || uri.length() == 0)
      {
        String localName = name.getLocalPart();
        if ("document".equals(localName) && (arity == 1 || arity == 2))
          {
            if (current == null)
              {
                throw new RuntimeException("current is null");
              }
            return new DocumentFunction(getRootStylesheet(), current);
          }
        else if ("key".equals(localName) && (arity == 2))
          {
            return new KeyFunction(getRootStylesheet());
          }
        else if ("format-number".equals(localName) &&
                 (arity == 2 || arity == 3))
          {
            return new FormatNumberFunction(getRootStylesheet());
          }
        else if ("current".equals(localName) && (arity == 0))
          {
            return new CurrentFunction(getRootStylesheet());
          }
        else if ("unparsed-entity-uri".equals(localName) && (arity == 1))
          {
            return new UnparsedEntityUriFunction();
          }
        else if ("generate-id".equals(localName) &&
                 (arity == 1 || arity == 0))
          {
            return new GenerateIdFunction();
          }
        else if ("system-property".equals(localName) && (arity == 1))
          {
            return new SystemPropertyFunction();
          }
        else if ("element-available".equals(localName) && (arity == 1))
          {
            return new ElementAvailableFunction(this);
          }
        else if ("function-available".equals(localName) && (arity == 1))
          {
            return new FunctionAvailableFunction(this);
          }
      }
    return null;
  }
  
  // -- Parsing --

  /**
   * apply-templates
   */
  final TemplateNode parseApplyTemplates(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String m = getAttribute(attrs, "mode");
    QName mode = (m == null) ? null : getQName(m);
    String s = getAttribute(attrs, "select");
    if (s == null)
      {
        s = "child::node()";
      }
    Node children = node.getFirstChild();
    List sortKeys = parseSortKeys(children);
    List withParams = parseWithParams(children);
    Expr select = (Expr) xpath.compile(s);
    return new ApplyTemplatesNode(select, mode,
                                  sortKeys, withParams, false);
  }

  /**
   * call-template
   */
  final TemplateNode parseCallTemplate(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String n = getRequiredAttribute(attrs, "name", node);
    QName name = getQName(n);
    Node children = node.getFirstChild();
    List withParams = parseWithParams(children);
    return new CallTemplateNode(name, withParams);
  }
  
  /**
   * value-of
   */
  final TemplateNode parseValueOf(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String s = getRequiredAttribute(attrs, "select", node);
    String doe = getAttribute(attrs, "disable-output-escaping");
    boolean d = "yes".equals(doe);
    Expr select = (Expr) xpath.compile(s);
    return new ValueOfNode(select, d);
  }
  
  /**
   * for-each
   */
  final TemplateNode parseForEach(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String s = getRequiredAttribute(attrs, "select", node);
    Node children = node.getFirstChild();
    List sortKeys = parseSortKeys(children);
    Expr select = (Expr) xpath.compile(s);
    ForEachNode ret = new ForEachNode(select, sortKeys);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * if
   */
  final TemplateNode parseIf(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String t = getRequiredAttribute(attrs, "test", node);
    Expr test = (Expr) xpath.compile(t);
    Node children = node.getFirstChild();
    IfNode ret = new IfNode(test);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * when
   */
  final TemplateNode parseWhen(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String t = getRequiredAttribute(attrs, "test", node);
    Expr test = (Expr) xpath.compile(t);
    Node children = node.getFirstChild();
    WhenNode ret = new WhenNode(test);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * element
   */
  final TemplateNode parseElement(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String name = getRequiredAttribute(attrs, "name", node);
    String namespace = getAttribute(attrs, "namespace");
    String uas = getAttribute(attrs, "use-attribute-sets");
    TemplateNode n = parseAttributeValueTemplate(name, node);
    TemplateNode ns = (namespace == null) ? null :
      parseAttributeValueTemplate(namespace, node);
    Node children = node.getFirstChild();
    ElementNode ret = new ElementNode(n, ns, uas, node);
    ret.children = parse(children);
    return ret;
  }

  /**
   * attribute
   */
  final TemplateNode parseAttribute(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String name = getRequiredAttribute(attrs, "name", node);
    String namespace = getAttribute(attrs, "namespace");
    TemplateNode n = parseAttributeValueTemplate(name, node);
    TemplateNode ns = (namespace == null) ? null :
      parseAttributeValueTemplate(namespace, node);
    Node children = node.getFirstChild();
    AttributeNode ret = new AttributeNode(n, ns, node);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * text
   */
  final TemplateNode parseText(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String doe = getAttribute(attrs, "disable-output-escaping");
    boolean d = "yes".equals(doe);
    Node children = node.getFirstChild();
    TextNode ret = new TextNode(d);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * copy
   */
  final TemplateNode parseCopy(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String uas = getAttribute(attrs, "use-attribute-sets");
    Node children = node.getFirstChild();
    CopyNode ret = new CopyNode(uas);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * processing-instruction
   */
  final TemplateNode parseProcessingInstruction(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String name = getRequiredAttribute(attrs, "name", node);
    Node children = node.getFirstChild();
    ProcessingInstructionNode ret = new ProcessingInstructionNode(name);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * number
   */
  final TemplateNode parseNumber(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String v = getAttribute(attrs, "value");
    String ff = getAttribute(attrs, "format");
    if (ff == null)
      {
        ff = "1";
      }
    TemplateNode format = parseAttributeValueTemplate(ff, node);
    String lang = getAttribute(attrs, "lang");
    String lv = getAttribute(attrs, "letter-value");
    int letterValue = "traditional".equals(lv) ?
      AbstractNumberNode.TRADITIONAL :
      AbstractNumberNode.ALPHABETIC;
    String gs = getAttribute(attrs, "grouping-separator");
    String gz = getAttribute(attrs, "grouping-size");
    int gz2 = (gz != null && gz.length() > 0) ?
      Integer.parseInt(gz) : 1;
    Node children = node.getFirstChild();
    TemplateNode ret;
    if (v != null && v.length() > 0)
      {
        Expr value = (Expr) xpath.compile(v);
        ret = new NumberNode(value, format, lang,
                              letterValue, gs, gz2);
      }
    else
      {
        String l = getAttribute(attrs, "level");
        int level =
          "multiple".equals(l) ? NodeNumberNode.MULTIPLE :
                      "any".equals(l) ? NodeNumberNode.ANY :
                      NodeNumberNode.SINGLE;
        String c = getAttribute(attrs, "count");
        String f = getAttribute(attrs, "from");
        Pattern count = null;
        Pattern from = null;
        if (c != null)
          {
            try
              {
                count = (Pattern) xpath.compile(c);
              }
            catch (ClassCastException e)
              {
                String msg = "invalid pattern: " + c;
                throw new TransformerConfigurationException(msg);
              }
          }
        if (f != null)
          {
            try
              {
                from = (Pattern) xpath.compile(f);
              }
            catch (ClassCastException e)
              {
                String msg = "invalid pattern: " + f;
                throw new TransformerConfigurationException(msg);
              }
          }
        ret = new NodeNumberNode(level, count, from,
                                  format, lang,
                                  letterValue, gs, gz2);
      }
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * copy-of
   */
  final TemplateNode parseCopyOf(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String s = getRequiredAttribute(attrs, "select", node);
    Expr select = (Expr) xpath.compile(s);
    Node children = node.getFirstChild();
    CopyOfNode ret = new CopyOfNode(select);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * message
   */
  final TemplateNode parseMessage(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    NamedNodeMap attrs = node.getAttributes();
    String t = getAttribute(attrs, "terminate");
    boolean terminate = "yes".equals(t);
    Node children = node.getFirstChild();
    MessageNode ret = new MessageNode(terminate);
    ret.children = parse(children);
    return ret;
  }
  
  /**
   * Parse template-level elements.
   */
  final TemplateNode parse(Node node)
    throws TransformerConfigurationException
  {
    TemplateNode first = null;
    TemplateNode previous = null;
    while (node != null)
      {
        Node next = node.getNextSibling();
        TemplateNode tnode = doParse(node);
        if (tnode != null)
          {
            if (first == null)
              {
                first = tnode;
      }
            if (previous != null)
              {
                previous.next = tnode;
              }
            previous = tnode;
          }
        node = next;
      }
    return first;
  }

  private final TemplateNode doParse(Node node)
    throws TransformerConfigurationException
  {
    // Hack to associate the document function with its declaring node
    current = node;
    try
      {
        String namespaceUri = node.getNamespaceURI();
        if (Stylesheet.XSL_NS.equals(namespaceUri) &&
            Node.ELEMENT_NODE == node.getNodeType())
          {
            String name = node.getLocalName();
            if ("apply-templates".equals(name))
              {
                return parseApplyTemplates(node);
              }
            else if ("call-template".equals(name))
              {
                return parseCallTemplate(node);
              }
            else if ("value-of".equals(name))
              {
                return parseValueOf(node);
              }
            else if ("for-each".equals(name))
              {
                return parseForEach(node);
              }
            else if ("if".equals(name))
              {
                return parseIf(node);
              }
            else if ("choose".equals(name))
              {
                Node children = node.getFirstChild();
                ChooseNode ret = new ChooseNode();
                ret.children = parse(children);
                return ret;
              }
            else if ("when".equals(name))
              {
                return parseWhen(node);
              }
            else if ("otherwise".equals(name))
              {
                Node children = node.getFirstChild();
                OtherwiseNode ret = new OtherwiseNode();
                ret.children = parse(children);
                return ret;
              }
            else if ("element".equals(name))
              {
                return parseElement(node);
              }
            else if ("attribute".equals(name))
              {
                return parseAttribute(node);
              }
            else if ("text".equals(name))
              {
                return parseText(node);
              }
            else if ("copy".equals(name))
              {
                return parseCopy(node);
              }
            else if ("processing-instruction".equals(name))
              {
                return parseProcessingInstruction(node);
              }
            else if ("comment".equals(name))
              {
                Node children = node.getFirstChild();
                CommentNode ret = new CommentNode();
                ret.children = parse(children);
                return ret;
              }
            else if ("number".equals(name))
              {
                return parseNumber(node);
              }
            else if ("param".equals(name) ||
                     "variable".equals(name))
              {
                boolean global = "variable".equals(name);
                NamedNodeMap attrs = node.getAttributes();
                Node children = node.getFirstChild();
                TemplateNode content = parse(children);
                String paramName = getRequiredAttribute(attrs, "name", node);
                String select = getAttribute(attrs, "select");
                ParameterNode ret;
                if (select != null)
                  {
                    if (content != null)
                      {
                        String msg = "parameter '" + paramName +
                          "' has both select and content";
                        DOMSourceLocator l = new DOMSourceLocator(node);
                        throw new TransformerConfigurationException(msg, l);
                      }
                    Expr expr = (Expr) xpath.compile(select);
                    ret = new ParameterNode(paramName, expr, global);
                  }
                else
                  {
                    ret = new ParameterNode(paramName, null, global);
                    ret.children = content;
                  }
                return ret;
              }
            else if ("copy-of".equals(name))
              {
                return parseCopyOf(node);
              }
            else if ("message".equals(name))
              {
                return parseMessage(node);
              }
            else if ("apply-imports".equals(name))
              {
                Node children = node.getFirstChild();
                ApplyImportsNode ret = new ApplyImportsNode();
                ret.children = parse(children);
                return ret;
              }
            else
              {
                // xsl:fallback
                // Pass over any other XSLT nodes
                return null;
              }
          }
        String prefix = node.getPrefix();
        if (extensionElementPrefixes.contains(prefix))
          {
            // Pass over extension elements
            return null;
          }
        switch (node.getNodeType())
          {
          case Node.TEXT_NODE:
            // Determine whether to strip whitespace
            Text text = (Text) node;
            if (!isPreserved(text))
              {
                // Strip
                text.getParentNode().removeChild(text);
                return null;
              }
            break;
          case Node.COMMENT_NODE:
            // Ignore comments
            return null;
          case Node.ELEMENT_NODE:
            // Check for attribute value templates and use-attribute-sets
            NamedNodeMap attrs = node.getAttributes();
            boolean convert = false;
            String useAttributeSets = null;
            int len = attrs.getLength();
            for (int i = 0; i < len; i++)
              {
                Node attr = attrs.item(i);
                String value = attr.getNodeValue();
                if (Stylesheet.XSL_NS.equals(attr.getNamespaceURI()) &&
                    "use-attribute-sets".equals(attr.getLocalName()))
                  {
                    useAttributeSets = value;
                    convert = true;
                    break;
                  }
                int start = value.indexOf('{');
                int end = value.indexOf('}');
                if (start != -1 || end != -1)
                  {
                    convert = true;
                    break;
                  }
              }
            if (convert)
              {
                // Create an element-producing template node instead
                // with appropriate attribute-producing child template nodes
                Node children = node.getFirstChild();
                TemplateNode child = parse(children);
                for (int i = 0; i < len; i++)
                  {
                    Node attr = attrs.item(i);
                    String ans = attr.getNamespaceURI();
                    String aname = attr.getNodeName();
                    if (Stylesheet.XSL_NS.equals(ans) &&
                        "use-attribute-sets".equals(attr.getLocalName()))
                      {
                        continue;
                      }
                    String value = attr.getNodeValue();
                    TemplateNode grandchild =
                      parseAttributeValueTemplate(value, node);
                    TemplateNode n =
                      parseAttributeValueTemplate(aname, node);
                    TemplateNode ns = (ans == null) ? null :
                      parseAttributeValueTemplate(ans, node);
                    TemplateNode newChild = new AttributeNode(n, ns, attr);
                    newChild.children = grandchild;
                    newChild.next = child;
                    child = newChild;
                  }
                String ename = node.getNodeName();
                TemplateNode n = parseAttributeValueTemplate(ename, node);
                TemplateNode ns = (namespaceUri == null) ? null :
                  parseAttributeValueTemplate(namespaceUri, node);
                ElementNode ret = new ElementNode(n, ns, useAttributeSets,
                                       node);
                ret.children = child;
                return ret;
              }
            // Otherwise fall through
            break;
          }
      }
    catch (XPathExpressionException e)
      {
        DOMSourceLocator l = new DOMSourceLocator(node);
        throw new TransformerConfigurationException(e.getMessage(), l, e);
      }
    Node children = node.getFirstChild();
    LiteralNode ret = new LiteralNode(node);
    ret.children = parse(children);
    return ret;
  }

  final List parseSortKeys(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    List ret = new LinkedList();
    while (node != null)
      {
        String namespaceUri = node.getNamespaceURI();
        if (Stylesheet.XSL_NS.equals(namespaceUri) &&
            Node.ELEMENT_NODE == node.getNodeType() &&
            "sort".equals(node.getLocalName()))
          {
            NamedNodeMap attrs = node.getAttributes();
            String s = getAttribute(attrs, "select");
            if (s == null)
              {
                s = ".";
              }
            Expr select = (Expr) xpath.compile(s);
            String l = getAttribute(attrs, "lang");
            TemplateNode lang = (l == null) ? null :
              parseAttributeValueTemplate(l, node);
            String dt = getAttribute(attrs, "data-type");
            TemplateNode dataType = (dt == null) ? null :
              parseAttributeValueTemplate(dt, node);
            String o = getAttribute(attrs, "order");
            TemplateNode order = (o == null) ? null :
              parseAttributeValueTemplate(o, node);
            String co = getAttribute(attrs, "case-order");
            TemplateNode caseOrder = (co == null) ? null :
              parseAttributeValueTemplate(co, node);
            ret.add(new SortKey(select, lang, dataType, order, caseOrder));
          }
        node = node.getNextSibling();
      }
    return ret.isEmpty() ? null : ret;
  }

  final List parseWithParams(Node node)
    throws TransformerConfigurationException, XPathExpressionException
  {
    List ret = new LinkedList();
    while (node != null)
      {
        String namespaceUri = node.getNamespaceURI();
        if (Stylesheet.XSL_NS.equals(namespaceUri) &&
            Node.ELEMENT_NODE == node.getNodeType() &&
            "with-param".equals(node.getLocalName()))
          {
            NamedNodeMap attrs = node.getAttributes();
            TemplateNode content = parse(node.getFirstChild());
            String name = getRequiredAttribute(attrs, "name", node);
            String select = getAttribute(attrs, "select");
            if (select != null)
              {
                if (content != null)
                  {
                    String msg = "parameter '" + name +
                      "' has both select and content";
                    DOMSourceLocator l = new DOMSourceLocator(node);
                    throw new TransformerConfigurationException(msg, l);
                  }
                Expr expr = (Expr) xpath.compile(select);
                ret.add(new WithParam(name, expr));
              }
            else
              {
                ret.add(new WithParam(name, content));
              }
          }
        node = node.getNextSibling();
      }
    return ret.isEmpty() ? null : ret;
  }

  /**
   * Created element nodes have a copy of the namespace nodes in the
   * stylesheet, except the XSLT namespace, extension namespaces, and
   * exclude-result-prefixes.
   */
  final void addNamespaceNodes(Node source, Node target, Document doc,
                               Collection elementExcludeResultPrefixes)
  {
    NamedNodeMap attrs = source.getAttributes();
    if (attrs != null)
      {
        int len = attrs.getLength();
        for (int i = 0; i < len; i++)
          {
            Node attr = attrs.item(i);
            String uri = attr.getNamespaceURI();
            if (uri == XMLConstants.XMLNS_ATTRIBUTE_NS_URI)
              {
                String prefix = attr.getLocalName();
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
                  {
                    prefix = "#default";
                  }
                String ns = attr.getNodeValue();
                // Should the namespace be excluded?
                if (XSL_NS.equals(ns) ||
                    extensionElementPrefixes.contains(prefix) ||
                    elementExcludeResultPrefixes.contains(prefix) ||
                    excludeResultPrefixes.contains(prefix))
                  {
                    continue;
                  }
                // Is the namespace already defined on the target?
                if (prefix == "#default")
                  {
                    prefix = null;
                  }
                if (target.lookupNamespaceURI(prefix) != null)
                  {
                    continue;
                  }
                attr = attr.cloneNode(true);
                attr = doc.adoptNode(attr);
                target.getAttributes().setNamedItemNS(attr);
              }
          }
      }
    Node parent = source.getParentNode();
    if (parent != null)
      {
        addNamespaceNodes(parent, target, doc, elementExcludeResultPrefixes);
      }
  }

  static final String getAttribute(NamedNodeMap attrs, String name)
  {
    Node attr = attrs.getNamedItem(name);
    if (attr == null)
      {
        return null;
      }
    String ret = attr.getNodeValue();
    if (ret.length() == 0)
      {
        return null;
      }
    return ret;
  }

  static final String getRequiredAttribute(NamedNodeMap attrs, String name,
                                           Node source)
    throws TransformerConfigurationException
  {
    String value = getAttribute(attrs, name);
    if (value == null || value.length() == 0)
      {
        String msg =
          name + " attribute is required on " + source.getNodeName();
        DOMSourceLocator l = new DOMSourceLocator(source);
        throw new TransformerConfigurationException(msg, l);
      }
    return value;
  }

  // Handle user data changes when nodes are cloned etc

  public void handle(short op, String key, Object data, Node src, Node dst)
  {
    dst.setUserData(key, data, this);
  }

}

