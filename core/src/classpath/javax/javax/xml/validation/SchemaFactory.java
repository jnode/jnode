/* SchemaFactory.java -- 
   Copyright (C) 2004, 2005, 2006  Free Software Foundation, Inc.

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

package javax.xml.validation;

import java.io.File;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Factory for obtaining schemata.
 *
 * @author Chris Burdess (dog@gnu.org)
 * @since 1.5
 */
public abstract class SchemaFactory
{
  protected SchemaFactory()
  {
  }

  /**
   * Returns an implementation of <code>SchemaFactory</code> that supports
   * the specified schema language.
   * @param schemaLanguage the URI of a schema language (see
   * <code>XMLConstants</code>)
   */
  public static final SchemaFactory newInstance(String schemaLanguage)
  {
    if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(schemaLanguage))
      return new gnu.xml.validation.xmlschema.XMLSchemaSchemaFactory();
    if (XMLConstants.RELAXNG_NS_URI.equals(schemaLanguage))
      return new gnu.xml.validation.relaxng.RELAXNGSchemaFactory();
    throw new IllegalArgumentException(schemaLanguage);
  }

  /**
   * Indicates whether the specified schema language is supported.
   * @param schemaLanguage the URI of a schema language (see
   * <code>XMLConstants</code>)
   */
  public abstract boolean isSchemaLanguageSupported(String schemaLanguage);

  public boolean getFeature(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException(name);
  }
  
  public void setFeature(String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException(name);
  }
  
  public Object getProperty(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException(name);
  }
  
  public void setProperty(String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException(name);
  }
  
  public abstract ErrorHandler getErrorHandler();
  
  public abstract void setErrorHandler(ErrorHandler errorHandler);

  public abstract LSResourceResolver getResourceResolver();

  public abstract void setResourceResolver(LSResourceResolver resourceResolver);
  
  /**
   * Returns a schema based on the specified source resource.
   * @param schema the source resource
   */
  public Schema newSchema(Source schema)
    throws SAXException
  {
    return newSchema(new Source[] { schema });
  }

  /**
   * Returns a schema based on the specified source file.
   * @param schema the source resource
   */
  public Schema newSchema(File schema)
    throws SAXException
  {
    return newSchema(new StreamSource(schema));
  }
  
  /**
   * Returns a schema based on the specified URL.
   * @param schema the source resource
   */
  public Schema newSchema(URL schema)
    throws SAXException
  {
    return newSchema(new StreamSource(schema.toString()));
  }
  
  /**
   * Parses the specified sources, and combine them into a single schema.
   * The exact procedure and semantics of this depends on the schema
   * language.
   * @param schemata the schema resources to load
   */
  public abstract Schema newSchema(Source[] schemata)
    throws SAXException;
  
  /**
   * Creates a special schema.
   * The exact semantics of this depends on the schema language.
   */
  public abstract Schema newSchema()
    throws SAXException;
  
}
