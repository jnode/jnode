/* SAXParserFactory.java -- 
   Copyright (C) 2005  Free Software Foundation, Inc.

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

package gnu.xml.stream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * SAX parser factory providing a SAX compatibility layer on top of StAX.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class SAXParserFactory
  extends javax.xml.parsers.SAXParserFactory
{

  static final Set FEATURE_NAMES = new HashSet();
  static
  {
    FEATURE_NAMES.add("http://xml.org/sax/features/namespaces");
    FEATURE_NAMES.add("http://xml.org/sax/features/string-interning");
    FEATURE_NAMES.add("http://xml.org/sax/features/validation");
  }

  Map features = new HashMap();

  public javax.xml.parsers.SAXParser newSAXParser()
    throws ParserConfigurationException, SAXException
  {
    boolean validating = isValidating();
    boolean namespaceAware = isNamespaceAware();
    boolean xIncludeAware = isXIncludeAware();
    SAXParser ret = new SAXParser(validating, namespaceAware, xIncludeAware);
    for (Iterator i = features.entrySet().iterator(); i.hasNext(); )
      {
        Map.Entry entry = (Map.Entry) i.next();
        String name = (String) entry.getKey();
        Boolean value = (Boolean) entry.getValue();
        ret.setFeature(name, value.booleanValue());
      }
    return ret;
  }

  public void setFeature(String name, boolean value)
    throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
  {
    if (!FEATURE_NAMES.contains(name))
      throw new SAXNotSupportedException(name);
    features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
  }

  public boolean getFeature(String name) 
    throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException  
  {
    if (!FEATURE_NAMES.contains(name))
      throw new SAXNotSupportedException(name);
    Boolean value = (Boolean) features.get(name);
    return (value == null) ? false : value.booleanValue();
  }
  
}
