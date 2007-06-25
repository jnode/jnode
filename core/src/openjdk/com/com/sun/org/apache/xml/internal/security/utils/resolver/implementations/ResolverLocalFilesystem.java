/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.sun.org.apache.xml.internal.security.utils.resolver.implementations;



import java.io.FileInputStream;

import com.sun.org.apache.xml.internal.utils.URI;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;


/**
 * A simple ResourceResolver for requests into the local filesystem.
 *
 * @author $Author: raul $
 */
public class ResolverLocalFilesystem extends ResourceResolverSpi {

   /** {@link java.util.logging} logging facility */
    static java.util.logging.Logger log = 
        java.util.logging.Logger.getLogger(
                    ResolverLocalFilesystem.class.getName());

   /**
    * @inheritDoc
    */
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
           throws ResourceResolverException {

     try {
        URI uriNew = new URI(new URI(BaseURI), uri.getNodeValue());

        // if the URI contains a fragment, ignore it
        URI uriNewNoFrag = new URI(uriNew);

        uriNewNoFrag.setFragment(null);

        String fileName =
           ResolverLocalFilesystem
              .translateUriToFilename(uriNewNoFrag.toString());
        FileInputStream inputStream = new FileInputStream(fileName);
        XMLSignatureInput result = new XMLSignatureInput(inputStream);

        result.setSourceURI(uriNew.toString());

        return result;
     } catch (Exception e) {
        throw new ResourceResolverException("generic.EmptyMessage", e, uri,
                                            BaseURI);
      }
   }

   /**
    * Method translateUriToFilename
    *
    * @param uri
    * @return the string of the filename
    */
   private static String translateUriToFilename(String uri) {

      String subStr = uri.substring("file:/".length());

      if (subStr.indexOf("%20") > -1)
      {
        int offset = 0;
        int index = 0;
        StringBuffer temp = new StringBuffer(subStr.length());
        do
        {
          index = subStr.indexOf("%20",offset);
          if (index == -1) temp.append(subStr.substring(offset));
          else
          {
            temp.append(subStr.substring(offset,index));
            temp.append(' ');
            offset = index+3;
          }
        }
        while(index != -1);
        subStr = temp.toString();
      }

      if (subStr.charAt(1) == ':') {
      	 // we're running M$ Windows, so this works fine
         return subStr;
      }
      // we're running some UNIX, so we have to prepend a slash
      return "/" + subStr;
   }

   /**
    * @inheritDoc
    */
   public boolean engineCanResolve(Attr uri, String BaseURI) {

      if (uri == null) {
         return false;
      }

      String uriNodeValue = uri.getNodeValue();

      if (uriNodeValue.equals("") || (uriNodeValue.charAt(0)=='#')) {
         return false;
      }

      try {
	         //URI uriNew = new URI(new URI(BaseURI), uri.getNodeValue());
	         if (true)
	         	if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "I was asked whether I can resolve " + uriNodeValue/*uriNew.toString()*/);

	         if ( uriNodeValue.startsWith("file:") ||
					 BaseURI.startsWith("file:")/*uriNew.getScheme().equals("file")*/) {
	            if (true)
	            	if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "I state that I can resolve " + uriNodeValue/*uriNew.toString()*/);

	            return true;
	         }
      } catch (Exception e) {}

      if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "But I can't");

      return false;
   }
}
